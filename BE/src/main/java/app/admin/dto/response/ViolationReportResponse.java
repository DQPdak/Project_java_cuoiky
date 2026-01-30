package app.admin.dto.response;

import app.admin.model.enums.ReportStatus;
import app.admin.model.enums.ReportTargetType;
import app.admin.model.enums.ViolationReason;

import java.time.OffsetDateTime;

public record ViolationReportResponse(
        Long id,
        Long reporterId,
        String reporterName,

        ReportTargetType targetType,
        Long targetId,

        ViolationReason reason,
        String description,
        String evidenceUrl,

        ReportStatus status,

        Long handledById,
        String handledByName,
        String adminNote,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime handledAt
) {}
