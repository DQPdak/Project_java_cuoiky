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

/**
 * CHỨC NĂNG:
 * - Tiếp nhận text -> Trích xuất -> Giao cho SkillMatchEvaluator và SkillAdviceGenerator xử lý -> Tổng hợp.
 */
@Service
public class SkillExtractionService {
    @Autowired private SkillExtractor skillExtractor;
    @Autowired private SkillMatchEvaluator matchEvaluator; // File phụ 1
    @Autowired private SkillAdviceGenerator adviceGenerator; // File phụ 2

    public Map<String, Object> analyzeFull(String cvText, String jobText, String targetRole) {
        // 1. Dùng SkillExtractor trích xuất kỹ năng từ văn bản thô
        List<String> cvSkills = skillExtractor.extract(cvText);
        Map<String, Set<String>> jobSkillsMap = skillExtractor.extractByCategory(jobText);

        // 2. Giao cho MatchEvaluator tính toán con số và mức độ khớp
        Map<String, Object> scoreData = matchEvaluator.evaluate(cvSkills, jobSkillsMap);

        // 3. Giao cho AdviceGenerator tìm kỹ năng thiếu và lộ trình học
        Map<String, Object> adviceData = adviceGenerator.generate(cvSkills, jobSkillsMap, targetRole);

        // 4. Tổng hợp tất cả vào một Map cuối cùng
        Map<String, Object> finalResponse = new LinkedHashMap<>();
        finalResponse.put("evaluation", scoreData);
        finalResponse.put("action_plan", adviceData);
        
        return finalResponse;
    }
}
