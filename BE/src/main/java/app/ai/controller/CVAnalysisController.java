package app.ai.controller;

import app.candidate.dto.response.CandidateProfileResponse; // 👈 Import DTO
import app.candidate.service.CandidateService;
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

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCV(@RequestParam("file") MultipartFile file) {
        try {
            String rawText = cvTextExtractor.extractText(file);
            GeminiResponse analysisResult = geminiService.parseCV(rawText);
            return ResponseEntity.ok(analysisResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi xử lý AI: " + e.getMessage());
        }
    }
}