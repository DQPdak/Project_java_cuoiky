package app.ai.service.cv.scoring.compoment;

import org.springframework.stereotype.Component;

import app.ai.service.cv.scoring.Interface.ISkillScorer;
import app.ai.service.cv.skill.matching.SkillMatcher;
import app.ai.service.cv.skill.matching.dto.SkillMatchResult;

import java.util.List;

// điểm cộng thêm cho số kỹ năng thừa mà ứng viên sở hữu so với yêu cầu công việc
@Component
public class BONUSScore implements ISkillScorer {
    private static final double BONUS_WEIGHT = 0.2;
    private SkillMatcher sm = new SkillMatcher();

    public int ExtraCount(List<String> candidateSkills, List<String> requiredSkills){
        SkillMatchResult matchResult = sm.match(candidateSkills, requiredSkills);
        int extraCount = matchResult.getExtraSkills().size();
        return extraCount;
    }
    @Override
    public double calculate(List<String> candidateSkills, List<String> requiredSkills) {
        if (candidateSkills == null || candidateSkills.isEmpty()) {
            return 0.0; // Nếu ứng viên không có kỹ năng nào, điểm là 0
        }

        return (double) Math.min(ExtraCount(candidateSkills, requiredSkills) * BONUS_WEIGHT * 10, 20); // tối đa 20 điểm bonus
    }
}
