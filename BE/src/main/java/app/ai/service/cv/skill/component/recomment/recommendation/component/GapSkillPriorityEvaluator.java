package app.ai.service.cv.skill.component.recomment.recommendation.component;

import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.component.recomment.recommendation.dto.SkillRecommendation;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * CLASS: GapSkillPriorityEvaluator
 * CHỨC NĂNG: Đánh giá độ quan trọng của từng kỹ năng thiếu.
 */
@Component
public class GapSkillPriorityEvaluator {

    private final Set<String> CORE_SKILLS = new HashSet<>(Arrays.asList(
        "java", "python", "javascript", "typescript", "c++", "go"
    ));

    public void evaluate(SkillRecommendation rec, List<String> criticalSkills) {
        String skillLower = rec.getSkillName().toLowerCase();

        // Ưu tiên 1: Nếu nằm trong danh sách bắt buộc (Critical)
        if (criticalSkills != null && criticalSkills.stream().anyMatch(s -> s.equalsIgnoreCase(skillLower))) {
            rec.setPriority(10);
            rec.setReason("Kỹ năng BẮT BUỘC cho vị trí này");
        } 
        // Ưu tiên 2: Nếu là ngôn ngữ lập trình cốt lõi
        else if (CORE_SKILLS.contains(skillLower)) {
            rec.setPriority(10);
            rec.setReason("Ngôn ngữ lập trình cốt lõi");
        }
    }
}
