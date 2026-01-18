package app.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; 

import app.auth.dto.request.*;
import app.auth.dto.response.AuthResponse;
import app.auth.dto.response.UserResponse;
import app.auth.exception.*;
import app.auth.model.PasswordResetToken;
import app.auth.model.RefreshToken;
import app.auth.model.User;
import app.auth.model.enums.AuthProvider;
import app.auth.model.enums.UserRole;
import app.auth.model.enums.UserStatus;
import app.auth.repository.PasswordResetTokenRepository;
import app.auth.repository.UserRepository;
import app.auth.security.JwtTokenProvider;
import app.service.CloudinaryService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final GoogleOAuthService googleOAuthService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    
    // 1. Thêm service này để upload ảnh
    private final CloudinaryService cloudinaryService;
    
    // --- ĐĂNG KÝ ---
    @Transactional
    public AuthResponse register(RegisterRequest request, MultipartFile avatar) { // Thêm tham số avatar
        log.info("Registering new user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email đã được sử dụng");
        }

        if (request.getUserRole() == UserRole.ADMIN) {
            throw new UnauthorizedException("Không thể đăng ký vai trò Quản trị viên qua đường dẫn này");
        }
        
        // Sinh mã xác thực 6 ký tự
        String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(request.getUserRole())
                .authProvider(AuthProvider.LOCAL)
                .status(UserStatus.PENDING_VERIFICATION) // Chờ xác thực
                .isEmailVerified(false)
                .verificationCode(verificationCode)
                .build();
        
        // --- LOGIC UPLOAD AVATAR MỚI ---
        if (avatar != null && !avatar.isEmpty()) {
            try {
                // Gọi hàm uploadAvatar bên CloudinaryService
                String avatarUrl = cloudinaryService.uploadAvatar(avatar);
                user.setProfileImageUrl(avatarUrl);
            } catch (Exception e) {
                log.error("Lỗi upload avatar khi đăng ký: {}", e.getMessage());
                // Nếu lỗi thì vẫn cho đăng ký nhưng dùng ảnh mặc định
                user.setProfileImageUrl("https://res.cloudinary.com/dqp6v7g3r/image/upload/v1/avatar/default_avatar");
            }
        } else {
            // Ảnh mặc định nếu người dùng không chọn ảnh
            user.setProfileImageUrl("https://res.cloudinary.com/dqp6v7g3r/image/upload/v1/avatar/default_avatar");
        }
        // -------------------------------

        user = userRepository.save(user);
        
        // Gửi email xác thực
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        
        log.info("User registered successfully via email, waiting for verification: {}", user.getId());
        
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    // --- XÁC THỰC EMAIL ---
    @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));

        if (user.getIsEmailVerified()) {
            throw new AuthException("Tài khoản đã được xác thực trước đó");
        }

        // Kiểm tra mã xác thực
        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            throw new InvalidTokenException("Mã xác thực không chính xác");
        }

        // Kích hoạt tài khoản
        user.setStatus(UserStatus.ACTIVE);
        user.setIsEmailVerified(true);
        user.setVerificationCode(null); // Xóa mã sau khi dùng
        userRepository.save(user);
        
        log.info("User verified email successfully: {}", email);
    }
    
    // --- ĐĂNG NHẬP LOCAL ---
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt with email: {}", request.getEmail());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email hoặc mật khẩu không đúng"));
        
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
             throw new UnauthorizedException("Vui lòng xác thực email trước khi đăng nhập");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Tài khoản đã bị khóa hoặc chưa được kích hoạt");
        }
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }
    
    // --- ĐĂNG NHẬP GOOGLE ---
    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        log.info("Processing Google Login");

        Map<String, String> googleInfo = googleOAuthService.verifyGoogleToken(request.getGoogleToken());
        
        String email = googleInfo.get("email");
        String googleId = googleInfo.get("googleId");
        String name = googleInfo.get("name");
        String pictureUrl = googleInfo.get("pictureUrl");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.info("Creating new user from Google: {}", email);
            user = User.builder()
                    .fullName(name)
                    .email(email)
                    .password(passwordEncoder.encode("GOOGLE_" + UUID.randomUUID()))
                    .userRole(request.getUserRole() != null ? request.getUserRole() : UserRole.CANDIDATE)
                    .authProvider(AuthProvider.GOOGLE)
                    .googleId(googleId)
                    .profileImageUrl(pictureUrl)
                    .status(UserStatus.ACTIVE)
                    .isEmailVerified(true)
                    .build();
            user = userRepository.save(user);
        } else {
            log.info("Updating existing user with Google info: {}", email);
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                user.setAuthProvider(AuthProvider.GOOGLE);
            }
            if (pictureUrl != null) {
                user.setProfileImageUrl(pictureUrl);
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                user.setStatus(UserStatus.ACTIVE);
                user.setIsEmailVerified(true);
            }
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    // --- LÀM MỚI TOKEN ---
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken);
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        return buildAuthResponse(user, newAccessToken, requestRefreshToken);
    }

    // --- ĐĂNG XUẤT ---
    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng"));
        refreshTokenService.deleteByUser(user);
    }

    // --- QUÊN MẬT KHẨU ---
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với email này"));
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);
        
        emailService.sendResetPasswordEmail(user.getEmail(), token);
    }

    // --- ĐẶT LẠI MẬT KHẨU ---
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
    }

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