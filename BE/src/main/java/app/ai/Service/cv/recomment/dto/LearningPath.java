package app.ai.service.cv.recomment.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lưu các danh sách skill cần bổ sung, đã hoàn thành.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath {
    private String targetRole;            // Role mục tiêu
    private int totalSteps;               // Tổng số skills cần học
    private int completedSteps;           // Số skills đã có
    private List<String> completedSkills; // Skills đã hoàn thành
    private List<String> remainingSkills; // Skills còn lại
    private double progressPercentage;    // % hoàn thành
}
