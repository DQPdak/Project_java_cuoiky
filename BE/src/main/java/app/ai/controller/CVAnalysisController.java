package app.ai.controller;

import app.candidate.model.CandidateProfile; // Entity mới
import app.candidate.service.CandidateService; // Service mới thay cho CVProcessingService
import app.ai.service.cv.extractortext.CVTextExtractor;
import app.ai.service.cv.gemini.GeminiService;
import app.ai.service.cv.gemini.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CVAnalysisController {

    private final CVTextExtractor cvTextExtractor;
    private final GeminiService geminiService;
    
    // [FIX] Inject CandidateService thay vì CVProcessingService đã xóa
    private final CandidateService candidateService;

    /**
     * API 1: Chỉ phân tích và trả về JSON (Dùng để test AI xem đọc đúng không)
     * Không lưu vào database.
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCV(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Đọc chữ
            String rawText = cvTextExtractor.extractText(file);
            System.out.println("--- TEXT EXTRACTED ---");
            // System.out.println(rawText); // Uncomment nếu muốn debug

            // 2. AI Phân tích
            GeminiResponse analysisResult = geminiService.parseCV(rawText);

            // 3. Trả kết quả
            return ResponseEntity.ok(analysisResult);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi xử lý AI: " + e.getMessage());
        }
    }

    /**
     * API 2: Upload CV, Phân tích và LƯU vào hồ sơ người dùng
     * [FIX] Thêm tham số userId vì Profile phải gắn với User
     */
    @PostMapping("/upload-cv")
    public ResponseEntity<?> uploadCV(
            @RequestParam("userId") Long userId, // Bắt buộc phải biết ai đang nộp
            @RequestParam("file") MultipartFile file) {
        try {
            // Gọi CandidateService để xử lý toàn bộ (Tách chữ -> AI -> Map Entity -> Save DB)
            CandidateProfile savedProfile = candidateService.uploadAndAnalyzeCV(userId, file);
            
            return ResponseEntity.ok(savedProfile);
            
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console server để dễ debug
            return ResponseEntity.badRequest().body("Lỗi lưu hồ sơ: " + e.getMessage());
        }
    }
}