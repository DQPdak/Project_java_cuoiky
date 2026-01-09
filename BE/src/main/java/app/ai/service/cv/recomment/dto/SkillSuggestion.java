package app.ai.service.cv.recomment.dto;

import lombok.Data;

// Chứa thông tin về một kỹ năng được gợi ý bao gồm tên, lý do gợi ý và mức độ ưu tiên (1-10).
@Data
public class SkillSuggestion {
    private String skillName;
    private String reason;
    private int priority;
}
