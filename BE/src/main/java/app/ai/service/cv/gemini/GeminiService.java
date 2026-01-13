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
        
        // Prompt cho model 
       String prompt = """
                Bạn là một chuyên gia tuyển dụng (AI Recruiter) và chuyên gia xử lý dữ liệu.
                
                NHIỆM VỤ:
                Trích xuất thông tin từ văn bản CV thô bên dưới và trả về định dạng JSON chuẩn.
                
                 CẢNH BÁO VỀ DỮ LIỆU ĐẦU VÀO (RẤT QUAN TRỌNG):
                1. Văn bản này được trích xuất tự động từ PDF/DOCX nên có thể chứa lỗi định dạng nghiêm trọng.
                2. Lỗi dính chữ: Do mất dấu xuống dòng, tiêu đề có thể bị dính vào nội dung trước đó.
                   - Ví dụ: "Hồ Chí MinhKỸ NĂNG" -> Hãy hiểu là địa chỉ "Hồ Chí Minh" và bắt đầu mục "KỸ NĂNG".
                   - Ví dụ: "DeveloperKINH NGHIỆM" -> Hãy tách ra.
                3. Lỗi chia cột: Nếu CV chia 2 cột, nội dung có thể bị trộn lẫn. Hãy dựa vào ngữ cảnh để sắp xếp lại đúng logic.
                
                YÊU CẦU ĐẦU RA (JSON FORMAT):
                Chỉ trả về duy nhất chuỗi JSON (không Markdown, không giải thích thêm), theo cấu trúc sau:
                {
                  "contact": {
                    "name": "Họ và tên đầy đủ của ứng viên",  <-- THÊM DÒNG NÀY
                    "email": "Email liên hệ",
                    "phoneNumber": "Số điện thoại",
                    "address": "Địa chỉ (nếu có)",
                    "linkedIn": "Link LinkedIn (nếu có)"
                },
                  "skills": ["Liệt kê các kỹ năng chuyên môn..."],
                  "experiences": [
                    {
                      "company": "Tên công ty",
                      "role": "Chức vụ",
                      "startDate": "Thời gian bắt đầu (ngắn gọn)",
                      "endDate": "Thời gian kết thúc (hoặc Present)",
                      "description": "Mô tả công việc (tóm tắt)"
                    }
                  ]
                }
                
                VĂN BẢN CV THÔ CẦN XỬ LÝ:
                -------------------------
                """ + rawText;

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