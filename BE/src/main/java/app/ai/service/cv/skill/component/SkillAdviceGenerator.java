package app.ai.service.cv.skill.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.component.recomment.ComprehensiveAdvisor;
import app.ai.service.cv.skill.component.recomment.dto.ComprehensiveRecommendation;
import app.ai.service.cv.skill.component.recomment.dto.SkillAdviceRequest;

/**
    * CHỨC NĂNG: Kết hợp 3 nguồn dữ liệu để đưa ra lời khuyên hành động:
    * - Kỹ năng còn thiếu so với Job (Gap Analysis).
    * - Kỹ năng nên học thêm dựa trên sở thích/thị trường (Related Skills).
    * - Lộ trình học tập cụ thể (Learning Path).
     */
    @Component
public class SkillAdviceGenerator {
    @Autowired 
    private ComprehensiveAdvisor advisor;

    public ComprehensiveRecommendation generate(List<String> cvSkills, Map<String, Set<String>> jobSkillsMap, String targetRole) {
        
        // Xuất giá trị ra SkillAdviceRequest để cho SkillRecommendationService sử lý
        SkillAdviceRequest request = new SkillAdviceRequest();
        request.setCurrentSkills(cvSkills);
        request.setTargetRole(targetRole);
        request.setRequiredSkills(jobSkillsMap.values().stream()
                                    .flatMap(Collection::stream).toList());

        //  Gửi cho tổng quản lý xử lý
        return advisor.advise(request);
    }
}
