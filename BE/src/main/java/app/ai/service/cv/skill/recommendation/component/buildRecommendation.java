package app.ai.service.cv.skill.recommendation.component;

import app.ai.service.cv.skill.recommendation.dto.SkillRecommendation;
import app.ai.service.cv.skill.recommendation.util.PriorityCalculator;
import app.ai.service.cv.skill.recommendation.util.SkillFrequency;
import app.ai.service.cv.skill.recommendation.util.generateReason;

public class buildRecommendation {
    private final PriorityCalculator pri = new PriorityCalculator();
    private final generateReason rea = new generateReason();
    public SkillRecommendation build(String skillName, SkillFrequency frequency) {
        SkillRecommendation recommendation = new SkillRecommendation();
        recommendation.setSkillName(skillName);
        recommendation.setPriority(pri.calculate(frequency));
        recommendation.setReason(rea.generate(frequency));
        recommendation.setRelatedTo(frequency.getRelatedSkills());
        return recommendation;
    }
}
