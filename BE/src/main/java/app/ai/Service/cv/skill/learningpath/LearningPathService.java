package app.ai.service.cv.skill.learningpath;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import app.ai.service.cv.skill.learningpath.componet.LearningPathMapper;
import app.ai.service.cv.skill.learningpath.componet.LearningPathProcessor;
import app.ai.service.cv.skill.learningpath.dto.LearningPathDTO;
import app.ai.service.cv.skill.learningpath.dto.RoleSuggestionDTO;
import app.ai.service.cv.skill.learningpath.dto.StepDTO;
import app.ai.service.cv.skill.learningpath.model.Role;
import app.ai.service.cv.skill.learningpath.repository.IRoleRepository;

@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final IRoleRepository roleRepository;
    private final LearningPathMapper mapper;
    private final LearningPathProcessor processor;

    @Transactional(readOnly = true) // sẽ không cho phép bất kỳ thây đổi nào đến dữ liệu khi đang thực hiện method
    public LearningPathDTO buildPath(String targetRole, List<String> currentSkills) {
        // 1. Lấy dữ liệu
        Role role = roleRepository.findByNameIgnoreCase(targetRole)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Set<String> userSkills = currentSkills.stream()
                .map(String::toLowerCase).collect(Collectors.toSet());

        // 2. Nhờ Mapper chuyển đổi dữ liệu
        List<StepDTO> steps = role.getSteps().stream()
                .map(step -> mapper.toStepDTO(step, userSkills))
                .collect(Collectors.toList());

        // 3. Nhờ Processor xử lý logic tính toán
        LearningPathDTO path = new LearningPathDTO();
        path.setTargetRole(targetRole);
        path.setSteps(steps);
        processor.processMetrics(path, steps);

        return path;
    }

    @Transactional(readOnly = true)
    public List<RoleSuggestionDTO> suggestRolesBySkills(List<String> currentSkills) {
        List<Role> allRoles = roleRepository.findAllWithSteps();
        Set<String> userSkills = currentSkills.stream()
                .map(String::toLowerCase).collect(Collectors.toSet());

        // Nhờ Processor tính toán độ phù hợp cho từng Role
        return allRoles.stream()
                .map(role -> processor.calculateRoleMatch(role, userSkills))
                .sorted(Comparator.comparingDouble(RoleSuggestionDTO::getMatchPercentage).reversed())
                .collect(Collectors.toList());
    }
}