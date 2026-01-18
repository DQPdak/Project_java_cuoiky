package app.admin.dto.response;

public interface DashboardSummaryProjection {
    Long getCandidateTotal();
    Long getRecruiterTotal();
    Long getJobPostTotal();
    Long getJobPostPublished();
    Long getApplicationTotal();
}
