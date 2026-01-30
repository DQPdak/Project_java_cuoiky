package app.admin.service;

import app.admin.model.enums.ReportStatus;
import app.admin.model.enums.ReportTargetType;
import app.auth.repository.UserRepository;
import app.auth.model.User;
import app.admin.model.ViolationReport;
import app.admin.repository.ViolationReportRepository;
import app.admin.dto.request.CreateViolationReportRequest;
import app.admin.dto.request.UpdateReportStatusRequest;
import app.admin.dto.response.ViolationReportResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ViolationReportService {

    private final ViolationReportRepository reportRepo;
    private final UserRepository userRepo;

    @Transactional
    public ViolationReportResponse create(Long reporterId, CreateViolationReportRequest req) {
        User reporter = userRepo.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found: " + reporterId));

        ViolationReport r = new ViolationReport();
        r.setReporter(reporter);
        r.setTargetType(req.targetType());
        r.setTargetId(req.targetId());
        r.setReason(req.reason());
        r.setDescription(req.description());
        r.setEvidenceUrl(req.evidenceUrl());
        r.setStatus(ReportStatus.PENDING);

        return toResponse(reportRepo.save(r));
    }

    public long countPending() {
        return reportRepo.countByStatus(ReportStatus.PENDING);
    }

    public Page<ViolationReportResponse> list(ReportStatus status, Pageable pageable) {
        if (status == null) {
            return reportRepo.findAll(pageable).map(this::toResponse);
        }
        return reportRepo.findByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional
    public ViolationReportResponse updateStatus(Long reportId, Long adminId, UpdateReportStatusRequest req) {
        ViolationReport r = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + adminId));

        r.setStatus(req.status());
        r.setAdminNote(req.adminNote());
        r.setHandledBy(admin);
        r.setHandledAt(OffsetDateTime.now());

        // (Khuyến khích) Nếu status VALID/RESOLVED thì bạn có thể gọi thêm logic khoá tin / khoá user / ẩn company...
        // handleTargetIfNeeded(r);

        return toResponse(r);
    }

    private ViolationReportResponse toResponse(ViolationReport r) {
        return new ViolationReportResponse(
                r.getId(),
                r.getReporter().getId(),
                safeName(r.getReporter()),

                r.getTargetType(),
                r.getTargetId(),

                r.getReason(),
                r.getDescription(),
                r.getEvidenceUrl(),

                r.getStatus(),

                r.getHandledBy() != null ? r.getHandledBy().getId() : null,
                r.getHandledBy() != null ? safeName(r.getHandledBy()) : null,
                r.getAdminNote(),

                r.getCreatedAt(),
                r.getUpdatedAt(),
                r.getHandledAt()
        );
    }

    private String safeName(User u) {
        // tuỳ schema: fullName/username/email...
        // return u.getFullName();
        return u.getFullName(); // đổi theo model của bạn
    }
}
