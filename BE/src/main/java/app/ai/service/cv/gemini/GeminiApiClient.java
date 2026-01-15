package app.ai.service.cv.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";

    /**
     * Gửi Prompt lên Google Gemini và nhận về chuỗi JSON thô đã làm sạch.
     */
    public String generateContent(String promptText) {
        try {
            // 1. Tạo Body Request
            Map<String, Object> contentPart = new HashMap<>();
            contentPart.put("text", promptText);
            
            Map<String, Object> parts = new HashMap<>();
            parts.put("parts", List.of(contentPart));
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(parts));

            // 2. Tạo Header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. Gọi API
            String url = GEMINI_API_URL + apiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // 4. Lấy phần text trả lời từ cấu trúc phức tạp của Google
            var jsonNode = objectMapper.readTree(response.getBody());
            String aiTextResponse = jsonNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // 5. Làm sạch Markdown (```json)
            return aiTextResponse.replaceAll("```json", "")
                                 .replaceAll("```", "")
                                 .trim();

        } catch (Exception e) {
            log.error("Lỗi kết nối Gemini API: ", e);
            throw new RuntimeException("Lỗi kết nối AI: " + e.getMessage());
        }
    }
}