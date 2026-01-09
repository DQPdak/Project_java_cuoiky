package app.ai.service.cv.skill.component.recomment.Interface;

import java.util.List;

import app.ai.service.cv.skill.component.recomment.dto.SkillSuggestion;

public interface ISkillRecommender {
    List<SkillSuggestion> suggest(List<String> currentSkills, int limit);
}
