package app.ai.service.cv.skill.recommendation.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.extractorskill.model.Skill;
import app.ai.service.cv.skill.skillrelation.component.SkillRelationshipComponent;

import java.util.*;

/**
 * CHỨC NĂNG: Thu thập và thống kê tần suất các kỹ năng liên quan.
 */
@Component
public class SkillFrequencyTracker {

    @Autowired
    private SkillRelationshipComponent relationshipRepo;

    public Map<String, SkillFrequency> calculateFrequency(List<String> currentSkills, Set<String> currentSkillsLower) {
        Map<String, SkillFrequency> skillFrequencyMap = new HashMap<>();

        for (String skillName : currentSkills) {
            List<Skill> relatedSkills = relationshipRepo.getRelatedSkills(skillName);

            for (Skill relatedSkill : relatedSkills) {
                String relatedLower = relatedSkill.getName().toLowerCase();

                // Bỏ qua nếu user đã có skill này
                if (currentSkillsLower.contains(relatedLower)) {
                    continue;
                }

                skillFrequencyMap.computeIfAbsent(relatedSkill.getName(), k -> new SkillFrequency())
                        .increment(skillName);
            }
        }
        return skillFrequencyMap;
    }
}
