package app.ai.service.cv.skill.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.ai.service.cv.skill.component.matching.SkillMatcher;
import app.ai.service.cv.skill.component.matching.dto.SkillMatchResult;
import app.ai.service.cv.skill.component.scoring.CvScoringService;

/**
 * CHỨC NĂNG:
 * - Điều phối 2 class CvScoringService và SkillMatcher
 * - Tính toán mức độ ăn khớp (%) giữa ứng viên và yêu cầu công việc.
 * - Sử dụng CvScoringService để phân tích điểm theo từng nhóm kỹ năng.
 * - Sử dụng SkillMatcher để tìm ra kỹ năng Trùng, Thiếu, Thừa.
 * ----------------------------------------------------------------------------
 */
@Component
public class SkillMatchEvaluator {
    @Autowired private CvScoringService cvScoringService; // Service điều phối scoring của bạn
    @Autowired private SkillMatcher skillMatcher;        // Service so khớp tập hợp

    public Map<String, Object> evaluate(List<String> cvSkills, Map<String, Set<String>> jobSkillsMap) {
        
        // CHUẨN HÓA ĐẦU VÀO
        // CvScoringService yêu cầu List, trong khi Extractor trả về Set. 
        // Ta cần chuyển đổi Map<String, Set<String>> thành Map<String, List<String>>.
        Map<String, List<String>> requiredSkillsList = new HashMap<>();
        jobSkillsMap.forEach((category, skills) -> 
            requiredSkillsList.put(category, new ArrayList<>(skills))
        );

        // GỌI CvScoringService 
        // Đây là nơi thực thi logic phân tích điểm mạnh/yếu theo từng Category (Java, SQL, SoftSkills...)
        Map<String, Double> categoryAnalysis = cvScoringService.getCategoryAnalysis(cvSkills, requiredSkillsList);

        // GỌI SkillMatcher
        // Trải phẳng jobSkillsMap thành 1 danh sách duy nhất để so sánh tập hợp.
        List<String> flatJobSkills = jobSkillsMap.values().stream()
                                     .flatMap(Collection::stream).toList();
        
        // Thực hiện so khớp để biết chính xác tên các skill bị thiếu (Missing) hoặc trùng (Matched).
        SkillMatchResult matchResult = skillMatcher.match(cvSkills, flatJobSkills);

        // Đóng gói dữ liệu định lượng
        Map<String, Object> result = new HashMap<>();
        result.put("điểm số theo hạng mục", categoryAnalysis);
        result.put("chi tiết so sánh", matchResult);
        return result;
    }
}
