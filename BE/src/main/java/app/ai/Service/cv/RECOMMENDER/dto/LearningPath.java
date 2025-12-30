package app.ai.Service.cv.RECOMMENDER.dto;

import java.util.List;

import lombok.Data;

// Lưu các danh sách skill cần bổ sung, đã hoàn thành.
@Data
public class LearningPath {
    private String targetRole;            // Role mục tiêu
    private int totalSteps;               // Tổng số skills cần học
    private int completedSteps;           // Số skills đã có
    private List<String> completedSkills; // Skills đã hoàn thành
    private List<String> remainingSkills; // Skills còn lại
    private double progressPercentage;    // % hoàn thành
}
