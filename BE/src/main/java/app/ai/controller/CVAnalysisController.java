package app.ai.controller;

import app.ai.models.Candidate;
import app.ai.service.CVProcessingService;
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
    private final CVProcessingService cvProcessingService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCV(@RequestParam("file") MultipartFile file) {
        try {
            // 1. "Mắt" đọc chữ từ file
            String rawText = cvTextExtractor.extractText(file);
            System.out.println("--- TEXT TRÍCH XUẤT ĐƯỢC ---");
            System.out.println(rawText); // In ra console để debug xem chữ có sạch không

            // 2. "Não" phân tích dữ liệu
            GeminiResponse analysisResult = geminiService.parseCV(rawText);

            // 3. Trả về kết quả JSON
            return ResponseEntity.ok(analysisResult);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi xử lý: " + e.getMessage());
        }
    }

    @PostMapping("/upload-cv") // Đổi tên endpoint cho đúng nghiệp vụ
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file) {
        try {
            Candidate savedCandidate = cvProcessingService.processAndSaveCV(file);
            return ResponseEntity.ok("Lưu thành công ứng viên ID: " + savedCandidate.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}