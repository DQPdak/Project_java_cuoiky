package app.ai.service.cv.gemini.dto;

import lombok.Data;
import java.util.List;

@Data
public class MatchResult {
    // --- PHẦN 1: SỐ LIỆU THỐNG KÊ (STATISTICS) ---

    // 1. Điểm số tổng quan (0-100)
    private int matchPercentage;

    // 2. Tổng số skill Job yêu cầu (Mẫu số)
    private int totalRequiredSkills;

    // 3. Kỹ năng khớp (CV có + Job cần) -> Tử số
    private int matchedSkillsCount;
    private List<String> matchedSkillsList; 

    // 4. Kỹ năng thiếu (Job cần + CV không có) -> Bị trừ điểm
    private int missingSkillsCount;
    private List<String> missingSkillsList;
    
    // 5. Kỹ năng thừa (CV có + Job không cần) -> Không tính điểm
    private int extraSkillsCount;
    private List<String> extraSkillsList;   

    // --- PHẦN 2: NỘI DUNG PHÂN TÍCH (CONTENT) ---

    // 6. Đánh giá chung (Ngắn gọn - Tiếng Việt)
    // Tóm tắt điểm mạnh, điểm yếu chí mạng
    private String evaluation; 

    // [THÊM MỚI] 7. Lộ trình học tập chi tiết (Dạng Markdown - Tiếng Việt)
    // Chứa kế hoạch hành động từng tuần để lấp lỗ hổng kỹ năng
    private String learningPath; 

    // [THÊM MỚI] 8. Lời khuyên sự nghiệp (Tiếng Việt)
    // Về thái độ, cách phỏng vấn, deal lương...
    private String careerAdvice; 
}