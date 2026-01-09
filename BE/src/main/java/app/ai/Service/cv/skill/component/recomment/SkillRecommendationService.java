package app.ai.service.cv.skill.component.recomment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.ai.service.cv.skill.component.recomment.component.ComprehensiveAdvisor;
import app.ai.service.cv.skill.component.recomment.dto.ComprehensiveRecommendation;
import app.ai.service.cv.skill.component.recomment.dto.SkillAdviceRequest;

@Service
public class SkillRecommendationService {
    @Autowired 
    private ComprehensiveAdvisor advisor;

    /**
     * Điểm truy cập duy nhất để lấy toàn bộ tư vấn kỹ năng.
     */
    public ComprehensiveRecommendation suggestComprehensive(SkillAdviceRequest request) {
        return advisor.advise(request);
    }
}
