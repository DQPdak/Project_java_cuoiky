package app.ai.service.cv.recomment.Interface;

import java.util.List;

import app.ai.service.cv.recomment.dto.SkillSuggestion;

public interface ISkillRecommender {
    List<SkillSuggestion> suggest(List<String> currentSkills, int limit);
}
