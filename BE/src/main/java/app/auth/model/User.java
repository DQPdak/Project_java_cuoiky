
package app.auth.model;

// Jakarta Persistence (JPA): các annotation để ánh xạ entity với bảng trong DB
import jakarta.persistence.*; // @Entity, @Table, @Index, @Id, @GeneratedValue, @Column, @Enumerated, @EntityListeners, EnumType

// Lombok: tự động sinh getter/setter, toString, equals/hashCode, constructor, builder...
import lombok.AllArgsConstructor; // Tạo constructor với tất cả tham số
import lombok.Builder;            // Hỗ trợ Builder pattern           // Sinh getter/setter, equals/hashCode, toString
import lombok.Getter;
import lombok.NoArgsConstructor;  // Tạo constructor không tham số
import lombok.Setter;

// Spring Data JPA Auditing: tự động điền các trường thời gian tạo/sửa khi bật auditing
import org.springframework.data.annotation.CreatedDate;        // Đánh dấu trường sẽ tự động set thời điểm tạo
import org.springframework.data.annotation.LastModifiedDate;   // Đánh dấu trường sẽ tự động set thời điểm cập nhật
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Listener phục vụ auditing

import app.auth.model.enums.AuthProvider;
import app.auth.model.enums.UserRole;
import app.auth.model.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * Entity User: Đại diện cho bảng người dùng trong hệ thống.
 * - @Entity: đánh dấu lớp là JPA entity.
 * - @Table: đặt tên bảng và index để tối ưu truy vấn (email, google_id).
 * - @EntityListeners(AuditingEntityListener.class): bật auditing để tự động set createdAt/updatedAt.
 * - Lombok: @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor giúp đơn giản hóa code boilerplate.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),        // Index trên email để tìm kiếm nhanh
    @Index(name = "idx_google_id", columnList = "google_id") // Index trên google_id phục vụ đăng nhập Google
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * Khóa chính (ID) tự tăng.
     * - @Id: xác định primary key.
     * - @GeneratedValue(strategy = GenerationType.IDENTITY): DB tự tăng giá trị.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Họ tên người dùng.
     * - name = "full_name": tên cột trong DB.
     * - nullable = false: bắt buộc có giá trị.
     * - length = 100: giới hạn độ dài tối đa 100 ký tự.
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    /**
     * Email người dùng.
     * - unique = true: không trùng lặp, dùng để đăng nhập.
     * - nullable = false: bắt buộc có.
     * - length = 100: giới hạn độ dài.
     */
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    /**
     * Mật khẩu đã được hash (không lưu plaintext).
     * - nullable = false: bắt buộc có.
     */
    @Column(nullable = false)
    private String password;
    
    /**
     * Vai trò người dùng (ADMIN, RECRUITER, CANDIDATE...).
     * - @Enumerated(EnumType.STRING): lưu giá trị enum dạng chuỗi (an toàn khi thay đổi thứ tự).
     * - name = "user_role": tên cột.
     * - length = 20: giới hạn độ dài chuỗi.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole userRole;
    
    /**
     * Nhà cung cấp xác thực (LOCAL, GOOGLE).
     * - @Enumerated(EnumType.STRING): lưu dạng chuỗi.
     * - name = "auth_provider": tên cột.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider;
    
    /**
     * ID Google (sub) liên kết với tài khoản nếu đăng nhập bằng Google.
     * - length = 100: giới hạn độ dài.
     */
    @Column(name = "google_id", length = 100)
    private String googleId;
    
    /**
     * URL ảnh đại diện người dùng.
     * - length = 500: giới hạn độ dài, hỗ trợ URL dài.
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    /**
     * Trạng thái tài khoản (ACTIVE/INACTIVE/BANNED/PENDING_VERIFICATION).
     * - @Enumerated(EnumType.STRING): lưu dạng chuỗi.
     * - nullable = false: bắt buộc có.
     * - @Builder.Default: khi khởi tạo bằng builder mà không set, mặc định là PENDING_VERIFICATION.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;
    
    /**
     * Đánh dấu email đã xác thực hay chưa.
     * - nullable = false: bắt buộc có.
     * - @Builder.Default: mặc định false khi tạo mới.
     */
    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;
    
    /**
     * Thời điểm     * Thời điểm tạo bản ghi người dùng.
     * - @CreatedDate: tự động set khi persist lần đầu (cần bật JPA Auditing).
     * - updatable = false: không cho phép chỉnh sửa sau khi set.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Thời điểm cập nhật gần nhất.
     * - @LastModifiedDate: tự động set khi entity được cập nhật (cần bật JPA Auditing).
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Thời điểm đăng nhập gần nhất của người dùng.
     * - Do ứng dụng tự cập nhật (không phải auditing).
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "verification_code")
    private String verificationCode;
}