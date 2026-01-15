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

    public List<String> extractSkillsFromJob(String jobDescription, String jobRequirements) {
       String prompt = """
                Bạn là một Hệ thống Chấm điểm Tuyển dụng Tự động (Automated Scoring System).
                Nhiệm vụ: So sánh 'CV Ứng viên' với 'Yêu cầu Công việc' (Job Requirements) theo quy tắc toán học tuyệt đối.

                --- QUY TẮC NGÔN NGỮ (LANGUAGE RULES) ---
                1. **DỮ LIỆU TRÍCH XUẤT (Lists)**: GIỮ NGUYÊN ngôn ngữ gốc. 
                   - Nếu Job viết "Java", output là "Java". 
                   - Nếu CV viết "Lập trình viên", output là "Lập trình viên". 
                   - KHÔNG ĐƯỢC DỊCH thuật ngữ.
                2. **NHẬN XÉT (Evaluation)**: BẮT BUỘC viết bằng **TIẾNG VIỆT**.

                --- QUY TẮC TÍNH ĐIỂM (SCORING LOGIC) ---
                Điểm số (matchPercentage) dựa trên 2 yếu tố:
                1. **Kỹ năng (70%)**: Chỉ tính dựa trên các kỹ năng ĐƯỢC VIẾT RÕ RÀNG trong 'JOB REQUIREMENTS'.
                   - Công thức: (Số kỹ năng trùng khớp / Tổng số kỹ năng Job yêu cầu) * 70.
                   - TUYỆT ĐỐI KHÔNG trừ điểm vì thiếu kỹ năng mà Job không yêu cầu (ví dụ: Job không ghi Kafka thì không được trừ điểm vì thiếu Kafka).
                   - TUYỆT ĐỐI KHÔNG cộng điểm cho kỹ năng thừa (Extra skills).
                2. **Kinh nghiệm (30%)**:
                   - Đọc số năm kinh nghiệm yêu cầu trong Job và số năm trong CV.
                   - Nếu CV >= Job: 30 điểm.
                   - Nếu CV < Job: Tính tỷ lệ (CV/Job) * 30.

                --- YÊU CẦU ĐẦU RA (JSON FORMAT) ---
                Hãy trả về JSON theo cấu trúc sau (không Markdown) với ngôn ngữ là "TIẾNG VIỆT":
                {
                  "matchPercentage": (Integer: Tổng điểm Skill + Kinh nghiệm, Max 100),
                  
                  "totalRequiredSkills": (Integer: Tổng số skill tìm thấy trong Job Requirements),
                  
                  "matchedSkillsCount": (Integer),
                  "matchedSkillsList": ["Danh sách skill có trong cả Job và CV (Giữ nguyên gốc)"],
                  
                  "missingSkillsCount": (Integer),
                  "missingSkillsList": ["Danh sách skill có trong Job nhưng CV thiếu (Giữ nguyên gốc)"],
                  
                  "extraSkillsCount": (Integer),
                  "extraSkillsList": ["Danh sách skill CV có nhưng Job KHÔNG yêu cầu (Giữ nguyên gốc)"],
                  
                  "evaluation": "Viết 3 đoạn văn ngắn bằng TIẾNG VIỆT:\n1. [Yêu cầu bắt buộc]: Liệt kê các skill trong missingSkillsList mà ứng viên CẦN học ngay để đáp ứng công việc.\n2. [Lời khuyên nâng cao]: Gợi ý các skill theo chuẩn ngành mà Job KHÔNG yêu cầu nhưng nên học để CV đẹp hơn (Ví dụ: CI/CD, Cloud...).\n3. [Lộ trình]: Đưa ra lời khuyên ngắn gọn về hướng phát triển."
                }

                --- DỮ LIỆU ĐẦU VÀO ---
                [JOB DESCRIPTION]
                %s

                [JOB REQUIREMENTS (NGUỒN CHUẨN ĐỂ SO SÁNH)]
                %s

                [CANDIDATE CV]
                %s
                """.formatted(jobDescription, jobRequirements);

        try {
            // Gọi hàm raw để lấy chuỗi JSON (ví dụ: "[\"Java\", \"Spring\"]")
            String jsonString = geminiApiClient.generateContent(prompt);
            
            // Parse chuỗi JSON thành List<String>
           return objectMapper.readValue(jsonString, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            log.error("Lỗi tách skill từ Job: ", e);
            return Collections.emptyList();
        }
    }

    /**
     * CHỨC NĂNG 2: So sánh CV (Raw Text) với Job (Dùng cho Flow Chi Tiết / Preview)
     * [MỚI] Hàm này được thêm vào để phục vụ JobMatchingService.matchCandidateWithJobAI
     */
    public MatchResult matchCVWithJob(String cvText, String jobDescription, String jobRequirements) {
       String prompt = """
                You are a strict Recruitment AI. Compare Candidate CV vs Job Description.

                --- CRITICAL RULES ---
                1. **STRICT SCORING**: 
                   - Only count a skill as "Missing" if it is EXPLICITLY written in 'JOB REQUIREMENTS'.
                   - If Candidate has all explicit skills -> Score = 100 (Do not deduct points for skills not listed).
                2. **LANGUAGE**: The value of "evaluation" field MUST be in **VIETNAMESE** (Tiếng Việt).
                3. **RECOMMENDATION**: Identify skills NOT in JD but good for the role, put them in 'recommendedSkills'.

                --- INPUT DATA ---
                JOB DESCRIPTION:
                %s
                
                JOB REQUIREMENTS (Truth Source):
                %s
                
                CANDIDATE CV:
                %s
                
                --- OUTPUT JSON FORMAT ---
                {
                  "matchPercentage": (0-100),
                  "matchedSkillsCount": (int),
                  "missingSkillsCount": (int),
                  "totalRequiredSkills": (int),
                  "missingSkillsList": ["Skill A", "Skill B"],
                  "recommendedSkills": ["Skill X (Nên học thêm)", "Skill Y"],
                  "evaluation": "Viết nhận xét ngắn gọn bằng Tiếng Việt. Ví dụ: 'Ứng viên đáp ứng đủ yêu cầu bắt buộc. Tuy nhiên để CV ấn tượng hơn, bạn nên bổ sung kiến thức về...'"
                }
                """.formatted(jobDescription, jobRequirements, cvText);

       return parseResponse(prompt, MatchResult.class);
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

        return parseResponse(prompt, MatchResult.class);
    }

    public CareerAdviceResult suggestCareerPath(String candidateData, String jobData) {

        String prompt = """
                Bạn là một Mentor Công nghệ tâm huyết. 
                Dựa trên CV của ứng viên và Job Description, hãy xây dựng lộ trình phát triển bản thân cho họ.

                JOB: %s
                CANDIDATE: %s

                YÊU CẦU ĐẦU RA (JSON FORMAT ONLY):
                Hãy trả về JSON theo cấu trúc sau (không Markdown):
                {
                    "extraSkills": ["Skill A", "Skill B"],
                    "recommendedSkills": ["Skill X", "Skill Y"],
                    "estimatedDuration": "Khoảng thời gian (ví dụ: 1 tháng)",
                    "learningPath": "Viết một lộ trình học tập chi tiết dạng Markdown. Chia theo tuần. Tập trung lấp đầy các kỹ năng còn thiếu (Missing Skills) và học thêm các kỹ năng gợi ý (Recommended Skills). Giọng văn khích lệ, tích cực.",
                    "careerAdvice": "Lời khuyên ngắn gọn về định hướng sự nghiệp."
                }
                """.formatted(jobData, candidateData);

       return parseResponse(prompt, CareerAdviceResult.class);
    }
    // --- HÀM HELPER GỌI API (Private) ---

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