package app.ai.service.cv.skill.component.recomment.recommendation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.ai.service.cv.skill.component.recomment.recommendation.component.SkillFrequency;
import app.ai.service.cv.skill.component.recomment.recommendation.component.SkillFrequencyTracker;
import app.ai.service.cv.skill.component.recomment.recommendation.component.SkillRecommendationSorter;
import app.ai.service.cv.skill.component.recomment.recommendation.component.buildRecommendation;
import app.ai.service.cv.skill.component.recomment.recommendation.dto.SkillRecommendation;

// CHỨC NĂNG: gợi ý các kỹ năng còn thiếu dựa trên các kỹ năng đã có những gợi ý này nằm ngoài yêu cầu công việc 
@Service
public class RelatedSkillRecommender {
   @Autowired private SkillFrequencyTracker frequencyTracker;
    @Autowired private SkillRecommendationSorter sorter;
    @Autowired private buildRecommendation builder;

    private static final int DEFAULT_LIMIT = 5;

    public List<SkillRecommendation> recommend(List<String> currentSkills, int limit) {
        if (currentSkills == null || currentSkills.isEmpty()) return new ArrayList<>();

        // 1. Chuẩn hóa dữ liệu đầu vào
        Set<String> currentSkillsLower = currentSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // 2. Logic tìm kiếm và tính tần suất
        Map<String, SkillFrequency> frequencyMap = frequencyTracker.calculateFrequency(currentSkills, currentSkillsLower);

        // 3. Logic chuyển đổi sang Object Recommendation thông qua Builder
        List<SkillRecommendation> recommendations = frequencyMap.entrySet().stream()
                .map(entry -> builder.build(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 4. Logic sắp xếp và giới hạn
        return sorter.sortAndLimit(recommendations, limit);
    }

    public List<SkillRecommendation> recommend(List<String> currentSkills) {
        return recommend(currentSkills, DEFAULT_LIMIT);
    }
}
