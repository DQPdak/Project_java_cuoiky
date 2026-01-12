package app.ai.service.cv.gemini;

import app.ai.service.cv.gemini.dto.GeminiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";
    public GeminiResponse parseCV(String rawText) {
        // Xử lý key: Xóa khoảng trắng thừa nếu có
        String cleanKey = (apiKey != null) ? apiKey.trim() : "";
        String url = API_URL + cleanKey;
        
        // Prompt cho model đời cũ (gemini-pro) cần rõ ràng hơn
        String prompt = """
            Bạn là chuyên gia đọc CV. Hãy trích xuất thông tin từ văn bản dưới đây và trả về JSON.
            
            QUY TẮC BẮT BUỘC:
            1. Chỉ trả về JSON thuần túy, không dùng Markdown (không dùng ```json).
            2. Nếu không tìm thấy thông tin, để null hoặc rỗng.
            
            CẤU TRÚC JSON MONG MUỐN:
            {
              "contact": { "email": "string", "phoneNumber": "string" },
              "experiences": [ 
                { "company": "string", "role": "string", "startDate": "YYYY-MM", "endDate": "YYYY-MM" } 
              ],
              "skills": [ "string", "string" ]
            }
            
            VĂN BẢN CV:
            %s
            """.formatted(rawText);

        Map<String, Object> request = Map.of("contents", new Object[]{
            Map.of("parts", new Object[]{ Map.of("text", prompt) })
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // Gọi API
            JsonNode rootNode = restTemplate.postForObject(url, new HttpEntity<>(request, headers), JsonNode.class);
            
            // Log kết quả thô để debug nếu cần
            // System.out.println("Gemini Raw Response: " + rootNode.toPrettyString());

            String jsonText = extractTextFromJsonNode(rootNode);
            return objectMapper.readValue(jsonText, GeminiResponse.class);
            
        } catch (Exception e) {
            log.error("Lỗi Gemini API (Model: gemini-1.5-flash): {}", e.getMessage());
            // Mẹo: In chi tiết lỗi ra console để bạn dễ nhìn thấy
            e.printStackTrace(); 
            return new GeminiResponse();
        }
    }

    private String extractTextFromJsonNode(JsonNode rootNode) {
        if (rootNode == null) return "{}";
        
        // Kiểm tra lỗi từ Google trả về (nếu có)
        if (rootNode.has("error")) {
            String errorMsg = rootNode.path("error").path("message").asText();
            log.error("Google API trả về lỗi: {}", errorMsg);
            return "{}";
        }

        JsonNode textNode = rootNode.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text");

        if (textNode.isMissingNode()) {
            return "{}";
        }

        // Làm sạch chuỗi JSON (xóa markdown ```json nếu AI lỡ thêm vào)
        String raw = textNode.asText();
        if (raw.startsWith("```json")) {
            raw = raw.substring(7);
        }
        if (raw.startsWith("```")) {
            raw = raw.substring(3);
        }
        if (raw.endsWith("```")) {
            raw = raw.substring(0, raw.length() - 3);
        }
        return raw.trim();
    }
}