package app.ai.service.cv.skill.component.recomment.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillAdviceRequest {
    private List<String> currentSkills; // danh sách kỹ năng của người dùng
    private List<String> requiredSkills; // danh sách kỹ năng yêu cầu
    private String targetRole; // vị trí ứng tuyển
}
