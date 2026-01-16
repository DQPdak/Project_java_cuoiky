package app.candidate.service;

import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.recruitment.entity.JobPosting;
import app.recruitment.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;            // Import Logger thủ công
import org.slf4j.LoggerFactory;     // Import LoggerFactory
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobRecommendationService {

    // 1. Khai báo Logger thủ công để sửa lỗi "cannot find symbol log"
    private static final Logger log = LoggerFactory.getLogger(JobRecommendationService.class);

    private final CandidateProfileRepository profileRepository;
    private final JobPostingRepository jobRepository;

    public List<Map<String, Object>> getRecommendedJobs(Long userId) {
        // Tìm hồ sơ ứng viên
        Optional<CandidateProfile> profileOpt = profileRepository.findByUserId(userId);

        if (profileOpt.isEmpty()) {
            log.info("User {} chưa có hồ sơ CandidateProfile. Trả về list rỗng.", userId);
            return Collections.emptyList();
        }

        CandidateProfile profile = profileOpt.get();
        List<String> candidateSkills = profile.getSkills();

        // Nếu chưa có kỹ năng thì không gợi ý được
        if (candidateSkills == null || candidateSkills.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Lấy tất cả công việc
        List<JobPosting> allJobs = jobRepository.findAll();
        List<Map<String, Object>> recommendedJobs = new ArrayList<>();

        for (JobPosting job : allJobs) {
            // Tính toán điểm số (thêm try-catch để an toàn tuyệt đối)
            try {
                // Kết hợp cả Mô tả và Yêu cầu để so khớp
                String description = job.getDescription() != null ? job.getDescription() : "";
                String requirements = job.getRequirements() != null ? job.getRequirements() : "";
                String jobContent = description + " " + requirements;

                double score = calculateMatchScore(candidateSkills, jobContent);

                if (score > 0) {
                    Map<String, Object> jobMap = new HashMap<>();
                    jobMap.put("id", job.getId());
                    jobMap.put("title", job.getTitle() != null ? job.getTitle() : "Chưa có tiêu đề");

                    // 2. Sửa lỗi "cannot find symbol getCompany": Chỉ lấy từ job.getCompany()
                    String companyName = "Công ty ẩn danh";
                    if (job.getCompany() != null) {
                        companyName = job.getCompany().getName();
                    }
                    // Đã xóa đoạn job.getRecruiter().getCompany() gây lỗi
                    jobMap.put("company", companyName);

                    jobMap.put("location", job.getLocation() != null ? job.getLocation() : "Remote");
                    jobMap.put("salary", job.getSalaryRange() != null ? job.getSalaryRange() : "Thỏa thuận");
                    jobMap.put("matchScore", Math.round(score));
                    jobMap.put("description", job.getDescription());
                    jobMap.put("requirements", job.getRequirements());

                    jobMap.put("skillsFound", findMatchingSkills(candidateSkills, jobContent));

                    recommendedJobs.add(jobMap);
                }
            } catch (Exception e) {
                log.warn("Lỗi khi tính toán job ID {}: {}", job.getId(), e.getMessage());
            }
        }

        // Sắp xếp theo điểm giảm dần
        recommendedJobs.sort((j1, j2) -> {
            Long s1 = (Long) j1.get("matchScore");
            Long s2 = (Long) j2.get("matchScore");
            return s2.compareTo(s1);
        });

        // Lấy Top 10
        if (recommendedJobs.size() > 10) {
            return recommendedJobs.subList(0, 10);
        }

        return recommendedJobs;
    }

    private double calculateMatchScore(List<String> candidateSkills, String jobContent) {
        if (jobContent == null || candidateSkills == null) return 0;
        String lowerContent = jobContent.toLowerCase();

        long matchCount = candidateSkills.stream()
                .filter(skill -> skill != null && lowerContent.contains(skill.toLowerCase()))
                .count();

        if (candidateSkills.isEmpty()) return 0;
        return ((double) matchCount / candidateSkills.size()) * 100;
    }

    private List<String> findMatchingSkills(List<String> candidateSkills, String jobContent) {
        if (jobContent == null || candidateSkills == null) return Collections.emptyList();
        String lowerContent = jobContent.toLowerCase();

        return candidateSkills.stream()
                .filter(skill -> skill != null && lowerContent.contains(skill.toLowerCase()))
                .collect(Collectors.toList());
    }
}