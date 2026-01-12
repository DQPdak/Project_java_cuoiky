package app.ai.service.cv;

import app.ai.service.cv.extractortext.CVTextExtractor;
import app.ai.service.cv.gemini.GeminiService; // Import lại Gemini
import app.ai.service.cv.gemini.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CVAnalysisService {

    private final CVTextExtractor textExtractor;
    private final GeminiService geminiService; // Dùng lại Gemini

    public GeminiResponse analyzeCV(MultipartFile file) {
        // 1. Lấy chữ
        String rawText = textExtractor.extractText(file);

        // 2. Gọi Gemini (Miễn phí)
        return geminiService.parseCV(rawText);
    }
}