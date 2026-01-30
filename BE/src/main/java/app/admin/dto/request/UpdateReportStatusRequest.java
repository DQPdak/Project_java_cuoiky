package app.admin.dto.request;

import app.admin.model.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateReportStatusRequest(
        @NotNull ReportStatus status,
        @Size(max = 2000) String adminNote
) {}
