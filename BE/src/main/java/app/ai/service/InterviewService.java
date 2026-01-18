package app.ai.service;

import app.ai.models.InterviewMessage;
import app.ai.models.InterviewSession;
import app.ai.repository.IInterviewMessageRepository;
import app.ai.repository.IInterviewSessionRepository;
import app.ai.service.cv.gemini.GeminiService;
import app.ai.service.prompt.InterviewPromptBuilder;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.recruitment.entity.JobPosting;
import app.recruitment.repository.JobPostingRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List; // Đã thêm import List
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final IInterviewSessionRepository sessionRepository;
    private final IInterviewMessageRepository messageRepository;
    private final JobPostingRepository jobRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository profileRepository;

    private final GeminiService geminiService;
    private final InterviewPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    // --- 1. BẮT ĐẦU PHỎNG VẤN ---
    @Transactional
    public InterviewSession startInterview(Long userId, Long jobId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        JobPosting job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Tạo Session mới
        InterviewSession session = InterviewSession.builder()
                .user(user)
                .jobPosting(job)
                .status("ONGOING")
                .messages(new ArrayList<>())
                .build();
        sessionRepository.save(session);

        // Lấy tên ứng viên để AI chào
        String candidateName = "Ứng viên";
        CandidateProfile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile != null && profile.getFullName() != null) {
            candidateName = profile.getFullName();
        }

        // Tạo Prompt bắt đầu
        String companyName = (job.getCompany() != null) ? job.getCompany().getName() : "Công ty Công nghệ";
        String prompt = promptBuilder.buildStartPrompt(companyName, job.getTitle(), candidateName);

        // Gọi AI lấy lời chào
        String aiGreeting = geminiService.callAiChat(prompt);

        // Lưu và trả về
        saveMessage(session, "AI", aiGreeting);
        return session;
    }

    // --- 2. GỬI TIN NHẮN (CHAT) ---
    @Transactional
    public InterviewMessage processUserMessage(Long sessionId, String userContent) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if ("COMPLETED".equalsIgnoreCase(session.getStatus())) {
            throw new RuntimeException("Buổi phỏng vấn đã kết thúc.");
        }

        // A. Lưu tin nhắn User
        saveMessage(session, "USER", userContent);

        // B. Tạo Prompt kèm lịch sử chat
        String prompt = promptBuilder.buildChatPrompt(session.getJobPosting(), session.getMessages());

        // C. Gọi AI
        String aiReply = geminiService.callAiChat(prompt);

        // D. Lưu tin nhắn AI
        return saveMessage(session, "AI", aiReply);
    }

    // --- 3. KẾT THÚC & CHẤM ĐIỂM ---
    @Transactional
    public InterviewSession endInterview(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // A. Tạo Prompt chấm điểm
        String gradingPrompt = promptBuilder.buildGradingPrompt(session.getMessages());

        // B. Gọi AI
        String resultJson = geminiService.callAiChat(gradingPrompt);

        // C. Parse kết quả JSON
        try {
            // Làm sạch JSON nếu AI lỡ thêm Markdown (```json ... ```)
            if (resultJson.contains("```json")) {
                resultJson = resultJson.replace("```json", "").replace("```", "").trim();
            } else if (resultJson.contains("```")) {
                 resultJson = resultJson.replace("```", "").trim();
            }

            Map<String, Object> map = objectMapper.readValue(resultJson, new TypeReference<Map<String, Object>>(){});

            session.setFinalScore((Integer) map.get("score"));
            session.setFeedback((String) map.get("feedback"));
            session.setStatus("COMPLETED");

        } catch (Exception e) {
            log.error("Lỗi chấm điểm phỏng vấn: ", e);
            // Fallback nếu lỗi parse JSON
            session.setFinalScore(0);
            session.setFeedback("Hệ thống gặp lỗi khi chấm điểm. Vui lòng thử lại sau.");
            session.setStatus("COMPLETED");
        }

        return sessionRepository.save(session);
    }

    // --- 4. LẤY DỮ LIỆU (GET) ---

    // Lấy chi tiết cuộc trò chuyện (để xem lại lịch sử chat)
    @Transactional(readOnly = true)
    public InterviewSession getSessionDetail(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên phỏng vấn"));
        
        // [FIX LỖI] "Chạm" vào danh sách để ép Hibernate tải dữ liệu ngay lập tức
        if (session.getMessages() != null) {
            session.getMessages().size(); 
        }
        
        return session;
    }

    // Lấy danh sách lịch sử phỏng vấn CỦA 1 JOB CỤ THỂ (Update mới)
    public List<InterviewSession> getHistory(Long jobId, Long userId) {
        // Gọi hàm tìm kiếm theo cả User và Job (để hiển thị đúng trong trang chi tiết Job)
        return sessionRepository.findByUserIdAndJobPostingIdOrderByCreatedAtDesc(userId, jobId);
    }

    // --- Helper ---
    private InterviewMessage saveMessage(InterviewSession session, String sender, String content) {
        InterviewMessage msg = InterviewMessage.builder()
                .session(session)
                .sender(sender)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build();

        // Cần add vào list của session để đồng bộ Hibernate cache trong cùng transaction
        if (session.getMessages() == null) session.setMessages(new ArrayList<>());
        session.getMessages().add(msg);

        return messageRepository.save(msg);
    }
}