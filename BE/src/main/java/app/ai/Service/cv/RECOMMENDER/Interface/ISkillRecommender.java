package app.ai.Service.cv.RECOMMENDER.Interface;

import app.ai.Service.cv.RECOMMENDER.dto.SkillSuggestion;
import java.util.List;

public interface ISkillRecommender {
    List<SkillSuggestion> suggest(List<String> currentSkills, int limit);
}
