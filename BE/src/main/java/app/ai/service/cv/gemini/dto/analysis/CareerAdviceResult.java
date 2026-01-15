package app.ai.service.cv.gemini.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CareerAdviceResult {
    
    // --- Phần 1: Phân tích Kỹ năng (Để user biết tại sao mình cần học) ---
    
    // [QUAN TRỌNG - THÊM MỚI] Danh sách kỹ năng Job yêu cầu mà User chưa có
    // (Đây là trọng tâm của Lộ trình học tập)
    private List<String> missingSkills;

    // User có mà Job không yêu cầu (Điểm cộng/Điểm mạnh)
    private List<String> extraSkills;         

    // Job không yêu cầu trong JD, nhưng AI khuyên nên học thêm (để đẹp profile)
    private List<String> recommendedSkills;   
    
    // --- Phần 2: Kế hoạch hành động ---
    
    // Thời gian ước tính hoàn thành (VD: "4 tuần")
    private String estimatedDuration;         
    
    // Nội dung chính: Lộ trình chi tiết dạng Markdown (chia theo Tuần/Chủ đề)
    private String learningPath;              
    
    // --- Phần 3: Lời khuyên ---
    
    // Lời khuyên ngắn gọn về thái độ, định hướng sự nghiệp
    private String careerAdvice;              
}