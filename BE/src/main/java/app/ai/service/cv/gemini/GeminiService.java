package app.ai.service.cv.gemini;

import app.ai.service.cv.gemini.dto.GeminiResponse;
import app.ai.service.cv.gemini.dto.MatchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}") // Lấy key từ application.properties
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";

    /**
     * CHỨC NĂNG 1: Phân tích CV (Raw Text -> JSON Profile)
     */
    public GeminiResponse parseCV(String rawText) {
        String prompt = """
                Bạn là một trợ lý nhân sự chuyên nghiệp (HR Assistant).
                Nhiệm vụ: Trích xuất thông tin từ văn bản CV dưới đây thành định dạng JSON chuẩn.
                
                NỘI DUNG CV:
                %s
                
                YÊU CẦU ĐẦU RA (JSON FORMAT ONLY):
                {
                  "contact": {
                    "name": "Họ tên đầy đủ",
                    "email": "Email",
                    "phoneNumber": "Số điện thoại",
                    "address": "Địa chỉ (nếu có)",
                    "linkedIn": "Link LinkedIn (nếu có)"
                  },
                  "skills": ["Kỹ năng A", "Kỹ năng B", ...],
                  "experiences": [
                    {
                      "company": "Tên công ty",
                      "role": "Vị trí",
                      "startDate": "dd/MM/yyyy hoặc MM/yyyy",
                      "endDate": "dd/MM/yyyy hoặc Present",
                      "description": "Mô tả công việc"
                    }
                  ]
                }
                Chỉ trả về JSON, không kèm lời dẫn.
                """.formatted(rawText);

        return callGemini(prompt, GeminiResponse.class);
    }

    /**
     * CHỨC NĂNG 2: So khớp Job & CV (Matching -> MatchResult)
     * [UPDATE] Đã thêm logic đếm kỹ năng (Matched/Missing/Extra)
     */
    public MatchResult matchJob(String candidateData, String jobData) {
        
        String prompt = """
                Bạn là một chuyên gia tuyển dụng (AI Recruiter). 
                Nhiệm vụ: So sánh năng lực Ứng viên với Yêu cầu Công việc và đưa ra các chỉ số thống kê chính xác.

                DỮ LIỆU CÔNG VIỆC (JOB):
                %s

                DỮ LIỆU ỨNG VIÊN (CANDIDATE):
                %s

                HÃY THỰC HIỆN CÁC BƯỚC TÍNH TOÁN SAU:
                1. Xác định danh sách 'Required Skills' (Kỹ năng bắt buộc) từ Job. Đếm tổng số lượng -> 'totalRequiredSkills'.
                2. Đối chiếu với kỹ năng của Ứng viên:
                   - 'matchedSkillsCount': Số lượng kỹ năng ứng viên CÓ TRONG danh sách yêu cầu.
                   - 'missingSkillsCount': Số lượng kỹ năng yêu cầu mà ứng viên KHÔNG CÓ.
                   - 'extraSkillsCount': Số lượng kỹ năng ứng viên có nhưng Job KHÔNG yêu cầu.
                3. 'matchPercentage': Tính điểm phần trăm độ phù hợp (0-100) dựa trên trọng số: Kỹ năng (60%%), Kinh nghiệm (30%%), Học vấn/Khác (10%%).

                YÊU CẦU ĐẦU RA (JSON FORMAT ONLY):
                {
                    "matchPercentage": (int 0-100),
                    "matchedSkillsCount": (int),
                    "missingSkillsCount": (int),
                    "extraSkillsCount": (int),
                    "totalRequiredSkills": (int),
                    "missingSkillsList": ["Skill A", "Skill B"],
                    "evaluation": "Nhận xét ngắn gọn (tiếng Việt) khoảng 3 câu về điểm mạnh/yếu."
                }
                Chỉ trả về JSON thuần túy.
                """.formatted(jobData, candidateData);

        return callGemini(prompt, MatchResult.class);
    }

    // --- HÀM HELPER GỌI API (Private) ---
    private <T> T callGemini(String prompt, Class<T> responseType) {
        try {
            // 1. Tạo Body request theo chuẩn Gemini API
            Map<String, Object> contentPart = new HashMap<>();
            contentPart.put("text", prompt);

            Map<String, Object> parts = new HashMap<>();
            parts.put("parts", List.of(contentPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(parts));

            // 2. Tạo Header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. Gửi Request POST
            String url = GEMINI_API_URL + apiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // 4. Parse Response của Gemini để lấy phần text
            // Cấu trúc trả về: candidates[0].content.parts[0].text
            String rawJson = response.getBody();
            var jsonNode = objectMapper.readTree(rawJson);
            String aiTextResponse = jsonNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // 5. Làm sạch chuỗi JSON (xóa ```json và ``` nếu có)
            String cleanJson = aiTextResponse.replaceAll("```json", "")
                                             .replaceAll("```", "")
                                             .trim();

            // 6. Map JSON clean vào Object đích (GeminiResponse hoặc MatchResult)
            return objectMapper.readValue(cleanJson, responseType);

        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini API: ", e);
            // Trả về null hoặc object rỗng tùy logic business của bạn
            throw new RuntimeException("AI processing failed: " + e.getMessage());
        }
    }
}