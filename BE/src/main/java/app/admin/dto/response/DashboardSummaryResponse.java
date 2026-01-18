package app.admin.dto.response;

import lombok.*;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class DashboardSummaryResponse {
    private long totalUsers;
    private long totalJobs;
    private long totalApplications;
    private long totalArticles;
}
