package app.admin.service;

import app.admin.dto.request.CreateViolationReportRequest;
import app.admin.dto.request.UpdateReportStatusRequest;
import app.admin.dto.response.ViolationReportResponse;
import app.admin.model.ViolationReport;
import app.admin.model.enums.ReportStatus;
import app.admin.model.enums.ReportTargetType;
import app.admin.repository.ViolationReportRepository;
import app.auth.model.User;
import app.auth.model.enums.UserStatus;
import app.auth.repository.UserRepository;
import app.recruitment.entity.JobPosting;
import app.recruitment.entity.enums.JobStatus;
import app.recruitment.repository.JobPostingRepository;

import org.springframework.transaction.annotation.Transactional;
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
    private final JobPostingRepository jobPostingRepo;

    @Transactional
    public ViolationReportResponse create(Long reporterId, CreateViolationReportRequest req) {
        User reporter = userRepo.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found: " + reporterId));

        // ✅ validate target tồn tại
        validateTargetExists(req.targetType(), req.targetId());

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

    @Transactional(readOnly = true)
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

        // ✅ Khi báo cáo đúng → tác động tới đối tượng bị report
        if (req.status() == ReportStatus.VALID || req.status() == ReportStatus.RESOLVED) {
            applyActionIfValid(r);
            handleTargetIfNeeded(r);
        }
        return toResponse(r);
    }

    private void validateTargetExists(ReportTargetType type, Long targetId) {
        switch (type) {
            case JOB_POSTING -> jobPostingRepo.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("JobPosting not found: " + targetId));
            case USER -> userRepo.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + targetId));
            case COMPANY -> {
            }
        }
    }

    private void applyActionIfValid(ViolationReport r) {
        if (r.getTargetType() == ReportTargetType.JOB_POSTING) {
            JobPosting jp = jobPostingRepo.findById(r.getTargetId())
                    .orElseThrow(() -> new RuntimeException("JobPosting not found: " + r.getTargetId()));

        // ✅ dùng enum JobStatus
        // LƯU Ý: bạn phải có JobStatus.BLOCKED (hoặc tên tương đương)
            jp.setStatus(JobStatus.BLOCKED);

            jobPostingRepo.save(jp);
        }
    }

        // Nếu bạn muốn: report USER → khoá user
        // if (r.getTargetType() == ReportTargetType.USER) { ... }

    private void handleTargetIfNeeded(ViolationReport r) {
        if (r.getTargetType() == ReportTargetType.USER) {
            User target = userRepo.findById(r.getTargetId())
                    .orElseThrow(() -> new RuntimeException("Target user not found: " + r.getTargetId()));

            // ✅ khoá user (đổi đúng enum của bạn)
            target.setStatus(UserStatus.BANNED); 
            userRepo.save(target);
        }
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
        return u.getFullName();
    }
}
