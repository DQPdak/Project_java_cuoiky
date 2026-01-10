package app.ai.service.cv.skill.component.scoring.compoment;

import java.util.List;

import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.component.scoring.dto.SkillScore;

@Component
public class TotalScore  {
    private MatchScore matchScore = new MatchScore();
    private BONUSScore bonusScore = new BONUSScore();

    public SkillScore calculate(List<String> candidateSkills, List<String> requiredSkills) {
        double match = matchScore.calculate(candidateSkills, requiredSkills);
        double bonus = bonusScore.calculate(candidateSkills, requiredSkills);
        double totalScore = Math.min(match + bonus, 100.0); // Tổng điểm tối đa là 100
        SkillScore score = new SkillScore();
        score.setBaseScore(match);
        score.setBonusScore(bonus);
        score.setTotalScore(totalScore);
        score.setExactMatches((int) matchScore.MatchCount(candidateSkills, requiredSkills));
        score.setTotalRequired(requiredSkills.size());
        score.setExtraSkillCount((int) bonusScore.ExtraCount(candidateSkills, requiredSkills));
        score.setLevel(Level(totalScore));
        return score;
    }
    /**
     * Phân loại score thành levels
     * 
     * LEVELS:
     * - EXCELLENT: 80-100
     * - GOOD: 60-79
     * - FAIR: 40-59
     * - POOR: 0-39
     * 
     */
    private String Level(double score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "FAIR";
        return "POOR";
    }
    
}
