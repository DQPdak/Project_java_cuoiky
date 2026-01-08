package app.ai.service.cv.skill.learningpath.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StepDTO {
    private int order;
    private String skillName;
    private String description;
    private boolean isCore;
    private boolean isCompleted;
}