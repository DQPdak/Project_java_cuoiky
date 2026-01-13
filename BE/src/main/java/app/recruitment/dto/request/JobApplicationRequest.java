package app.recruitment.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class JobApplicationRequest {
    @NotNull
    private Long jobId;
    
    // Có thể bỏ @NotNull nếu cho phép hệ thống tự lấy từ Profile
    private String cvUrl; 

    // [FIX LỖI 1] Thêm trường này để Service gọi được getCoverLetter()
    private String coverLetter; 
}