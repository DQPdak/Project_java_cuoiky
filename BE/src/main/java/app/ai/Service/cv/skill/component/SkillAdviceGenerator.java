package app.ai.service.cv.skill.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.component.recomment.learningpath.LearningPathService;
import app.ai.service.cv.skill.component.recomment.learningpath.dto.LearningPathDTO;
import app.ai.service.cv.skill.component.recomment.recommendation.GapSkillOrchestrator;
import app.ai.service.cv.skill.component.recomment.recommendation.RelatedSkillRecommender;
import app.ai.service.cv.skill.component.recomment.recommendation.dto.SkillRecommendation;

/**
    * CHỨC NĂNG: Kết hợp 3 nguồn dữ liệu để đưa ra lời khuyên hành động:
    * - Kỹ năng còn thiếu so với Job (Gap Analysis).
    * - Kỹ năng nên học thêm dựa trên sở thích/thị trường (Related Skills).
    * - Lộ trình học tập cụ thể (Learning Path).
     */
    @Component
public class SkillAdviceGenerator {
    @Autowired private GapSkillOrchestrator gapOrchestrator;     // Tìm cái THIẾU
    @Autowired private RelatedSkillRecommender relatedRecommender; // Tìm cái LIÊN QUAN
    @Autowired private LearningPathService learningPathService; // Vẽ LỘ TRÌNH

    public Map<String, Object> generate(List<String> cvSkills, Map<String, Set<String>> jobSkillsMap, String targetRole) {
        
        // --- CHUẨN BỊ DỮ LIỆU PHẲNG ---
        List<String> flatJobSkills = jobSkillsMap.values().stream()
                                     .flatMap(Collection::stream).toList();

        // TÌM KỸ NĂNG THIẾU (BẮT BUỘC)
        // Đây là những kỹ năng trong JD mà CV không có.
        List<SkillRecommendation> gaps = gapOrchestrator.getRecommendations(
            cvSkills, flatJobSkills, new ArrayList<>()
        );

        // TÌM KỸ NĂNG LIÊN QUAN (MỞ RỘNG)
        // Dựa trên những gì người dùng ĐÃ CÓ, gợi ý thêm các kỹ năng vệ tinh để làm đẹp CV.
        // Ví dụ: Người dùng có Java, hệ thống gợi ý thêm Redis, Kafka...
        List<SkillRecommendation> related = relatedRecommender.recommend(cvSkills, 5);

        // LOGIC XÂY DỰNG LỘ TRÌNH HỌC TẬP
        LearningPathDTO path = learningPathService.buildPath(targetRole, cvSkills);

        // --- ĐÓNG GÓI KẾT QUẢ ---
        Map<String, Object> adviceResult = new LinkedHashMap<>();
        
        // Nhóm "Phải học để đáp ứng Job"
        adviceResult.put("mandatory_gaps", gaps); 
        
        // Nhóm "Nên học để nâng cấp bản thân"
        adviceResult.put("suggested_extensions", related); 
        
        // Bản đồ lộ trình
        adviceResult.put("learning_path", path);
        
        return adviceResult;
    }
}
