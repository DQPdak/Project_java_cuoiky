package app.ai.service.cv.skill.recommendation.util;

import java.util.ArrayList;
import java.util.List;

/**
 * CHỨC NĂNG:
 * - Đếm số lần skill này được gợi ý từ các current skills.
 * - Lưu danh sách skills nào đã trigger recommendation này.
 */

public class SkillFrequency {
    private int count = 0;                      // Số lần được recommend
        private final List<String> relatedSkills;   // Skills trigger recommendation
        
        public SkillFrequency() {
            this.relatedSkills = new ArrayList<>();
        }
        
        public void increment(String relatedSkill) {
            count++;
            relatedSkills.add(relatedSkill);
        }
        
        public int getCount() {
            return count;
        }
        
        public List<String> getRelatedSkills() {
            return relatedSkills;
        }
    }

