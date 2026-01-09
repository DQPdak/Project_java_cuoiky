package app.ai.service.cv.scoring.compoment;

import java.util.List;

import org.springframework.stereotype.Component;

import app.ai.service.cv.scoring.Interface.ISkillScorer;
import app.ai.service.cv.skill.matching.SkillMatcher;
import app.ai.service.cv.skill.matching.dto.SkillMatchResult;

/**
 * Class có trách nhiệm chấm điểm tỷ lệ tuơng đồng giữa kỹ năng của ứng viên và kỹ năng yêu cầu.
 */

@Component
public class MatchScore implements ISkillScorer {
    private SkillMatcher sm = new SkillMatcher();
    public int MatchCount(List<String> candidateSkills, List<String> requiredSkills){
        SkillMatchResult matchResult = sm.match(candidateSkills, requiredSkills);
        int matchCount = matchResult.getMatchCount();
        return matchCount;
    }
    @Override
    public double calculate(List<String> candidateSkills, List<String> requiredSkills) {
        if (candidateSkills == null || candidateSkills.isEmpty()) {
            return 0.0; // Nếu ứng viên không có kỹ năng nào, điểm là 0
        }
        int matchCount = MatchCount(candidateSkills, requiredSkills);
        // Tính điểm dựa trên tỷ lệ kỹ năng khớp
        return (double) matchCount / requiredSkills.size()*100;
    }
}
