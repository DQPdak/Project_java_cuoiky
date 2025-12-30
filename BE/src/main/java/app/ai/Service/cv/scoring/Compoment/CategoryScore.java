package app.ai.Service.cv.scoring.Compoment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import app.ai.Service.cv.scoring.Interface.ISkillScorer;
import app.ai.Service.cv.skill.Component.SkillDatabase;

@Component
public class CategoryScore {
    private final ISkillScorer matchScore;
    private final SkillDatabase skillDatabase;

    public CategoryScore(ISkillScorer matchScore, SkillDatabase skillDatabase) {
        this.matchScore = matchScore;
        this.skillDatabase = skillDatabase;
    }

    public Map<String, Double> calculate(List<String> candidateFlatSkills,
                                         Map<String, List<String>> requiredSkills) {
        
        // 1. Phân loại kỹ năng ứng viên dựa trên SkillDatabase
        Map<String, List<String>> categorizedCandidate = categorizeSkills(candidateFlatSkills);

        // 2. Tính điểm
        Map<String, Double> results = new HashMap<>();

        for (String category : requiredSkills.keySet()) {
            // Lấy danh sách kỹ năng ứng viên đã được phân loại vào nhóm này
            List<String> candidateSkillsInCat = categorizedCandidate.getOrDefault(category, new ArrayList<>());
            
            // Lấy danh sách kỹ năng yêu cầu trong nhóm này
            List<String> requiredSkillsInCat = requiredSkills.get(category);

            // Tính toán điểm số bằng matchScore
            double score = matchScore.calculate(candidateSkillsInCat, requiredSkillsInCat);
            results.put(category, score);
        }

        return results;
    }

    /**
     * Hàm phụ trợ: Chuyển đổi List phẳng sang Map có Category
     */
    private Map<String, List<String>> categorizeSkills(List<String> flatSkills) {
        Map<String, List<String>> categorizedMap = new HashMap<>();

        for (String skill : flatSkills) {
            // Sử dụng hàm getCategoryOfSkill có sẵn trong SkillDatabase của bạn
            String category = skillDatabase.getCategoryOfSkill(skill);
            
            if (category != null) {
                categorizedMap.computeIfAbsent(category, k -> new ArrayList<>()).add(skill);
            } else {
                // Tùy chọn: Xếp vào nhóm "OTHER" nếu không tìm thấy trong DB
                categorizedMap.computeIfAbsent("UNCATEGORIZED", k -> new ArrayList<>()).add(skill);
            }
        }
        return categorizedMap;
    }
}