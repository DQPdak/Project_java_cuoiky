
package app.auth.service;

// Lombok: tự động sinh constructor cho các field final và cung cấp logger
import lombok.RequiredArgsConstructor; // Tạo constructor với các field final
import lombok.extern.slf4j.Slf4j;      // Tạo logger 'log' (log.info, log.error,...)

// Spring Security: xác thực người dùng và mã hóa mật khẩu
import org.springframework.security.authentication.AuthenticationManager;              // Quản lý xác thực tổng quát
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Token chứa email/password để xác thực
import org.springframework.security.core.Authentication;                                // Kết quả xác thực (principal, authorities,...)
import org.springframework.security.crypto.password.PasswordEncoder;                    // Mã hóa mật khẩu (BCrypt,...)

// Spring Framework: khai báo service và giao dịch (transaction)
import org.springframework.stereotype.Service;           // Đánh dấu lớp là Service (bean của Spring)
import org.springframework.transaction.annotation.Transactional; // Quản lý giao dịch DB

// DTO request/response phục vụ API
import app.auth.dto.request.*;
import app.auth.dto.response.AuthResponse;
import app.auth.dto.response.UserResponse;

// Entity và enum của miền nghiệp vụ
import app.auth.entity.PasswordResetToken;
import app.auth.entity.RefreshToken;
import app.auth.entity.User;
import app.auth.entity.enums.AuthProvider;
import app.auth.entity.enums.UserStatus;

// Exception tuỳ chỉnh của ứng dụng
import app.auth.exception.*;

// Repository và provider JWT
import app.auth.repository.PasswordResetTokenRepository;
import app.auth.repository.UserRepository;
import app.auth.security.JwtTokenProvider;

