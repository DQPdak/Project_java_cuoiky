package app.ai.service.cv.skill.component.recomment.learningpath.componet;

import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.component.recomment.learningpath.dto.StepDTO;
import app.ai.service.cv.skill.component.recomment.learningpath.model.LearningStep;

import java.util.Set;

/**
     * Chuyển đổi từ Entity LearningStep sang DTO StepDTO
     * và xác định trạng thái hoàn thành dựa trên bộ kỹ năng của User.
     */
@Component
public class LearningPathMapper {
    public StepDTO toStepDTO(LearningStep entity, Set<String> userSkills) {
        boolean isCompleted = userSkills.contains(entity.getSkillName().toLowerCase());
        
        return new StepDTO(
            entity.getSequenceOrder(),
            entity.getSkillName(),
            entity.getDescription(),
            entity.isCore(),
            isCompleted
        );
    }
}
