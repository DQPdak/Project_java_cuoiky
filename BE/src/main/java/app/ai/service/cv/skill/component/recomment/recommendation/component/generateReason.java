package app.ai.service.cv.skill.component.recomment.recommendation.component;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 *CHỨC NĂNG:
 * - Nhận vào SkillFrequency.
 * - Trả về chuỗi mô tả lý do gợi ý (ví dụ: "Thường đi kèm với Java và Spring").
 * 
*/
@Component
public class generateReason {
    public String generate(SkillFrequency frequency) {
       List<String> relatedSkills = frequency.getRelatedSkills();
        
        if (relatedSkills == null || relatedSkills.isEmpty()) {
            return "Kỹ năng phổ biến trong lĩnh vực này";
        }
        
        int size = relatedSkills.size();
        
        return switch (size) {
            case 1 -> "Thường đi kèm với " + relatedSkills.get(0);
            case 2 -> String.format("Thường đi kèm với %s và %s", 
                        relatedSkills.get(0), relatedSkills.get(1));
            default -> String.format("Thường đi kèm với %s, %s và %d kỹ năng khác", 
                        relatedSkills.get(0), relatedSkills.get(1), size - 2);
        };
    }
}