// Thư viện Java thời gian và tiện ích
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * AuthService: Chứa toàn bộ nghiệp vụ liên quan đến xác thực/ủy quyền:
 * - Đăng ký, đăng nhập, đăng nhập bằng Google
 * - Làm mới token, đăng xuất
 * - Quên mật khẩu / đặt lại mật khẩu
 *
 * - @Service: đăng ký bean ở tầng service.
 * - @RequiredArgsConstructor: Lombok sinh constructor nhận các field final.
 * - @Slf4j: cung cấp logger để ghi log.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    // Repository thao tác DB với User
    private final UserRepository userRepository;
    // Mã hóa mật khẩu (BCrypt,...)
    private final PasswordEncoder passwordEncoder;
    // Cung cấp chức năng tạo/parse/validate JWT
    private final JwtTokenProvider jwtTokenProvider;
    // Quản lý xác thực (dùng cho login email/password)
    private final AuthenticationManager authenticationManager;
    // Nghiệp vụ refresh token (tạo / kiểm tra hết hạn / xóa)
    private final RefreshTokenService refreshTokenService;
    // Nghiệp vụ xác thực Google (verify token, lấy thông tin user từ Google)
    private final GoogleOAuthService googleOAuthService;
    // Repository thao tác DB với token đặt lại mật khẩu
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    
    /**
     * Đăng ký tài khoản mới:
     * - Kiểm tra trùng email.
     * - Tạo user (mã hóa mật khẩu, set vai trò, provider LOCAL, trạng thái...).
     * - Lưu DB.
     * - Sinh access token và refresh token.
     * - Trả về AuthResponse chứa token + thông tin user.
     *
     * @Transactional: đảm bảo mọi thao tác DB thực thi trong một giao dịch.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng");
        }
        
        // Tạo user mới (mã hóa mật khẩu trước khi lưu)
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(request.getUserRole())
                .authProvider(AuthProvider.LOCAL)
                .status(UserStatus.ACTIVE) // Hoặc PENDING_VERIFICATION nếu cần xác thực email
                .isEmailVerified(false)
                .build();
        
        user = userRepository.save(user);
        log.info("User registered successfully with id: {}", user.getId());
        
        // Sinh token đăng nhập
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }
    
    /**
     * Đăng nhập bằng email và mật khẩu:
     * - Dùng AuthenticationManager để xác thực (ném lỗi nếu sai).
     * - Nạp user từ DB (phòng hờ trong trường hợp khác).
     * - Kiểm tra trạng thái tài khoản (phải ACTIVE).
     * - Cập nhật thời điểm đăng nhập gần nhất.
     * - Sinh access token (từ Authentication) và refresh token.
     * - Trả về AuthResponse.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt with email: {}", request.getEmail());
        
        // Xác thực email/password (ném lỗi nếu không hợp lệ)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        // Lấy user từ DB (nếu không có, coi như thông tin đăng nhập không đúng)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email hoặc mật khẩu không đúng"));
        
        // Kiểm tra trạng thái tài khoản
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Tài khoản đã bị khóa hoặc chưa được kích hoạt");
        }
        
        // Cập nhật thời điểm đăng nhập
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Sinh token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        
        log.info("User logged in successfully: {}", user.getEmail());
        
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }
    
    /**
     * Đăng nhập/đăng ký bằng Google:
     * - Verify Google token để lấy thông tin (googleId, email, name, picture).
     * - Tìm user theo googleId, nếu không có thì tìm theo email.
     * - Nếu chưa có user:
     *   + Yêu cầu chọn vai trò (CANDIDATE/RECRUITER).
     *   + Tạo user mới (provider GOOGLE, email verified true).
     * - Nếu đã có user:
     *   + Cập nhật googleId/proficeImage nếu chưa có.
     *   + Đánh dấu email đã xác thực, cập nhật lastLoginAt.
     * - Sinh token và trả về AuthResponse.
     */
    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        log.info("Google authentication attempt");
        
        // Xác thực token Google và lấy thông tin người dùng
        Map<String, String> userInfo = googleOAuthService.verifyGoogleToken(request.getGoogleToken());
        
        String googleId = userInfo.get("googleId");
        String email = userInfo.get("email");
        String name = userInfo.get("name");
        String pictureUrl = userInfo.get("pictureUrl");
        
        // Tìm user theo googleId, nếu chưa thì thử theo email
        User user = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email))
                .orElse(null);
        
        if (user == null) {
            // Tạo user mới từ Google (cần vai trò cho lần đầu)
            if (request.getUserRole() == null) {
                throw new AuthException("Vui lòng chọn loại tài khoản (Ứng viên hoặc Nhà tuyển dụng)");
            }
            
            user = User.builder()
                    .fullName(name)
                    .email(email)
                    // Đặt mật khẩu ngẫu nhiên (không dùng để login trực tiếp)
                    .password(passwordEncoder.encode("GOOGLE_AUTH_" + UUID.randomUUID()))
                    .googleId(googleId)
                    .profileImageUrl(pictureUrl)
                    .userRole(request.getUserRole())
                    .authProvider(AuthProvider.GOOGLE)
                    .status(UserStatus.ACTIVE)
                    .isEmailVerified(true)
                    .build();
            
            user = userRepository.save(user);
            log.info("New user created via Google auth: {}", email);
        } else {
            // Cập nhật thông tin cho user đã tồn tại
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }
            if (user.getProfileImageUrl() == null || user.getProfileImageUrl().isEmpty()) {
                user.setProfileImageUrl(pictureUrl);
            }
            user.setIsEmailVerified(true);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Existing user logged in via Google: {}", email);
        }
        
        // Sinh token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }
    
    /**
     * Làm mới access token từ refresh token:
     * - Tìm refresh token theo chuỗi được gửi lên.
     * - Verify hạn dùng của refresh token (ném lỗi nếu hết hạn).
     * - Sinh access token mới từ email người dùng.
     * - Trả về AuthResponse với access token mới và refresh token cũ.
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken);
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        
        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        
        return buildAuthResponse(user, newAccessToken, requestRefreshToken);
    }
    
    /**
     * Đăng xuất người dùng:
     * - Xóa các refresh token của user (vô hiệu hóa phiên đăng nhập).
     */
    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));
        
        refreshTokenService.deleteByUser(user);
        log.info("User logged out: {}", email);
    }
    
    /**
     * Yêu cầu quên mật khẩu:
     * - Tìm user theo email.
     * - Tạo password reset token (UUID) hết hạn sau 24h.
     * - Lưu DB và (TODO) gửi email đường dẫn đặt lại mật khẩu.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với email này"));
        
        // Sinh reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        
        passwordResetTokenRepository.save(resetToken);
        
        // TODO: Gửi email chứa link đặt lại mật khẩu (đính kèm token)
        log.info("Password reset token generated for: {}", user.getEmail());
    }
    
    /**
     * Đặt lại mật khẩu:
     * - Tìm PasswordResetToken theo token.
     * - Kiểm tra đã dùng hay chưa, còn hạn hay không.
     * - Mã hóa và cập nhật mật khẩu user.
     * - Đánh dấu token đã dùng để tránh tái sử dụng.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Token không hợp lệ"));
        
        if (resetToken.getUsed()) {
            throw new InvalidTokenException("Token đã được sử dụng");
        }
        
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token đã hết hạn");
        }
        
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        log.info("Password reset successfully for: {}", user.getEmail());
    }
    
    /**
     * buildAuthResponse: Chuẩn hóa dữ liệu trả về cho client sau khi xác thực.
     * - Build UserResponse từ entity User.
     * - Build AuthResponse chứa accessToken, refreshToken, tokenType, expiresIn.
     */
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .status(user.getStatus())
                .profileImageUrl(user.getProfileImageUrl())
                .isEmailVerified(user.getIsEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .user(userResponse)
                .build();
       }
}