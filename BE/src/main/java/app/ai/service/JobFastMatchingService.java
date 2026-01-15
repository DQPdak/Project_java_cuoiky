package app.ai.service;

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
public class JobFastMatchingService {

    private final JobPostingRepository jobPostingRepository;

    /**
     * TÍNH ĐIỂM NHANH (Jaccard)
     * Chạy trên RAM, không tốn tiền AI.
     */
    public Map<Long, Integer> calculateBatchCompatibility(List<String> candidateSkills, List<Long> jobIds) {
       Map<Long, Integer> scores = new HashMap<>();
        
        // Nếu ứng viên chưa có skill nào -> 0 điểm hết
        if (candidateSkills == null || candidateSkills.isEmpty()) {
            jobIds.forEach(id -> scores.put(id, 0));
            return scores;
        }

        // 1. Chuẩn hóa skill ứng viên (Lower case + Trim)
        Set<String> normalizedCandidateSkills = candidateSkills.stream()
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toSet());

        // 2. [QUAN TRỌNG] Gọi hàm query tối ưu (JOIN FETCH) lấy Job + Skill 1 lần
        List<JobPosting> jobs = jobPostingRepository.findAllByIdsWhithSkills(jobIds);

        for (JobPosting job : jobs) {
            // Lấy list skill đã được AI tách sẵn trong DB
            List<String> jobSkills = job.getExtractedSkills();
            
            // Tính điểm
            int score = calculateScore(normalizedCandidateSkills, jobSkills);
            scores.put(job.getId(), score);
        }
        return scores;
    }

    private int calculateScore(Set<String> candidateSkills, List<String> jobSkillsRaw) {
        // Nếu Job không yêu cầu skill gì cụ thể -> Cho điểm trung bình hoặc 0
        if (jobSkillsRaw == null || jobSkillsRaw.isEmpty()) return 0;

        // Chuẩn hóa skill job
        Set<String> jobSkills = jobSkillsRaw.stream()
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toSet());

        if (jobSkills.isEmpty()) return 0;

        // Tìm điểm giao (Intersection - Kỹ năng trùng khớp)
        Set<String> match = new HashSet<>(candidateSkills);
        match.retainAll(jobSkills);

        // Công thức: (Số skill trùng / Tổng skill Job yêu cầu) * 100
        return (int) Math.min(Math.round(((double) match.size() / jobSkills.size()) * 100), 100);
    }

}