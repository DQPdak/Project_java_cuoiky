
package app.exception;

import lombok.extern.slf4j.Slf4j; // Lombok: tự sinh logger (log) theo chuẩn SLF4J khi dùng @Slf4j
import org.springframework.http.HttpStatus; // Spring: enum HttpStatus để gán mã trạng thái HTTP (400, 401, 404, 409, 500...)
import org.springframework.http.ResponseEntity; // Spring: đối tượng HTTP response có body, status, header
import org.springframework.security.authentication.BadCredentialsException; // Spring Security: ném khi thông tin đăng nhập sai (username/password)
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Spring Security: ném khi không tìm thấy user theo username
import org.springframework.validation.FieldError; // Spring Validation: đại diện lỗi ở từng field cụ thể
import org.springframework.web.bind.MethodArgumentNotValidException; // Spring MVC: ném khi @Valid thất bại (validate request body)
import org.springframework.web.bind.annotation.ExceptionHandler; // Annotation: đánh dấu method xử lý một loại exception cụ thể
import org.springframework.web.bind.annotation.RestControllerAdvice; // Annotation: advice toàn cục cho REST controller (bắt và xử lý exception)
import app.auth.dto.response.MessageResponse; // DTO tùy biến: chuẩn hóa response trả về (success, message, data)
import app.auth.exception.*; // Import các exception tùy biến trong module auth (EmailAlreadyExistsException, UserNotFoundException, ...)

import java.util.HashMap;
import java.util.Map;

/**
 * @RestControllerAdvice:
 *  - Định nghĩa một "global exception handler" cho toàn bộ ứng dụng REST.
 *  - Tự động bắt các exception phát sinh từ Controller và trả về response thống nhất.
 *
 * @Slf4j:
 *  - Từ Lombok: tạo sẵn biến logger 'log' (theo SLF4J) để ghi log (info, error, warn...).
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Xử lý khi email đã tồn tại.
     * - @ExceptionHandler chỉ định bắt EmailAlreadyExistsException.
     * - Trả về HTTP 409 (CONFLICT) kèm MessageResponse.error(message).
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.error("Email already exists: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(MessageResponse.error(ex.getMessage()));
    }
    
    /**
     * Xử lý khi không tìm thấy người dùng (UserNotFoundException).
     * - Trả về HTTP 404 (NOT_FOUND).
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<MessageResponse> handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(MessageResponse.error(ex.getMessage()));
    }
    
    /**
     * Xử lý token không hợp lệ (InvalidTokenException).
     * - Trả về HTTP 401 (UNAUTHORIZED).
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<MessageResponse> handleInvalidToken(InvalidTokenException ex) {
        log.error("Invalid token: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.error(ex.getMessage()));
    }
    
    /**
     * Xử lý truy cập không được phép (UnauthorizedException).
     * - Trả về HTTP 401 (UNAUTHORIZED).
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<MessageResponse> handleUnauthorized(UnauthorizedException ex) {
        log.error("Unauthorized: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.error(ex.getMessage()));
    }
    
    /**
     * Xử lý thông tin đăng nhập không hợp lệ (InvalidCredentialsException).
     * - Trả về HTTP 401 (UNAUTHORIZED).
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<MessageResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.error("Invalid credentials: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.error(ex.getMessage()));
    }
    
    /**
     * Xử lý BadCredentialsException của Spring Security.
     * - Trả về HTTP 401 (UNAUTHORIZED).
     * - Thông báo tiếng Việt cố định: "Email hoặc mật khẩu không đúng".
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MessageResponse> handleBadCredentials(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse.error("Email hoặc mật khẩu không đúng"));
    }
    
    /**
     * Xử lý UsernameNotFoundException của Spring Security.
     * - Trả về HTTP 404 (NOT_FOUND).
     * - Thông báo tiếng Việt: "Không tìm thấy người dùng".
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<MessageResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.error("Username not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(MessageResponse.error("Không tìm thấy người dùng"));
    }
    
    /**
     * Xử lý lỗi validate khi @Valid thất bại trên request body (MethodArgumentNotValidException).
     * - Duyệt qua tất cả FieldError để gom {fieldName -> errorMessage}.
     * - Log chi tiết các lỗi và trả về HTTP 400 (BAD_REQUEST).
     * - MessageResponse chứa: success=false, message="Validation failed", data=map lỗi.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("Validation failed: {}", errors);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(false, "Validation failed", errors));
    }
    
    /**
     * Xử lý các lỗi xác thực tùy biến chung (AuthException).
     * - Trả về HTTP 400 (BAD_REQUEST).
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<MessageResponse> handleAuthException(AuthException ex) {
        log.error("Auth exception: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(MessageResponse.error(ex.getMessage()));
    }
    
    /**
     * Xử lý mọi lỗi không bắt riêng (fallback handler).
     * - Log đầy đủ stacktrace với log.error("Unexpected error: ", ex).
     * - Trả về HTTP 500 (INTERNAL_SERVER_ERROR) với thông điệp tiếng Việt thân thiện người dùng.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MessageResponse.error("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."));
    }
}
