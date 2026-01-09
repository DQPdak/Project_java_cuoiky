package app.ai.service.cv.skill.component.recomment.recommendation.component;

import app.ai.service.cv.skill.component.recomment.recommendation.dto.SkillRecommendation;

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
