package app.ai.service.cv.skill.recommendation.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: SkillRecommendation
 * Chứa kết quả gợi ý kỹ năng cụ thể sau khi đã qua Processor xử lý.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillRecommendation {

    private String skillName;    // Skill được recommend
    private int priority;        // Mức độ ưu tiên (1-10)
    private String reason;       // Giải thích lý do (từ ReasonGenerator)
    private List<String> relatedTo; // Danh sách kỹ năng gốc dẫn đến gợi ý này

    @Override
    public String toString() {
        return String.format("[%d/10] %s: %s (Dựa trên: %s)", 
            priority, skillName, reason, String.join(", ", relatedTo));
    }
}