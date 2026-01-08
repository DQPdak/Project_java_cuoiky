package app.ai.service.cv.skill.recommendation.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: GapAnalysis
 * CHỨC NĂNG: Trả về kết quả phân tích sự chênh lệch giữa kỹ năng hiện có và yêu cầu.
 */
@Data
@NoArgsConstructor  
@AllArgsConstructor 
@Builder            
public class GapAnalysis {
    
    private int totalRequired;      // Tổng số kỹ năng yêu cầu
    private int matchingCount;       // Số kỹ năng khớp
    private int missingCount;        // Số kỹ năng thiếu
    private int extraCount;          // Số kỹ năng thừa (User có nhưng Job không yêu cầu)
    private double coveragePercentage; // Tỷ lệ bao phủ (%)

    private List<String> matchingSkills; 
    private List<String> missingSkills;  
    private List<String> extraSkills;    

    /**
     * Tùy chỉnh toString để log dữ liệu đẹp mắt và dễ đọc hơn khi debug.
     */
    @Override
    public String toString() {
        return String.format(
            "GapAnalysis [Coverage: %.1f%% | Matching: %d/%d | Missing: %d | Extra: %d]", 
            coveragePercentage, matchingCount, totalRequired, missingCount, extraCount
        );
    }
}