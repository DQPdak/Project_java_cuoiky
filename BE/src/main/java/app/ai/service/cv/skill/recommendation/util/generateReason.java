package app.ai.service.cv.skill.recommendation.util;

import java.util.List;

/**
 *CHỨC NĂNG:
 * - Nhận vào SkillFrequency.
 * - Trả về chuỗi mô tả lý do gợi ý (ví dụ: "Thường đi kèm với Java và Spring").
 * 
*/

public class generateReason {
    public String generate(SkillFrequency frequency) {
        List<String> relatedSkills = frequency.getRelatedSkills();
        
        if (relatedSkills.size() == 1) {
            return "Thường đi kèm với " + relatedSkills.get(0);
        } else if (relatedSkills.size() == 2) {
            return "Thường đi kèm với " + 
                   relatedSkills.get(0) + " và " + relatedSkills.get(1);
        } else {
            return "Thường đi kèm với " + 
                   relatedSkills.get(0) + ", " + 
                   relatedSkills.get(1) + " và " + 
                   (relatedSkills.size() - 2) + " kỹ năng khác";
        }
    }
}
