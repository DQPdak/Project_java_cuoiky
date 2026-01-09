package app.ai.service.cv.skill.recommendation.component;

import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.recommendation.dto.SkillRecommendation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CHỨC NĂNG: Sắp xếp và cắt tỉa danh sách kết quả.
 */
@Component
public class SkillRecommendationSorter {

    public List<SkillRecommendation> sortAndLimit(List<SkillRecommendation> recommendations, int limit) {
        return recommendations.stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}