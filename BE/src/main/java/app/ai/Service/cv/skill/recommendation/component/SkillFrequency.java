package app.ai.service.cv.skill.recommendation.component;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * CHỨC NĂNG:
 * - Đếm số lần skill này được gợi ý từ các current skills.
 * - Lưu danh sách skills nào đã trigger recommendation này.
 */
@Component
public class SkillFrequency {
    private int count = 0;                      // Số lần được recommend
        private final List<String> relatedSkills;   // Skills trigger recommendation
        
        public SkillFrequency() {
            this.relatedSkills = new ArrayList<>();
        }
        
        public void increment(String relatedSkill) {
            this.count++;
        // Ngăn chặn trùng lặp kỹ năng liên quan nếu cần
        if (!this.relatedSkills.contains(relatedSkill)) {
            this.relatedSkills.add(relatedSkill);
        }
        }
        
        public int getCount() {
            return count;
        }
        
        public List<String> getRelatedSkills() {
            return relatedSkills;
        }
    }

