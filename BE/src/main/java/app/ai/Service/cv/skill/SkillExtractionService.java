package app.ai.service.cv.skill;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.ai.service.cv.skill.component.SkillAdviceGenerator;
import app.ai.service.cv.skill.component.SkillMatchEvaluator;
import app.ai.service.cv.skill.component.extractorskill.SkillExtractor;
import app.ai.service.cv.skill.component.recomment.dto.ComprehensiveRecommendation;

/**
 * CHỨC NĂNG:
 * - Tiếp nhận text -> Trích xuất -> Giao cho SkillMatchEvaluator và SkillAdviceGenerator xử lý -> Tổng hợp.
 */
@Service
public class SkillExtractionService {
    @Autowired private SkillExtractor skillExtractor;
    @Autowired private SkillMatchEvaluator matchEvaluator;  // Xử lý điểm số khớp
    @Autowired private SkillAdviceGenerator adviceGenerator; // Xử lý tư vấn & gợi ý

    public Map<String, Object> analyzeFull(String cvText, String jobText, String targetRole) {
        // Dùng SkillExtractor trích xuất kỹ năng từ văn bản thô (AI/Regex)
        List<String> cvSkills = skillExtractor.extract(cvText);
        Map<String, Set<String>> jobSkillsMap = skillExtractor.extractByCategory(jobText);

        // Giao cho MatchEvaluator tính toán độ khớp 
        Map<String, Object> scoreData = matchEvaluator.evaluate(cvSkills, jobSkillsMap);

        // 3. Giao cho AdviceGenerator xử lý cuối cùng trả về 1 ComprehensiveRecommendation
        ComprehensiveRecommendation recommendation = adviceGenerator.generate(cvSkills, jobSkillsMap, targetRole);

        // 4. Tổng hợp tất cả vào một Map cuối cùng để trả về API (JSON)
        Map<String, Object> finalResponse = new LinkedHashMap<>();
        
        // Điểm số và mức độ phù hợp hiện tại
        finalResponse.put("Mức độ phù hợp", scoreData);
        
        // Kế hoạch hành động (Kỹ năng thiếu, kỹ năng liên quan, lộ trình học)
        // Spring Boot sẽ tự động chuyển Object recommendation thành JSON đẹp đẽ
        finalResponse.put("Gợi ý bổ sung", recommendation); 
        
        return finalResponse;
    }
}
