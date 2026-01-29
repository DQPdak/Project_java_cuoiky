// RejectJobPostingRequest
package app.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectJobPostingRequest(@NotBlank String reason) {}
