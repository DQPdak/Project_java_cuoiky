package app.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import app.auth.dto.request.*;
import app.auth.dto.response.AuthResponse;
import app.auth.dto.response.MessageResponse;
import app.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // ... (Các API register, login, google, refresh, logout GIỮ NGUYÊN code cũ) ...
    // Mình viết lại register để bạn thấy thay đổi message
    
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        // Thông báo cho user biết cần check mail
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MessageResponse.success("Đăng ký thành công. Vui lòng kiểm tra email để nhập mã xác thực.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(MessageResponse.success("Đăng nhập thành công", response));
    }
    
    // --- API MỚI: XÁC THỰC EMAIL ---
    /**
     * Xác thực email
     * - HTTP: POST /api/auth/verify-email
     * - Params: email, code
     */
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String email, @RequestParam String code) {
        log.info("Verification request for email: {}", email);
        authService.verifyEmail(email, code);
        return ResponseEntity.ok(MessageResponse.success("Xác thực tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ."));
    }
    // ---------------------------------

    @PostMapping("/google")
    public ResponseEntity<MessageResponse> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        AuthResponse response = authService.googleAuth(request);
        return ResponseEntity.ok(MessageResponse.success("Xác thực Google thành công", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<MessageResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(MessageResponse.success("Token đã được làm mới", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse.error("Không có phiên đăng nhập hợp lệ"));
        }
        authService.logout(authentication.getName());
        return ResponseEntity.ok(MessageResponse.success("Đăng xuất thành công"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(MessageResponse.success("Link đặt lại mật khẩu đã được gửi đến email của bạn"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(MessageResponse.success("Mật khẩu đã được đặt lại thành công"));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API is working!");
    }
} 