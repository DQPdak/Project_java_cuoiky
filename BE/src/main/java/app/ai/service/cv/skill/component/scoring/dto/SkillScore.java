package app.ai.service.cv.skill.component.scoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CHỨC NĂNG: Chứa điểm số kỹ năng của ứng viên
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillScore {
    private double baseScore;        // Điểm cơ bản (exact matches)
    private double bonusScore;       // Điểm bonus (extra skills)
    private double totalScore;       // Tổng điểm
    private int exactMatches;        // Số skills match chính xác
    private int totalRequired;       // Tổng số skills required
    private int extraSkillCount;     // Số extra skills
    private String level;           // Mức độ đánh giá (EXCELLENT, GOOD, FAIR, POOR)
}
