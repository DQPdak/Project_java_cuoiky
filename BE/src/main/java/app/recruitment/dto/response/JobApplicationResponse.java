package app.recruitment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    
    private Long studentId;     // Hoặc đổi thành candidateId tùy bạn
    private String studentName; // Hoặc đổi thành candidateName
    
    private String companyName;
    private String cvUrl;
    
    private String status; // Kiểu String
    
    private LocalDateTime appliedAt;
    private String recruiterNote;

    // [FIX LỖI 3] Thêm 2 trường này để Builder không báo lỗi
    private Integer matchScore;
    private String aiEvaluation;
}