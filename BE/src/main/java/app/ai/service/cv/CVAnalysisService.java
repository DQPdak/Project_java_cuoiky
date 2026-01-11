package app.ai.service.cv;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.ai.service.cv.dto.CVAnalysisResult;
import app.ai.service.cv.extractorcontact.ContactInfoExtractor;
import app.ai.service.cv.extractorcontact.dto.ContactInfo;
import app.ai.service.cv.extractorexperience.ExperienceService;
import app.ai.service.cv.extractorexperience.dto.ExperienceDTO;
import app.ai.service.cv.extractortext.CVTextExtractor;
import app.ai.service.cv.skill.SkillExtractionService;
import app.ai.service.cv.skill.component.recomment.dto.ComprehensiveRecommendation;

@Service
public class CVAnalysisService {
    @Autowired private CVTextExtractor textExtractor;
    @Autowired private ExperienceService experienceService;
    @Autowired private ContactInfoExtractor contactExtractor;
    @Autowired private SkillExtractionService skillService;

    // Thread Pool để chạy song song (Optimization)
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     * Phương thức điều phối toàn bộ hoạt động phân tích thông minh
     * (Đây là phương thức duy nhất chúng ta cần dùng)
     */
    public CVAnalysisResult analyzeComprehensive(MultipartFile file, String jobText, String targetRole) throws Exception {
        long startTime = System.currentTimeMillis();

        // Bước 1: Điều phối Text Extraction (Dùng Extractor thông minh có OCR)
        String cvText = textExtractor.extractText(file); 

        // Bước 2: Điều phối các hoạt động phân tích chuyên sâu chạy song song
        
        // Luồng 1: Phân tích kinh nghiệm
        CompletableFuture<ExperienceDTO> expFuture = CompletableFuture
            .supplyAsync(() -> experienceService.analyzeExperience(cvText), executor);

        // Luồng 2: Trích xuất thông tin liên hệ
        CompletableFuture<ContactInfo> contactFuture = CompletableFuture
            .supplyAsync(() -> contactExtractor.extract(cvText), executor);

        // Luồng 3: Phân tích Skill chiến thuật (Trích xuất + So khớp + Lời khuyên)
        CompletableFuture<Map<String, Object>> skillAnalysisFuture = CompletableFuture
            .supplyAsync(() -> skillService.analyzeFull(cvText, jobText, targetRole), executor);

        // Chờ tất cả các "bộ não" xử lý xong (Timeout 10s cho an toàn với OCR)
        CompletableFuture.allOf(expFuture, contactFuture, skillAnalysisFuture).get(10, TimeUnit.SECONDS);

        // Bước 3: Tổng hợp toàn bộ trí tuệ nhân tạo vào một kết quả duy nhất
        CVAnalysisResult finalResult = new CVAnalysisResult();
        
        // Tổng hợp thông tin liên hệ
        finalResult.setContactInfo(contactFuture.get());
        
        // Tổng hợp kinh nghiệm
        finalResult.setExperience(expFuture.get());
        
        // Tổng hợp phân tích Skill (Lấy từ Map trả về)
        Map<String, Object> skillData = skillAnalysisFuture.get();
        finalResult.setMatchScore(skillData.get("Mức độ phù hợp"));
        finalResult.setRecommendations((ComprehensiveRecommendation) skillData.get("Gợi ý bổ sung"));
        
        // Metadata
        finalResult.setRawText(cvText);
        finalResult.setProcessingTimeMs(System.currentTimeMillis() - startTime);

        return finalResult;
    }
}