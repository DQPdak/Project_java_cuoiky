package app.ai.service.cv.skill.component.recomment;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.ai.service.cv.skill.component.recomment.dto.ComprehensiveRecommendation;
import app.ai.service.cv.skill.component.recomment.dto.SkillAdviceRequest;
import app.ai.service.cv.skill.component.recomment.learningpath.LearningPathService;
import app.ai.service.cv.skill.component.recomment.learningpath.dto.LearningPathDTO;
import app.ai.service.cv.skill.component.recomment.recommendation.GapSkillOrchestrator;
import app.ai.service.cv.skill.component.recomment.recommendation.RelatedSkillRecommender;
import app.ai.service.cv.skill.component.recomment.recommendation.dto.SkillRecommendation;

@Service
public class ComprehensiveAdvisor {
    @Autowired 
    private RelatedSkillRecommender relatedRecommender;
    @Autowired 
    private GapSkillOrchestrator gapOrchestrator;
    @Autowired 
    private LearningPathService learningPathService;

    /**
     * Phương thức chính để tổng hợp mọi loại tư vấn dựa trên thông tin truyền vào.
     */
    public ComprehensiveRecommendation advise(SkillAdviceRequest request) {
        // Khởi tạo đối tượng kết quả tổng hợp
        ComprehensiveRecommendation report = new ComprehensiveRecommendation();
        
        List<String> cvSkills = request.getCurrentSkills();
        List<String> jobSkills = request.getRequiredSkills();

        // GỢI Ý KỸ NĂNG LIÊN QUAN (Luôn thực hiện dựa trên CV)
        report.setRelatedSkills(relatedRecommender.recommend(request.getCurrentSkills(), 5));

        // PHÂN TÍCH GAP (Chỉ thực hiện nếu có dữ liệu từ tin tuyển dụng)
        if (jobSkills != null && !jobSkills.isEmpty()) {
            // Lấy danh sách kỹ năng thiếu (Gaps)
            List<SkillRecommendation> gaps = gapOrchestrator.getRecommendations(
                cvSkills, jobSkills, Collections.emptyList()
            );
            report.setGapSkills(gaps);

            // Lấy phân tích chi tiết (Completed vs Missing)
            report.setGapAnalysis(gapOrchestrator.getGapAnalysis(cvSkills, jobSkills));
        }

        // LỘ TRÌNH HỌC TẬP (Chỉ thực hiện nếu người dùng có mục tiêu cụ thể)
        if (request.getTargetRole() != null && !request.getTargetRole().trim().isEmpty()) {
            LearningPathDTO path = learningPathService.buildPath(
                request.getTargetRole(), cvSkills
            );
            report.setLearningPath(path);
        }

        // GỢI Ý VỊ TRÍ PHÙ HỢP (Dựa trên bộ kỹ năng hiện tại)
        report.setRoleSuggestions(learningPathService.suggestRolesBySkills(cvSkills));

        return report;
    }
}
