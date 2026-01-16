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
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("#{'${gemini.api.keys}'.split(',')}")
    private List<String> apiKeys;

    //Biến đếm an toàn luồng (Thread-safe) để xoay vòng key
    private final AtomicInteger keyIndex = new AtomicInteger(0);
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";
    private String getRotatedKey() {
        if (apiKeys == null || apiKeys.isEmpty()) {
            throw new RuntimeException("Không tìm thấy API Key nào trong cấu hình!");
        }
        // Lấy chỉ số hiện tại, tăng lên 1, rồi chia lấy dư cho tổng số key
        int index = keyIndex.getAndIncrement() % apiKeys.size();
        // Xử lý trường hợp số âm (dù ít khi xảy ra với AtomicInteger dương)
        if (index < 0) index = Math.abs(index);
        
        String selectedKey = apiKeys.get(index);
        log.info("Đang sử dụng Key thứ {}/{} : ...{}", (index + 1), apiKeys.size(), selectedKey.substring(selectedKey.length() - 4));
        return selectedKey;
    }
    /**
     * Gửi Prompt lên Google Gemini và nhận về chuỗi JSON thô đã làm sạch.
     */
    public String generateContent(String promptText) {
        // Tự động thử lại (Retry) nếu gặp lỗi 429 (Too Many Requests)
        // Đây là "bí thuật" giúp đồ án không bị chết khi đang demo
        int maxRetries = 3; 
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                return callGeminiApi(promptText);
            } catch (Exception e) {
                attempt++;
                log.warn("Lần thử {} thất bại: {}. Đang thử key khác...", attempt, e.getMessage());
                
                // Nếu đã thử hết số lần cho phép thì ném lỗi ra ngoài
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Đã thử " + maxRetries + " key khác nhau nhưng vẫn thất bại. Lỗi cuối cùng: " + e.getMessage());
                }
            }
        }
        return null; // Should not reach here
    }

    // Tách logic gọi API ra riêng để dễ quản lý
    private String callGeminiApi(String promptText) throws Exception {
        // 1. Lấy Key xoay vòng
        String currentKey = getRotatedKey();
        
        // 2. Tạo Body Request
        Map<String, Object> contentPart = new HashMap<>();
        contentPart.put("text", promptText);
        
        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", List.of(contentPart));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(parts));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 3. Gọi API với Key vừa lấy
        String url = GEMINI_API_URL + currentKey;
        
        // Lưu ý: restTemplate ném exception nếu HTTP code là 4xx, 5xx
        // Nên nó sẽ nhảy xuống catch ở hàm generateContent để retry
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw new RuntimeException("Gemini API trả về response rỗng");
        }

        // 4. Parse JSON
        var jsonNode = objectMapper.readTree(response.getBody());
        
        if (jsonNode.path("candidates").isEmpty() || !jsonNode.path("candidates").has(0)) {
            // Check lỗi specific từ Google trả về nếu có
            throw new RuntimeException("Gemini API response không hợp lệ (Block/Filter)");
        }
        
        var candidate = jsonNode.path("candidates").get(0);
        if (!candidate.has("content")) {
             throw new RuntimeException("Gemini không trả về nội dung (có thể do safety settings)");
        }
        
        String aiTextResponse = candidate.path("content").path("parts").get(0).path("text").asText();
        
        return aiTextResponse.replaceAll("```json", "")
                             .replaceAll("```", "")
                             .trim();
    }
}