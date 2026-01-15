package app.ai.service.cv.gemini;

import app.ai.service.cv.gemini.dto.GeminiResponse;
import app.ai.service.cv.gemini.dto.MatchResult;
import app.ai.service.cv.gemini.dto.analysis.CareerAdviceResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final ObjectMapper objectMapper;
    private final GeminiApiClient geminiApiClient;

    @Value("${gemini.api.key}") 
    private String apiKey;

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

        return parseResponse(prompt, GeminiResponse.class);
    }

    /**
     * CHỨC NĂNG 2: Tách Skill từ Job Description
     */
    public List<String> extractSkillsFromJob(String jobDescription, String jobRequirements) {
       String prompt = """
                You are an expert Job Analyst. Extract technical and soft skills from the Job Description below.
                Return ONLY a JSON Array of strings (e.g., ["Java", "Teamwork", "SQL"]).
                Do not include generic words like "Experience", "Degree". Keep skills concise.
                JOB TITLE & DESCRIPTION:
                %s
                REQUIREMENTS:
                %s
                """.formatted(jobDescription, jobRequirements);

        try {
            String jsonString = geminiApiClient.generateContent(prompt);
           return objectMapper.readValue(jsonString, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            log.error("Lỗi tách skill từ Job: ", e);
            return Collections.emptyList();
        }
    }

    /**
     * CHỨC NĂNG 3: Chấm điểm & Gợi ý lộ trình (All-in-One)
     */
    public MatchResult matchCVWithJob(String cvText, String jobDescription, String jobRequirements) {
       String prompt = """
    Bạn là một Hệ thống Chấm điểm Tuyển dụng Tự động. Nhiệm vụ: So sánh CV với Job Requirements.

    --- QUY TẮC BẮT BUỘC ---
    1. **NGÔN NGỮ**: Các trường 'evaluation', 'learningPath', 'careerAdvice' BẮT BUỘC viết bằng **TIẾNG VIỆT**.
    2. **DANH SÁCH**: Các trường List (matchedSkillsList...) không được để null. Nếu rỗng thì để [].
    3. **TÍNH ĐIỂM**:
       - Kỹ năng (70%%): (Trùng khớp / Tổng yêu cầu) * 70.
       - Kinh nghiệm (30%%): So sánh số năm kinh nghiệm.
       - KHÔNG trừ điểm nếu thiếu kỹ năng mà Job KHÔNG yêu cầu.

    --- CẤU TRÚC JSON MẪU (BẮT BUỘC ĐIỀN ĐỦ) ---
    Hãy trả về JSON đúng cấu trúc sau:

    {
      "matchPercentage": (Integer 0-100),
      "totalRequiredSkills": (Integer),
      
      "matchedSkillsCount": (Integer),
      "matchedSkillsList": ["Skill A", "Skill B", "Skill C"],

      "missingSkillsCount": (Integer),
      "missingSkillsList": ["Skill D", "Skill E"],

      "extraSkillsCount": (Integer),
      "extraSkillsList": ["Skill F", "Skill G"],

      "evaluation": "Viết 3 đoạn văn ngắn bằng TIẾNG VIỆT nhận xét điểm mạnh và điểm yếu.",
      
      "learningPath": "Viết Lộ trình học tập chi tiết dạng Markdown bằng TIẾNG VIỆT. Chia theo tuần (Tuần 1, Tuần 2...). Tập trung vào missingSkillsList.",
      
      "careerAdvice": "Lời khuyên ngắn gọn bằng TIẾNG VIỆT về thái độ và định hướng."
    }

    --- DỮ LIỆU ĐẦU VÀO ---
    [JOB DESCRIPTION]
    %s

    [JOB REQUIREMENTS]
    %s

    [CANDIDATE CV]
    %s
    """.formatted(jobDescription, jobRequirements, cvText);

       return parseResponse(prompt, MatchResult.class);
    }

    // --- HÀM HELPER ---
    private <T> T parseResponse(String prompt, Class<T> responseType) {
        try {
            String jsonResponse = geminiApiClient.generateContent(prompt);
            return objectMapper.readValue(jsonResponse, responseType);
        } catch (Exception e) {
            log.error("Lỗi parse dữ liệu AI: ", e);
            throw new RuntimeException("AI Error: " + e.getMessage());
        }
    }
}