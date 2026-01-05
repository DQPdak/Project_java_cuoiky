package app.ai.Service.cv.scoring.Compoment;

import org.springframework.stereotype.Component;

import app.ai.Service.cv.scoring.dto.SkillScore;

// CHỨC NĂNG: Tạo giải thích chi tiết về điểm số kỹ năng của ứng viên
@Component
public class ScoreExplanation {
     public String generateScoreExplanation(SkillScore score) {
        StringBuilder explanation = new StringBuilder();
        
        // Level
        String level = score.getLevel();
        explanation.append("Mức độ phù hợp: ").append(level).append("\n");
        
        // Exact matches
        explanation.append("✓ Đáp ứng ")
                  .append(score.getExactMatches())
                  .append("/")
                  .append(score.getTotalRequired())
                  .append(" kỹ năng yêu cầu\n");
        
        // Bonus
        if (score.getBonusScore() > 0) {
            explanation.append("✓ Có thêm ")
                      .append(score.getExtraSkillCount())
                      .append(" kỹ năng bổ sung (+" )
                      .append(String.format("%.1f", score.getBonusScore()))
                      .append(" điểm)\n");
        }
        
        // Advice
        if (score.getTotalScore() < 60) {
            int missingCount = score.getTotalRequired() - score.getExactMatches();
            explanation.append("⚠ Cần bổ sung thêm ")
                      .append(missingCount)
                      .append(" kỹ năng để tăng cơ hội");
        }
        
        return explanation.toString();
    }
}
