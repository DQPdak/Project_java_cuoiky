package app.ai.service.cv.gemini.dto.analysis;

import lombok.Data;
import java.util.List;

@Data
public class CareerAdviceResult {
    // Phần 1: Gợi ý kỹ năng
    private List<String> extraSkills;         // User có, Job không cần (Điểm mạnh)
    private List<String> recommendedSkills;   // Job không cần, nhưng User nên học (Xu hướng)
    
    // Phần 2: Lộ trình
    private String estimatedDuration;         // VD: "4 tuần"
    private String learningPath;              // Nội dung Markdown lộ trình
    
    // Phần 3: Lời khuyên
    private String careerAdvice;              // Lời khuyên tổng quát
}