package app.recruitment.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;

@Data
public class JobPostingRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String requirements;

    @NotBlank(message = "SalaryRange is required")
    private String salaryRange;

    @NotBlank(message = "Location is required")
    private String location;

    @Future(message = "ExpiryDate must be in the future")
    private LocalDateTime expiryDate;
    
    private String status; // DRAFT | PUBLISHED | CLOSED
}