package app.candidate.service;

import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.recruitment.entity.JobPosting;
import app.recruitment.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRecommendationService {

    private final CandidateProfileRepository profileRepository;
    private final JobPostingRepository jobRepository;

    public List<Map<String, Object>> getRecommendedJobs(Long userId) {
        // 1. Tìm hồ sơ, nếu chưa có thì trả về danh sách rỗng (KHÔNG NÉM LỖI 500 NỮA)
        Optional<CandidateProfile> profileOpt = profileRepository.findByUserId(userId);

        if (profileOpt.isEmpty()) {
            log.info("User {} chưa có hồ sơ CandidateProfile. Trả về list rỗng.", userId);
            return Collections.emptyList();
        }

        CandidateProfile profile = profileOpt.get();
        List<String> candidateSkills = profile.getSkills();

        // Nếu hồ sơ có nhưng chưa có kỹ năng (null hoặc rỗng)
        if (candidateSkills == null || candidateSkills.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Lấy tất cả công việc
        List<JobPosting> allJobs = jobRepository.findAll();
        List<Map<String, Object>> recommendedJobs = new ArrayList<>();

        for (JobPosting job : allJobs) {
            // Tính toán điểm số (thêm try-catch để an toàn tuyệt đối)
            try {
                double score = calculateMatchScore(candidateSkills, job.getDescription());

                if (score > 0) {
                    Map<String, Object> jobMap = new HashMap<>();
                    jobMap.put("id", job.getId());
                    jobMap.put("title", job.getTitle() != null ? job.getTitle() : "Không tiêu đề");
                    jobMap.put("company", "Tech Company");
                    jobMap.put("location", job.getLocation() != null ? job.getLocation() : "Remote");
                    jobMap.put("salary", job.getSalaryRange() != null ? job.getSalaryRange() : "Thỏa thuận");
                    jobMap.put("matchScore", Math.round(score));
                    jobMap.put("skillsFound", findMatchingSkills(candidateSkills, job.getDescription()));

                    recommendedJobs.add(jobMap);
                }
            } catch (Exception e) {
                log.warn("Lỗi khi tính toán job ID {}: {}", job.getId(), e.getMessage());
            }
        }

        // 3. Sắp xếp điểm giảm dần
        recommendedJobs.sort((j1, j2) -> {
            Long s1 = (Long) j1.get("matchScore");
            Long s2 = (Long) j2.get("matchScore");
            return s2.compareTo(s1);
        });

        return recommendedJobs;
    }

    private double calculateMatchScore(List<String> candidateSkills, String jobDescription) {
        if (jobDescription == null || candidateSkills == null) return 0;

        String lowerDesc = jobDescription.toLowerCase();
        long matchCount = candidateSkills.stream()
                .filter(skill -> skill != null && lowerDesc.contains(skill.toLowerCase()))
                .count();

        if (candidateSkills.isEmpty()) return 0;
        return ((double) matchCount / candidateSkills.size()) * 100;
    }

    private List<String> findMatchingSkills(List<String> candidateSkills, String jobDescription) {
        if (jobDescription == null || candidateSkills == null) return Collections.emptyList();

        String lowerDesc = jobDescription.toLowerCase();
        return candidateSkills.stream()
                .filter(skill -> skill != null && lowerDesc.contains(skill.toLowerCase()))
                .collect(Collectors.toList());
    }
}