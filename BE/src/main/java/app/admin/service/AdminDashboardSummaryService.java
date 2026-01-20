package app.admin.service;

import app.admin.dto.response.DashboardSummaryResponse;
import app.admin.dto.response.DashboardSummaryProjection;
import app.admin.repository.AdminDashboardSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardSummaryService {

    private final AdminDashboardSummaryRepository adminDashboardRepository;

    public DashboardSummaryResponse getSummary() {
        DashboardSummaryProjection p = adminDashboardRepository.getDashboardSummary();

        return DashboardSummaryResponse.builder()
                .totalCandidates(p.getCandidateTotal())
                .totalRecruiters(p.getRecruiterTotal())
                .totalActiveJobs(p.getJobTotal())
                .totalApplications(p.getApplicationTotal())
                .build();
    }
}