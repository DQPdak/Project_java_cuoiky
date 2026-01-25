package app.recruitment.service;

import app.recruitment.dto.response.RecruiterDashboardResponse;
import app.recruitment.entity.enums.JobStatus;
import app.recruitment.repository.JobApplicationRepository;
import app.recruitment.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecruiterDashboardService {

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Transactional(readOnly = true)
    public RecruiterDashboardResponse getDashboardStats(Long recruiterId) {
        // 1. Đếm số tin đang tuyển (PUBLISHED)
        long activeJobs = jobPostingRepository.countByRecruiterIdAndStatus(recruiterId, JobStatus.PUBLISHED);

        // 2. Tổng số ứng viên
        long totalCandidates = jobApplicationRepository.countByJobPostingRecruiterId(recruiterId);

        // 3. Ứng viên mới trong ngày (từ 00:00 hôm nay)
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long newToday = jobApplicationRepository.countByJobPostingRecruiterIdAndAppliedAtAfter(recruiterId, startOfDay);

        // 4. Pipeline stats
        List<Object[]> statusCounts = jobApplicationRepository.countApplicationsByStatus(recruiterId);
        Map<String, Long> pipelineMap = new HashMap<>();
        
        // Convert List<Object[]> sang Map<String, Long>
        for (Object[] row : statusCounts) {
            String status = row[0].toString(); // Enum name
            Long count = (Long) row[1];
            pipelineMap.put(status, count);
        }

        return RecruiterDashboardResponse.builder()
                .totalActiveJobs(activeJobs)
                .totalCandidates(totalCandidates)
                .newCandidatesToday(newToday)
                .pipelineStats(pipelineMap)
                .build();
    }
}