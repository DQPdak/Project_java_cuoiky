package app.ai.service.cv.RECOMMENDER.Interface;

import java.util.List;

import app.ai.service.cv.RECOMMENDER.dto.SkillSuggestion;

public interface ISkillRecommender {
    List<SkillSuggestion> suggest(List<String> currentSkills, int limit);
}
