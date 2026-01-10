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
    private Long studentId;
    private String studentName;
    private String cvUrl;
    private String status; // APPLIED, SCREENING, INTERVIEW, OFFER, REJECTED
    private LocalDateTime appliedAt;
    private String recruiterNote;
}