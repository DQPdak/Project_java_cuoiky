package app.ai.service.cv.skill.component.recomment.recommendation.component;

import org.springframework.stereotype.Component;

/**
 * CHỨC NĂNG:
 * - Nhận vào SkillFrequency.
 * - Trả về priority (1-10) theo công thức định nghĩa.
 */
@Component
public class PriorityCalculator {
    public int calculate(SkillFrequency frequency) {
        int count = frequency.getCount();
        return Math.min(count * 2 + 3, 10);
    }
}
