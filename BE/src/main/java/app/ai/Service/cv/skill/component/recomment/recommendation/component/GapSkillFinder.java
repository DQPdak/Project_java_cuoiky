package app.ai.service.cv.skill.component.recomment.recommendation.component;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CLASS: GapSkillFinder
 * CHỨC NĂNG: Chỉ thực hiện logic so khớp (Set Difference).
 */
@Component
public class GapSkillFinder {

    public List<String> findMissing(List<String> currentSkills, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> currentSet = currentSkills.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        return requiredSkills.stream()
            .filter(skill -> !currentSet.contains(skill.toLowerCase()))
            .collect(Collectors.toList());
    }
}
