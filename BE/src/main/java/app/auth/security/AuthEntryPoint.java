
package app.auth.security;

// Jackson: ObjectMapper dùng để chuyển đổi đối tượng Java thành JSON
import com.fasterxml.jackson.databind.ObjectMapper;

// Servlet API: cung cấp HttpServletRequest và HttpServletResponse để xử lý request/response
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Lombok: @Slf4j tự động tạo logger (log.info, log.error...)
import lombok.extern.slf4j.Slf4j;

// Spring Security: AuthenticationEntryPoint là interface để xử lý lỗi xác thực (401 Unauthorized)
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

// Spring: @Component đánh dấu class là bean để Spring quản lý
import org.springframework.stereotype.Component;

// DTO phản hồi chuẩn cho API
import app.auth.dto.response.MessageResponse;

import java.io.IOException;

/**
 * AuthEntryPoint: Xử lý khi người dùng chưa xác thực mà truy cập vào tài nguyên yêu cầu bảo mật.
 * - @Component: đăng ký bean trong Spring context.
 * - @Slf4j: cung cấp logger để ghi log.
 * - Implements AuthenticationEntryPoint: bắt buộc override phương thức commence().
 */
@Component
@Slf4j
public class AuthEntryPoint implements AuthenticationEntryPoint {
    
    /**
     * commence(): Được gọi khi xảy ra lỗi xác thực (401 Unauthorized).
     * @param request HttpServletRequest: request từ client.
     * @param response HttpServletResponse: response trả về cho client.
     * @param authException AuthenticationException: thông tin lỗi xác thực.
     * @throws IOException nếu có lỗi khi ghi dữ liệu vào response.
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        
        // Ghi log lỗi xác thực
        log.error("Unauthorized error: {}", authException.getMessage());
        
        // Thiết lập kiểu dữ liệu trả về là JSON và mã hóa UTF-8
        response.setContentType("application/json;charset=UTF-8");
        
        // Thiết lập mã trạng thái HTTP là 401 (Unauthorized)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
               // Tạo đối tượng phản hồi lỗi với thông báo chi tiết
        MessageResponse errorResponse = MessageResponse.error(
            "Unauthorized: " + authException.getMessage()
        );
        
        // Dùng ObjectMapper để chuyển đối tượng Java thành chuỗi JSON
        ObjectMapper mapper = new ObjectMapper();
        
        // Ghi JSON vào body của response
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}