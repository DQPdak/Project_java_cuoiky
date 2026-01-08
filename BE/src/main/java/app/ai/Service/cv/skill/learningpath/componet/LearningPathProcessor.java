package app.ai.service.cv.skill.learningpath.componet;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.learningpath.dto.LearningPathDTO;
import app.ai.service.cv.skill.learningpath.dto.RoleSuggestionDTO;
import app.ai.service.cv.skill.learningpath.dto.StepDTO;
import app.ai.service.cv.skill.learningpath.model.Role;

@Component
public class LearningPathProcessor {

    /**
     * Tính toán các chỉ số đo lường cho lộ trình (Progress, Next Step, Weeks)
     */
    public void processMetrics(LearningPathDTO path, List<StepDTO> steps) {
        int total = steps.size();
        int completed = (int) steps.stream().filter(StepDTO::isCompleted).count();
        
        path.setTotalSteps(total);
        path.setCompletedSteps(completed);
        path.setProgressPercentage(total == 0 ? 0 : (double) completed / total * 100);
        
        // Tìm bước tiếp theo
        path.setNextStep(steps.stream()
                .filter(s -> !s.isCompleted())
                .findFirst()
                .orElse(null));

        // Tính thời gian dự kiến
        int weeks = steps.stream()
                .filter(s -> !s.isCompleted())
                .mapToInt(s -> s.isCore() ? 2 : 1)
                .sum();
        path.setEstimatedWeeks(weeks);
    }

    /**
     * Tính toán độ phù hợp (%) của một Role so với kỹ năng hiện tại
     */
    public RoleSuggestionDTO calculateRoleMatch(Role role, Set<String> userSkills) {
        long matchCount = role.getSteps().stream()
                .filter(s -> userSkills.contains(s.getSkillName().toLowerCase()))
                .count();

        int total = role.getSteps().size();
        double percentage = (total == 0) ? 0 : (double) matchCount / total * 100;

        return new RoleSuggestionDTO(role.getName(), percentage, (int) matchCount, total);
    }
}
