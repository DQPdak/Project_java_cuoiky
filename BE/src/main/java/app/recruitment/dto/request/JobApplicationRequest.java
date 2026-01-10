package app.recruitment.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class JobApplicationRequest {
@NotNull
private Long jobId;
@NotNull
private String cvUrl;
}