package app.payment.controller;

import app.auth.dto.response.AuthResponse;
import app.auth.dto.response.UserResponse; // Import UserResponse
import app.auth.model.User;
import app.auth.model.enums.UserRole;
import app.auth.repository.UserRepository;
import app.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/vip-upgrade")
    public ResponseEntity<?> upgradeToVip() {
        // 1. Lấy email user hiện tại
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Tìm user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRole currentRole = user.getUserRole();
        String currentRoleName = currentRole.name();

        // 3. Logic ghép role
        if (currentRoleName.endsWith("_VIP")) {
            return ResponseEntity.badRequest().body("Tài khoản đã là VIP.");
        }
        if (currentRole == UserRole.ADMIN) {
            return ResponseEntity.badRequest().body("Admin không cần mua VIP.");
        }

        try {
            // 4. Update Role mới
            UserRole newRole = UserRole.valueOf(currentRoleName + "_VIP");
            user.setUserRole(newRole);
            userRepository.save(user);

            // 5. Tạo Token mới
            String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

            // 6. Tạo UserResponse object 
            UserResponse userResponse = UserResponse.builder()
                    .id(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .userRole(newRole) // Role mới đã update
                    .status(user.getStatus())
                    .profileImageUrl(user.getProfileImageUrl())
                    .isEmailVerified(user.getIsEmailVerified())
                    .createdAt(user.getCreatedAt())
                    .lastLoginAt(user.getLastLoginAt())
                    .build();

            // 7. Trả về AuthResponse chứa UserResponse
            return ResponseEntity.ok(AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .user(userResponse) // Gán object user vào đây
                    .expiresIn(jwtTokenProvider.getAccessTokenExpiration()) // Optional nếu cần
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Không tìm thấy gói VIP phù hợp: " + currentRoleName);
        }
    }
}