package app.ai.service.cv.skill.recommendation.component;
import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.recommendation.dto.SkillRecommendation;

import java.util.List;

/**
 * CLASS: GapSkillSorter
 * CHỨC NĂNG: Sắp xếp danh sách.
 */
@Component
public class GapSkillSorter {

    public void sort(List<SkillRecommendation> recommendations) {
        recommendations.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }
}
