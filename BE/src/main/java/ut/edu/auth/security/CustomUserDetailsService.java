
package ut.edu.auth.security;

// Lombok: @RequiredArgsConstructor tự động tạo constructor với các field final (userRepository)
import lombok.RequiredArgsConstructor;

// Spring Security: các interface và class phục vụ xác thực người dùng
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Đại diện cho quyền (authority) đơn giản theo chuỗi
import org.springframework.security.core.userdetails.UserDetails;         // Interface mô tả thông tin người dùng cho Spring Security
import org.springframework.security.core.userdetails.UserDetailsService; // Interface để tải thông tin người dùng theo username
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Ngoại lệ khi không tìm thấy người dùng

// Spring: @Service đánh dấu class là bean tầng service
import org.springframework.stereotype.Service;

// Entity và enum của ứng dụng
import ut.edu.auth.entity.User;
import ut.edu.auth.entity.enums.UserStatus;
import ut.edu.auth.repository.UserRepository;

import java.util.Collections;

/**
 * CustomUserDetailsService: Triển khai UserDetailsService để cung cấp thông tin người dùng cho Spring Security.
 * - @Service: đăng ký bean trong Spring context.
 * - @RequiredArgsConstructor: Lombok tạo constructor nhận các field final (userRepository).
 * 
 * Nhiệm vụ:
 * - Tải người dùng từ DB theo email (username).
 * - Chuyển đổi User (entity) sang UserDetails (đối tượng mà Spring Security sử dụng).
 * - Ánh xạ vai trò (UserRole) thành GrantedAuthority theo chuẩn "ROLE_<ROLE_NAME>".
 * - Thiết lập trạng thái tài khoản (locked/disabled) dựa vào UserStatus.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    // Repository thao tác DB cho entity User (được inject qua constructor do Lombok sinh)
    private final UserRepository userRepository;
    
    /**
     * loadUserByUsername: Tải thông tin người dùng theo email (được dùng như username).
     * - Nếu không tìm thấy, ném UsernameNotFoundException với thông báo tiếng Việt.
     * - Nếu tìm thấy, xây dựng đối tượng UserDetails của Spring Security:
     *   + username: dùng email.
     *   + password: mật khẩu (đã hash) từ DB.
     *   + authorities: danh sách quyền, ánh xạ từ vai trò người dùng (ROLE_<USER_ROLE_NAME>).
     *   + accountExpired: cố định false (không kiểm tra hết hạn tài khoản).
     *   + accountLocked: true nếu status == BANNED (bị cấm).
     *   + credentialsExpired: cố định false (không kiểm tra hết hạn mật khẩu).
     *   + disabled: true nếu status != ACTIVE (không hoạt động).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email)
                );
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name())
                ))
                .accountExpired(false)
                .accountLocked(user.getStatus() == UserStatus.BANNED)
                .credentialsExpired(false)
                .disabled(user.getStatus() != UserStatus.ACTIVE)
                .build();
    }
    
    /**
     * loadUserById: Tải thông tin người dùng theo id.
     * - Tìm user theo id; nếu không có, ném UsernameNotFoundException.
     * - Sau đó tái sử dụng logic loadUserByUsername để tạo UserDetails thống nhất.
     */
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new UsernameNotFoundException("Không tìm thấy người dùng với id: " + userId)
                );
        
        return loadUserByUsername(user.getEmail());
    }
}