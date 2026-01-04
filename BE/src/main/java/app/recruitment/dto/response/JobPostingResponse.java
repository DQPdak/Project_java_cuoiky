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
public class JobPostingResponse {
private Long id;
private String title;
private String description;
private String requirements;
private String salaryRange;
private String location;
private LocalDateTime expiryDate;
private String status; // DRAFT | PUBLISHED | CLOSED
private Long recruiterId;
private String recruiterName;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
}