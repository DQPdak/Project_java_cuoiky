
package app.auth.controller;

// Thư viện validation của Jakarta để dùng @Valid kiểm tra dữ liệu request
import jakarta.validation.Valid;

// Lombok: tạo constructor với các field final (@RequiredArgsConstructor)
// và cung cấp logger thông qua @Slf4j (không cần khai báo thủ công)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Spring: ResponseEntity để trả về HTTP response (status + body)
import org.springframework.http.ResponseEntity;
// Spring Web: các annotation xây dựng REST API (@RestController, @RequestMapping, @GetMapping, @PutMapping, @DeleteMapping, @RequestParam, @RequestBody)
import org.springframework.web.bind.annotation.*;

// Các DTO (Data Transfer Object) để nhận/trả dữ liệu
import app.auth.dto.request.ChangePasswordRequest;
import app.auth.dto.response.MessageResponse;
import app.auth.dto.response.UserResponse;

// Service xử lý nghiệp vụ người dùng
import app.auth.service.UserService;

/**
 * Controller REST quản lý người dùng.
 * - @RestController: đánh dấu lớp này là REST controller, các method trả về dữ liệu (JSON).
 * - @RequestMapping("/api/users"): prefix cho tất cả endpoint trong controller.
 * - @RequiredArgsConstructor: Lombok tạo constructor nhận các field final (userService).
 * - @Slf4j: Lombok tạo logger 'log' để ghi log.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    // Dependency injection: service xử lý nghiệp vụ (được inject qua constructor do Lombok sinh ra)
    private final UserService userService;
    
    /**
     * [GET] /api/users/me
     * Mục đích: Lấy thông tin người dùng hiện tại (đang đăng nhập).
     * - log.info: ghi log cho request.
     * - userService.getCurrentUser(): gọi service lấy dữ liệu người dùng.
     * - Trả về ResponseEntity với MessageResponse.success: thông báo + payload (UserResponse).
     */
    @GetMapping("/me")
    public ResponseEntity<MessageResponse> getCurrentUser() {
        log.info("Get current user request");
        
        UserResponse user = userService.getCurrentUser();
        
        return ResponseEntity.ok(
                MessageResponse.success("Lấy thông tin người dùng thành công", user)
        );
    }
    
    /**
     * [PUT] /api/users/me
     * Mục đích: Cập nhật hồ sơ người dùng (họ tên, ảnh đại diện).
     * - @RequestParam(required = false): tham số query có thể bỏ trống.
     * - log.info: ghi log cho request.
     * - userService.updateProfile(fullName, profileImageUrl): xử lý cập nhật thông tin.
     * - Trả về ResponseEntity với MessageResponse.success: thông báo + payload (UserResponse).
     */
    @PutMapping("/me")
    public ResponseEntity<MessageResponse> updateProfile(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String profileImageUrl
    ) {
        log.info("Update profile request");
        
        UserResponse user = userService.updateProfile(fullName, profileImageUrl);
        
        return ResponseEntity.ok(
                MessageResponse.success("Cập nhật thông tin thành công", user)
        );
    }
    
    /**
     * [PUT] /api/users/change-password
     * Mục đích: Đổi mật khẩu.
     * - @RequestBody: nhận dữ liệu JSON từ body request.
     * - @Valid: kích hoạt validation theo các ràng buộc trong ChangePasswordRequest.
     * - log.info: ghi log cho request.
     * - userService.changePassword(request): gọi service xử lý đổi mật khẩu.
     * - Trả về ResponseEntity với MessageResponse.success: thông báo thành công (không kèm payload).
     */
    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password request");
        
        userService.changePassword(request);
        
        return ResponseEntity.ok(
                MessageResponse.success("Đổi mật khẩu thành công")
        );
    }
    
    /**
     * [DELETE] /api/users/me
     * Mục đích: Xóa tài khoản của người dùng hiện tại.
     * - log.info: ghi log cho request.
     * - userService.deleteAccount(): xử lý xóa tài khoản tại tầng service (bao gồm nghiệp vụ liên quan).
     * - Trả về ResponseEntity với MessageResponse.success: thông báo xóa thành công.
     */
    @DeleteMapping("/me")
    public ResponseEntity<MessageResponse> deleteAccount() {
        log.info("Delete account request");
        
        userService.deleteAccount();
        
        return ResponseEntity.ok(
                MessageResponse.success("Xóa tài khoản thành công")
        );
    }
}

