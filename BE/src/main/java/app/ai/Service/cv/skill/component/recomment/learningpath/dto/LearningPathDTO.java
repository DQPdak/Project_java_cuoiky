package app.ai.service.cv.skill.component.recomment.learningpath.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LearningPathDTO {
    private String targetRole;          // Role mục tiêu
    
    // Phần lộ trình chi tiết
    private List<StepDTO> steps;        // Danh sách các bước học cụ thể
    private StepDTO nextStep;           // Bước tiếp theo cần thực hiện ngay
    private int estimatedWeeks;         // Dự kiến hoàn thành trong bao lâu
    
    // Phần thống kê kỹ năng (Lấy từ LearningPath cũ)
    private List<String> completedSkills; // Các kỹ năng ứng viên đã có
    private List<String> remainingSkills; // Các kỹ năng ứng viên còn thiếu
    
    // Phần định lượng
    private int totalSteps;             // Tổng số bước/kỹ năng
    private int completedSteps;         // Số bước/kỹ năng đã xong
    private double progressPercentage;  // % tiến độ
}
