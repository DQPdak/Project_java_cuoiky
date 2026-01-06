package app.ai.service.cv.skill.matching;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import app.ai.service.cv.skill.extractorskill.component.SkillFormatter;
import app.ai.service.cv.skill.matching.dto.SkillMatchResult;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * CHỨC NĂNG: So sánh Kỹ năng trích xuất từ CV với Yêu cầu công việc
 * - Xác định kỹ năng chung giữa CV và job
 * - Xác định kỹ năng thiếu trong CV so với yêu cầu job
 * - Xác định kỹ năng thừa trong CV không cần thiết cho job
 * - Tính toán điểm số phù hợp kỹ năng giữa CV và job
 */

@Service
public class SkillMatcher {
    @Autowired
    private SkillFormatter skillFormatter;
    // CHỨC NĂNG:So sánh kỹ năng trích xuất từ CV với yêu cầu công việc
    public SkillMatchResult match (List<String> candidateSkills, List<String> jobSkills) {
        // Chuẩn hóa tất cả kỹ năng
        Set<String> candidateSet = candidateSkills.stream()
            .map(skillFormatter::normalize)
            .collect(Collectors.toSet());

        Set<String> jobSet = jobSkills.stream()
            .map(skillFormatter::normalize)
            .collect(Collectors.toSet());
        
        // Tìm kỹ năng chung giữa ứng viên và yêu cầu công việc
        Set<String> matching = jobSet.stream().filter(candidateSet::contains).collect(Collectors.toSet());

        // Tìm kỹ năng job có nhưng cv thiếu
        Set<String> missing = jobSet.stream().filter(s -> !candidateSet.contains(s) ).collect(Collectors.toSet());

        // Kỹ năng cv có mà job không có
        Set<String> extra = candidateSet.stream().filter(s -> !jobSet.contains(s) ).collect(Collectors.toSet());

        // Trả về kết quả so sánh
        SkillMatchResult result = new SkillMatchResult();
        result.setMatchedSkills(matching);
        result.setMissingSkills(missing);
        result.setExtraSkills(extra);
        result.setTotalRequired(jobSkills.size());
        result.setMatchCount(matching.size());

        return result;
    }
}
