package app.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.ai.service.cv.gemini.*;
import app.ai.service.cv.gemini.dto.GeminiResponse;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestGeminiController {

    private final GeminiService geminiService;

    @GetMapping("/ai")
    public ResponseEntity<?> testGemini() {
        // Giả lập một đoạn text CV ngắn để test
        String mockCVText = """
            Nguyễn Văn A
            Email: anguyen@gmail.com | SĐT: 0909123456
            
            KINH NGHIỆM LÀM VIỆC
            Công ty ABC Tech
            Java Backend Developer
            01/2020 - Hiện tại
            - Phát triển hệ thống Microservices dùng Spring Boot.
            
            KỸ NĂNG
            Java, Spring Boot, SQL, Docker
            """;

        System.out.println("Dang goi Gemini API...");
        GeminiResponse result = geminiService.parseCV(mockCVText);
        
        return ResponseEntity.ok(result);
    }
}
