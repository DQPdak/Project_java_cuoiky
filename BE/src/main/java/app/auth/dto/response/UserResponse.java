
package app.auth.dto.response;

// Lombok: cung cấp các annotation để tự động sinh code
import lombok.AllArgsConstructor;  // Tạo constructor với tất cả các tham số
import lombok.Builder;             // Cho phép sử dụng Builder pattern để khởi tạo đối tượng
import lombok.Data;                // Tự động sinh getter, setter, toString, equals, hashCode
import lombok.NoArgsConstructor;   // Tạo constructor không tham số

// Import enum UserRole và UserStatus: định nghĩa vai trò và trạng thái người dùng
import app.auth.entity.enums.UserRole;
import app.auth.entity.enums.UserStatus;

// Import LocalDateTime: kiểu dữ liệu thời gian trong Java (ngày + giờ)
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dùng để trả về thông tin người dùng cho client.
 * - @Data: Lombok tạo getter/setter, equals, hashCode, toString.
 * - @Builder: Cho phép khởi tạo đối tượng bằng cú pháp builder.
 * - @NoArgsConstructor: Tạo constructor rỗng.
 * - @AllArgsConstructor: Tạo constructor với tất cả các field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    /**
     * id: Định danh duy nhất của người dùng trong hệ thống (primary key).
     */
    private Long id;
    
    /**
     * fullName: Họ và tên của người dùng.
     */
    private String fullName;
    
    /**
     * email: Địa chỉ email của người dùng (dùng để đăng nhập và liên lạc).
     */
    private String email;
    
    /**
     * userRole: Vai trò của người dùng (ví dụ: ADMIN, STUDENT, TEACHER).
     */
    private UserRole userRole;
    
    /**
     * status: Trạng thái tài khoản (ví dụ: ACTIVE, INACTIVE, BLOCKED).
     */
    private UserStatus status;
    
    /**
     * profileImageUrl: Đường dẫn ảnh đại diện của người dùng.
     */
    private String profileImageUrl;
    
    /**
     * isEmailVerified: Cho biết email đã được xác thực hay chưa.
     */
    private Boolean isEmailVerified;
    
    /**
     * createdAt: Thời điểm tạo tài khoản.
     */
    private LocalDateTime createdAt;
    
    /**
     * lastLoginAt: Thời điểm người dùng đăng nhập lần cuối.
     */
    private LocalDateTime lastLoginAt;
}