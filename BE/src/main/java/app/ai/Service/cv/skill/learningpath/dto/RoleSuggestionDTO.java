package app.ai.service.cv.skill.learningpath.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chức năng: Chứa kết quả tính toán độ phù hợp giữa User và một Role cụ thể.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSuggestionDTO {
    private String roleName;         // Tên vị trí (VD: Java Backend Developer)
    private double matchPercentage;  // Tỷ lệ khớp (%)
    private int matchedSkillsCount;  // Số kỹ năng đã có
    private int totalRequiredSkills; // Tổng số kỹ năng yêu cầu của Role đó
}
