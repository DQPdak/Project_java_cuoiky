package app.ai.service.cv.skill.component.recomment.recommendation.component;

import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.component.recomment.recommendation.dto.SkillRecommendation;

import java.util.ArrayList;

/**
 * CLASS: GapSkillMapper
 * CHỨC NĂNG: Khởi tạo Object SkillRecommendation từ tên kỹ năng.
 */
@Component
public class GapSkillMapper {

    public SkillRecommendation map(String skillName) {
        SkillRecommendation recommendation = new SkillRecommendation();
        recommendation.setSkillName(skillName);
        recommendation.setPriority(7); // Priority mặc định
        recommendation.setReason("Yêu cầu bởi vị trí ứng tuyển");
        recommendation.setRelatedTo(new ArrayList<>());
        return recommendation;
    }
}
