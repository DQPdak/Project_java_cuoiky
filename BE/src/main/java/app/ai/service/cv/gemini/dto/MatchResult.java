package app.ai.service.cv.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchResult {
    private int matchPercentage;      // Điểm tương thích %
    
    // Các con số thống kê
    private int matchedSkillsCount;   // Số kỹ năng tương thích
    private int missingSkillsCount;   // Số kỹ năng còn thiếu
    private int extraSkillsCount;     // Số kỹ năng thừa ra
    private int totalRequiredSkills;  // Tổng số kỹ năng Job yêu cầu
    private List<String> missingSkillsList; //  Danh sách chi tiết để hiển thị nếu cần
    private String evaluation;        // Nhận xét ngắn gọn

    
}
