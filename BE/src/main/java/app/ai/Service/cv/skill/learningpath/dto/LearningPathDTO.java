package app.ai.service.cv.skill.learningpath.dto;

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
    private String targetRole;
    private List<StepDTO> steps;
    private int totalSteps;
    private int completedSteps;
    private double progressPercentage;
    private StepDTO nextStep;
    private int estimatedWeeks;
}
