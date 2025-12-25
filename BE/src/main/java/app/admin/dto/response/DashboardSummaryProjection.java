package app.admin.dto.response;

public interface DashboardSummaryProjection {
    Long getTotalUsers();
    Long getTotalJobs();
    Long getTotalApplications();
    Long getTotalArticles();
}
