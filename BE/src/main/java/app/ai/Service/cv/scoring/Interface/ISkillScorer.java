package app.ai.service.cv.scoring.Interface;

import java.util.List;

// Interface định nghĩa phương thức chấm điểm kỹ năng

public interface ISkillScorer {
    double calculate(List<String> candidateSkills, List<String> requiredSkills);
}
