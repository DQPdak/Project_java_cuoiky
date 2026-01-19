package app.ai.service;



import app.ai.dto.InterviewChatRequest;
import app.ai.dto.InterviewMessage;
import app.ai.models.InterviewSession;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final IInterviewSessionRepository sessionRepository;
    private final JobPostingRepository jobRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository profileRepository;

    private final GeminiService geminiService;
    private final InterviewPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    // --- 1. KHỞI TẠO SESSION ---
    @Transactional
    public InterviewSession startInterview(Long userId, Long jobId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        JobPosting job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Tạo Session mới (Chỉ để track trạng thái và lưu điểm sau này)
        InterviewSession session = InterviewSession.builder()
                .user(user)
                .jobPosting(job)
                .status("ONGOING")
                .build();
        return sessionRepository.save(session);
    }
    
    // Hàm phụ trợ để lấy lời chào (không lưu DB)
    public String getInitialGreeting(Long userId, Long jobId) {
        JobPosting job = jobRepository.findById(jobId).orElseThrow();
        String candidateName = getCandidateName(userId);
        String companyName = (job.getCompany() != null) ? job.getCompany().getName() : "Công ty";

        String prompt = promptBuilder.buildStartPrompt(companyName, job.getTitle(), candidateName);
        return geminiService.callAiChat(prompt);
    }

    // --- 2. XỬ LÝ CHAT (STATELESS) ---
    // Nhận: SessionID, Tin mới, Lịch sử cũ
    // Trả về: Câu trả lời của AI
    public String chat(Long sessionId, String newMessage, List<InterviewChatRequest.MessageItem> historyDtos) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if ("COMPLETED".equalsIgnoreCase(session.getStatus())) {
            return "Phiên phỏng vấn đã kết thúc.";
        }

        // 1. Convert DTO history sang List<InterviewMessage> (POJO) để dùng cho PromptBuilder
        List<InterviewMessage> context = new ArrayList<>();
        if (historyDtos != null) {
            context = historyDtos.stream()
                    .map(dto -> new InterviewMessage(dto.getSender(), dto.getContent()))
                    .collect(Collectors.toList());
        }
        
        // 2. Thêm tin nhắn mới nhất vào ngữ cảnh (RAM only)
        context.add(new InterviewMessage("USER", newMessage));

        // 3. Build Prompt & Gọi AI
        String prompt = promptBuilder.buildChatPrompt(session.getJobPosting(), context);
        return geminiService.callAiChat(prompt);
    }

    // --- 3. KẾT THÚC & CHẤM ĐIỂM ---
    @Transactional
    public InterviewSession endInterview(Long sessionId, List<InterviewChatRequest.MessageItem> fullHistoryDtos) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 1. Convert DTO sang POJO để chấm điểm
        List<InterviewMessage> fullContext = new ArrayList<>();
        if (fullHistoryDtos != null) {
            fullContext = fullHistoryDtos.stream()
                    .map(dto -> new InterviewMessage(dto.getSender(), dto.getContent()))
                    .collect(Collectors.toList());
        }

        // 2. Gọi AI chấm điểm
        String gradingPrompt = promptBuilder.buildGradingPrompt(fullContext);
        String resultJson = geminiService.callAiChat(gradingPrompt);

        // 3. Parse JSON & Lưu vào Session
        try {
            if (resultJson.contains("```json")) {
                resultJson = resultJson.replace("```json", "").replace("```", "").trim();
            } else if (resultJson.contains("```")) {
                 resultJson = resultJson.replace("```", "").trim();
            }
            Map<String, Object> map = objectMapper.readValue(resultJson, new TypeReference<Map<String, Object>>(){});

            Number scoreNum = (Number) map.get("score");
            session.setFinalScore(scoreNum != null ? scoreNum.intValue() : 0);
            session.setFeedback((String) map.get("feedback"));
            session.setStatus("COMPLETED");

        } catch (Exception e) {
            log.error("Lỗi chấm điểm: ", e);
            session.setFinalScore(0);
            session.setFeedback("Lỗi khi chấm điểm.");
            session.setStatus("COMPLETED");
        }

        // Chỉ lưu Session (Score, Feedback), không lưu messages
        return sessionRepository.save(session);
    }

    // --- Helper ---
    public List<InterviewSession> getCompletedHistory(Long jobId, Long userId) {
        return sessionRepository.findByUserIdAndJobPostingIdAndStatusOrderByCreatedAtDesc(
            userId, 
            jobId, 
            "COMPLETED" // 👈 Chỉ lấy trạng thái này
        );
    }
    
    public InterviewSession getSessionById(Long id) {
        return sessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }

    private String getCandidateName(Long userId) {
        CandidateProfile profile = profileRepository.findByUserId(userId).orElse(null);
        return (profile != null && profile.getFullName() != null) ? profile.getFullName() : "Ứng viên";
    }
}