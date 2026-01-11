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
import app.ai.service.cv.skill.component.SkillTextNormalizer; // Import file mới tạo

/**
 * CHỨC NĂNG:
 * - Tiếp nhận text -> Làm sạch (Mới) -> Trích xuất -> Giao cho SkillMatchEvaluator và SkillAdviceGenerator xử lý -> Tổng hợp.
 */
@Service
public class SkillExtractionService {
    @Autowired private SkillExtractor skillExtractor;       // Logic tách skill (Giữ nguyên)
    @Autowired private SkillMatchEvaluator matchEvaluator;  // Xử lý điểm số khớp (Giữ nguyên)
    @Autowired private SkillAdviceGenerator adviceGenerator; // Xử lý tư vấn & gợi ý (Giữ nguyên)
    
    @Autowired private SkillTextNormalizer normalizer;      // Inject thêm "Người gác cổng" (Mới)

    public Map<String, Object> analyzeFull(String cvText, String jobText, String targetRole) {
        
        // LÀM SẠCH VĂN BẢN (Intervention) ---
        // Giúp SkillExtractor bên dưới hiểu được "J@va", "C #" mà không cần sửa code cũ
        String cleanCV = normalizer.normalize(cvText);
        String cleanJob = normalizer.normalize(jobText);

        // TRÍCH XUẤT 
        // Dùng SkillExtractor trích xuất kỹ năng từ văn bản (giờ đã sạch)
        List<String> cvSkills = skillExtractor.extract(cleanCV);
        Map<String, Set<String>> jobSkillsMap = skillExtractor.extractByCategory(cleanJob);

        // TÍNH ĐIỂM 
        // Giao cho MatchEvaluator tính toán độ khớp 
        Map<String, Object> scoreData = matchEvaluator.evaluate(cvSkills, jobSkillsMap);

        //  GỢI Ý 
        // Giao cho AdviceGenerator xử lý cuối cùng trả về 1 ComprehensiveRecommendation
        ComprehensiveRecommendation recommendation = adviceGenerator.generate(cvSkills, jobSkillsMap, targetRole);

        // TỔNG HỢP (Logic cũ)
        // Tổng hợp tất cả vào một Map cuối cùng để trả về API (JSON)
        Map<String, Object> finalResponse = new LinkedHashMap<>();
        
        // Điểm số và mức độ phù hợp hiện tại
        finalResponse.put("Mức độ phù hợp", scoreData);
        
        // Kế hoạch hành động (Kỹ năng thiếu, kỹ năng liên quan, lộ trình học)
        finalResponse.put("Gợi ý bổ sung", recommendation); 
        
        return finalResponse;
    }
}