package app.admin.repository;

import app.admin.model.ViolationReport;
import app.admin.model.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViolationReportRepository extends JpaRepository<ViolationReport, Long> {

    long countByStatus(ReportStatus status);

    @EntityGraph(attributePaths = {"reporter", "handledBy"})
    Page<ViolationReport> findByStatus(ReportStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"reporter", "handledBy"})
    Page<ViolationReport> findAll(Pageable pageable);
}
