package app.ai.service.cv.recomment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Chứa thông tin về một kỹ năng được gợi ý bao gồm tên, lý do gợi ý và mức độ ưu tiên (1-10).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillSuggestion {
    private String skillName;
    private String reason;
    private int priority;
}
