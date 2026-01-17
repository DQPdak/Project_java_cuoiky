
package app.auth.security;

// Lombok: Tự động sinh constructor với các field final (jwtAuthFilter, userDetailsService, authEntryPoint)
import lombok.RequiredArgsConstructor;

// Spring Context & Security: Khai báo bean, cấu hình bảo mật
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // Provider dùng UserDetailsService + PasswordEncoder
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Bật phân quyền theo annotation (ví dụ: @PreAuthorize)
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // API cấu hình bảo mật HTTP
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Bật Spring Security
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Dùng để disable CSRF gọn
import org.springframework.security.config.http.SessionCreationPolicy; // Chính sách session (STATELESS cho JWT)
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Mã hóa mật khẩu
import org.springframework.security.crypto.password.PasswordEncoder; // Interface encoder
import org.springframework.security.web.SecurityFilterChain; // Chuỗi filter bảo mật
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Filter xác thực username/password mặc định

// CORS: cấu hình nguồn cho phép cross-origin
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig: Lớp cấu hình bảo mật Spring Security.
 * - @Configuration: đánh dấu là lớp cấu hình Spring.
 * - @EnableWebSecurity: bật các tính năng bảo mật của Spring Security.
 * - @EnableMethodSecurity: cho phép dùng annotation ở mức method (ví dụ: @PreAuthorize("hasRole('ADMIN')")).
 * - @RequiredArgsConstructor: Lombok tạo constructor cho các field final.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    // Filter kiểm tra và thiết lập xác thực dựa trên JWT cho mỗi request
    private final JwtAuthenticationFilter jwtAuthFilter;
    // Service cung cấp UserDetails cho quá trình xác thực
    private final CustomUserDetailsService userDetailsService;
    // Entry point xử lý lỗi 401 (Unauthorized) khi chưa xác thực
    private final AuthEntryPoint authEntryPoint;
    
    /**
     * Định nghĩa chuỗi filter bảo mật (SecurityFilterChain).
     * - Disable CSRF: vì dùng JWT, không cần CSRF token.
     * - CORS: cấu hình nguồn cho phép (corsConfigurationSource()).
     * - Exception handling: dùng AuthEntryPoint cho lỗi 401.
     * - Stateless session: không lưu trạng thái session (phù hợp JWT).
     * - Phân quyền đường dẫn:
     *    + /api/auth/**, /api/public/**, swagger: cho phép truy cập không cần xác thực.
     *    + /api/admin/**: yêu cầu vai trò ADMIN.
     *    + /api/recruiter/**: yêu cầu vai trò RECRUITER.
     *    + Các request còn lại: phải xác thực.
     * - Đăng ký AuthenticationProvider (DAO với UserDetailsService + PasswordEncoder).
     * - Chèn JwtAuthenticationFilter trước UsernamePasswordAuthenticationFilter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì API stateless (JWT)
            .csrf(AbstractHttpConfigurer::disable)
            // Bật CORS với cấu hình nguồn tự định nghĩa
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Cấu hình xử lý ngoại lệ bảo mật (401 Unauthorized)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authEntryPoint)
            )
            // Phiên làm việc stateless: không dùng HttpSession để lưu xác thực
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Phân quyền truy cập theo đường dẫn
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // các API auth: login, register, refresh...
                .requestMatchers("/api/public/**").permitAll() // các API công khai
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // tài liệu API
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // yêu cầu ROLE_ADMIN
                .requestMatchers("/api/recruiter/**").hasRole("RECRUITER") // yêu cầu ROLE_RECRUITER
                .requestMatchers("/api/interview/**").permitAll()
                .requestMatchers("/api/cv/**").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                .anyRequest().authenticated() // còn lại cần xác thực
            )
            // Provider xác thực sử dụng UserDetailsService
            .authenticationProvider(authenticationProvider())
            // Chèn filter JWT trước filter mặc định của Spring (UsernamePasswordAuthenticationFilter)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * authenticationProvider: Sử dụng DaoAuthenticationProvider để:
     * - Lấy thông tin người dùng qua CustomUserDetailsService.
     * - So khớp mật khẩu bằng PasswordEncoder (BCrypt).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // nguồn dữ liệu người dùng
        authProvider.setPasswordEncoder(passwordEncoder());     // encoder BCrypt
        return authProvider;
    }
    
    /**
     * authenticationManager: Lấy AuthenticationManager từ AuthenticationConfiguration
     * để sử dụng trong quy trình xác thực (ví dụ login).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * passwordEncoder: Định nghĩa bộ mã hóa mật khẩu BCrypt.
     * - BCryptPasswordEncoder: an toàn, có salt, chống tấn công brute-force.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * corsConfigurationSource: Cấu hình CORS cho phép frontend từ các origin cụ thể gọi API.
     * - AllowedOrigins: danh sách domain được phép (localhost cho dev).
     * - AllowedMethods: các HTTP method được phép.
     * - AllowedHeaders: cho phép tất cả header (*).
     * - AllowCredentials: cho phép gửi cookie/authorization header.
     * - MaxAge: cache preflight trong 3600 giây.
     * - Đăng ký áp dụng cho tất cả đường dẫn "/**".
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
                       "http://localhost:8081"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // áp dụng cho tất cả endpoint
        return source;
    }
}