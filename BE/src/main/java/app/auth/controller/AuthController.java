
package app.auth.controller;

import jakarta.validation.Valid; // Kích hoạt Bean Validation cho DTO (ví dụ @Email, @NotBlank)
import lombok.RequiredArgsConstructor; // Tự sinh constructor cho các field final -> phục vụ DI
import lombok.extern.slf4j.Slf4j; // Tự tạo logger SLF4J: dùng log.info/log.error
import org.springframework.http.HttpStatus; // Xác định mã trạng thái HTTP (200, 201, 400, ...)
import org.springframework.http.ResponseEntity; // Trả về response có status + body
import org.springframework.security.core.Authentication; // (tuỳ chọn) để dùng trong logout an toàn hơn
import org.springframework.security.core.context.SecurityContextHolder; // Lấy thông tin user đang đăng nhập
import org.springframework.web.bind.annotation.*; // Các annotation REST: @RestController/@PostMapping/@GetMapping/...

import app.auth.dto.request.*; // DTO nhận dữ liệu từ client: RegisterRequest, LoginRequest, ...
import app.auth.dto.response.AuthResponse; // DTO dữ liệu auth trả về: accessToken, refreshToken, userInfo
import app.auth.dto.response.MessageResponse; // DTO bao bọc phản hồi: success, message, data
import app.auth.service.AuthService; // Service chứa logic nghiệp vụ cho auth

/**
 * Controller chịu trách nhiệm xử lý các API liên quan đến xác thực/người dùng.
 * Base path: /api/auth
 */
@RestController // Đánh dấu đây là REST controller -> trả JSON
@RequestMapping("/api/auth") // Base path cho các endpoint bên trong
@RequiredArgsConstructor // Tự tạo constructor cho final field (authService) -> thuận tiện DI
@Slf4j // Tạo logger 'log' để ghi log
public class AuthController {

    private final AuthService authService; // Inject AuthService từ Spring Context

    /**
     * Đăng ký tài khoản mới.
     * - HTTP: POST /api/auth/register
     * - Body: RegisterRequest (email, password, fullName, ...)
     * - Validation: @Valid sẽ kiểm tra các ràng buộc trong DTO
     * - Trả về: 201 CREATED + MessageResponse chứa AuthResponse (token và thông tin user)
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for email: {}", request.getEmail());

        // Giao cho service xử lý: kiểm tra trùng email, mã hoá password, tạo user, sinh token...
        AuthResponse response = authService.register(request);

        // Trả về 201 Created, kèm theo message và data
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MessageResponse.success("Đăng ký thành công", response));
    }

    /**
     * Đăng nhập.
     * - HTTP: POST /api/auth/login
     * - Body: LoginRequest (email, password)
     * - Trả về: 200 OK + MessageResponse chứa AuthResponse (accessToken/refreshToken)
     */
    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        // Service kiểm tra thông tin đăng nhập, sinh JWT tokens...
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                MessageResponse.success("Đăng nhập thành công", response)
        );
    }

    /**
     * Đăng nhập/xác thực bằng Google (Social Login).
     * - HTTP: POST /api/auth/google
     * - Body: GoogleAuthRequest (idToken hoặc authorizationCode tuỳ cách triển khai)
     * - Trả về: 200 OK + AuthResponse
     */
    @PostMapping("/google")
    public ResponseEntity<MessageResponse> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        log.info("Google authentication request received");

        // Service xác minh idToken với Google, tạo/tìm user tương ứng, sinh token...
        AuthResponse response = authService.googleAuth(request);

        return ResponseEntity.ok(
                MessageResponse.success("Xác thực Google thành công", response)
        );
    }

    /**
     * Làm mới access token bằng refresh token.
     * - HTTP: POST /api/auth/refresh
     * - Body: RefreshTokenRequest (refreshToken)
     * - Trả về: 200 OK + AuthResponse (token mới)
     */
    @PostMapping("/refresh")
    public ResponseEntity<MessageResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");

        // Service xác thực refresh token, sinh access token mới (và có thể rotate refresh token)
        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(
                MessageResponse.success("Token đã được làm mới", response)
        );
    }

    /**
     * Đăng xuất.
     * - HTTP: POST /api/auth/logout
     * - Yêu cầu: người dùng đang đăng nhập (Authentication không null)
     * - Cơ chế: có thể revoke refresh token, thêm access token vào blacklist (tuỳ policy)
     * - Trả về: 200 OK, không cần body request
     *
     * Lưu ý: Nên bảo vệ endpoint này bằng cấu hình Security (chỉ cho phép authenticated).
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        // Lấy Authentication hiện tại từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Phòng NullPointerException nếu chưa đăng nhập hoặc SecurityContext rỗng
        if (authentication == null || authentication.getName() == null) {
            log.warn("Logout request received but no authenticated user in context");
            // Tuỳ chính sách: có thể trả 401 Unauthorized hoặc 200 OK "không có phiên đăng nhập"
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse.error("Không có phiên đăng nhập hợp lệ"));
        }

        String email = authentication.getName();
        log.info("Logout request received from: {}", email);

        // Service thực hiện logout: revoke tokens, xoá session, ghi log...
        authService.logout(email);

        return ResponseEntity.ok(
                MessageResponse.success("Đăng xuất thành công")
        );
    }

    /**
     * Yêu cầu đặt lại mật khẩu (gửi email kèm link reset).
     * - HTTP: POST /api/auth/forgot-password
     * - Body: ForgotPasswordRequest (email)
     * - Trả về: 200 OK + message
     *
     * Lưu ý: chỉ trả về message chung để tránh lộ user tồn tại hay không (avoid user enumeration).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        // Service sinh reset token và gửi email kèm link
        authService.forgotPassword(request);

        return ResponseEntity.ok(
                MessageResponse.success("Link đặt lại mật khẩu đã được gửi đến email của bạn")
        );
    }

    /**
     * Đặt lại mật khẩu bằng reset token.
     * - HTTP: POST /api/auth/reset-password
     * - Body: ResetPasswordRequest (token, newPassword)
     * - Trả về: 200 OK + message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset password request received");

        // Service xác thực token, mã hoá mật khẩu mới, cập nhật vào DB, vô hiệu hoá token reset sau khi dùng
        authService.resetPassword(request);

        return ResponseEntity.ok(
                MessageResponse.success("Mật khẩu đã được đặt lại thành công")
        );
    }

    /**
     * Endpoint kiểm tra nhanh (health check) cho Auth API.
     * - HTTP: GET /api/auth/test
     * - Trả về: 200 OK + chuỗi đơn giản
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API is working!");
    }
}
