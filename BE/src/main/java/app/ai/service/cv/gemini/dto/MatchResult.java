package app.ai.service.cv.gemini.dto;

import lombok.Data;
import java.util.List;

@Data
public class MatchResult {
    // 1. Điểm số
    private int matchPercentage;

    // 2. Kỹ năng khớp (CV có + Job cần)
    private int matchedSkillsCount;
    private List<String> matchedSkillsList; // [THÊM MỚI]

    // 3. Kỹ năng thiếu (Job cần + CV không có)
    private int missingSkillsCount;
    private List<String> missingSkillsList;
    
    // 4. Kỹ năng thừa (CV có + Job không cần)
    private int extraSkillsCount;
    private List<String> extraSkillsList;   // [THÊM MỚI]

    // Tổng số skill Job yêu cầu
    private int totalRequiredSkills;

    // 5. Gợi ý hoàn thiện (3 phần: Bổ sung cái thiếu, Lời khuyên cái chưa có, Lộ trình)
    // Phần này AI sẽ trả về text Tiếng Việt dài
    private String evaluation; 
}