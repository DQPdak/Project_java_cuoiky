package app.ai.service.cv.skill.component.recomment.dto;

import java.util.List;

import app.ai.service.cv.skill.component.recomment.learningpath.dto.LearningPathDTO;
import app.ai.service.cv.skill.component.recomment.learningpath.dto.RoleSuggestionDTO;
import app.ai.service.cv.skill.component.recomment.recommendation.dto.GapAnalysis;
import app.ai.service.cv.skill.component.recomment.recommendation.dto.SkillRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprehensiveRecommendation {
    private List<SkillRecommendation> relatedSkills; // Danh sách kỹ năng liên quan nên học thêm 
    private List<SkillRecommendation> gapSkills; // Danh sách kỹ năng còn thiếu so với Job 
    private GapAnalysis gapAnalysis; // Phân tích chi tiết phần trăm khớp, kỹ năng đã có 
    private LearningPathDTO learningPath; // Lộ trình học tập chi tiết 
    private List<RoleSuggestionDTO> roleSuggestions; // Danh sách các vị trí công việc phù hợp khác (Từ LearningPathService)
}
