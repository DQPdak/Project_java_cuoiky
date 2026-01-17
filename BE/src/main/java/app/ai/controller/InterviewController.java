package app.ai.controller;

import app.ai.dto.InterviewDTO;
import app.ai.models.InterviewMessage;
import app.ai.models.InterviewSession;
import app.ai.service.InterviewService;
import app.auth.dto.response.MessageResponse;
import app.auth.model.User;
import app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final SecurityUtils securityUtils;

    // --- HÀM CHUYỂN ĐỔI (MAPPER) CÓ CỜ KIỂM SOÁT ---
    private InterviewDTO convertToDTO(InterviewSession session, boolean includeMessages) {
        InterviewDTO.InterviewDTOBuilder builder = InterviewDTO.builder()
                .id(session.getId())
                .status(session.getStatus())
                .score(session.getFinalScore())
                .feedback(session.getFeedback())
                .createdAt(session.getCreatedAt())
                // Lấy thông tin Job an toàn (Check null kỹ càng)
                .jobId(session.getJobPosting().getId())
                .jobTitle(session.getJobPosting().getTitle())
                .companyName(
                    (session.getJobPosting().getCompany() != null) 
                    ? session.getJobPosting().getCompany().getName() 
                    : "Chưa cập nhật"
                )
                // Lấy thông tin User an toàn
                .candidateId(session.getUser().getId())
                .candidateName(session.getUser().getFullName());

        // 👇 CHỈ LOAD MESSAGES KHI CẦN THIẾT (True)
        // Giúp tránh lỗi Lazy Load khi xem danh sách lịch sử
        if (includeMessages && session.getMessages() != null) {
            builder.messages(session.getMessages().stream().map(msg -> 
                InterviewDTO.MessageDTO.builder()
                    .sender(msg.getSender())
                    .content(msg.getContent())
                    .sentAt(msg.getSentAt()) // Hoặc msg.getCreatedAt() tùy entity của bạn
                    .build()
            ).collect(Collectors.toList()));
        } else {
            builder.messages(null); // Không tải tin nhắn
        }

        return builder.build();
    }

    // --- 1. LẤY LỊCH SỬ (Không tải tin nhắn -> Fix lỗi Lazy) ---
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam Long jobId) {
        try {
            User user = securityUtils.getCurrentUser();
            List<InterviewSession> history = interviewService.getHistory(jobId, user.getId());
            
            // 👇 QUAN TRỌNG: Truyền FALSE để không kích hoạt load tin nhắn
            List<InterviewDTO> dtos = history.stream()
                                             .map(s -> convertToDTO(s, false)) 
                                             .collect(Collectors.toList());
                                             
            return ResponseEntity.ok(MessageResponse.success("Lấy lịch sử thành công", dtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    // --- 2. CÁC API CÒN LẠI (Vẫn tải tin nhắn bình thường) ---

    @PostMapping("/start")
    public ResponseEntity<?> startInterview(@RequestBody Map<String, Long> request) {
        try {
            Long jobId = request.get("jobId");
            User user = securityUtils.getCurrentUser();
            InterviewSession session = interviewService.startInterview(user.getId(), jobId);
            
            // Start xong thì cần hiện tin chào mừng -> Truyền TRUE
            return ResponseEntity.ok(MessageResponse.success("Bắt đầu thành công", convertToDTO(session, true)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/chat")
    public ResponseEntity<?> chat(@PathVariable Long sessionId, @RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            InterviewMessage reply = interviewService.processUserMessage(sessionId, message);
            return ResponseEntity.ok(MessageResponse.success("Gửi tin thành công", reply)); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> endInterview(@PathVariable Long sessionId) {
        try {
            InterviewSession result = interviewService.endInterview(sessionId);
            // Kết thúc thì trả về full để xem lại -> Truyền TRUE
            return ResponseEntity.ok(MessageResponse.success("Kết thúc phỏng vấn", convertToDTO(result, true)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSessionDetail(@PathVariable Long sessionId) {
        try {
            InterviewSession session = interviewService.getSessionDetail(sessionId);
            // Check quyền user...
            if (!session.getUser().getId().equals(securityUtils.getCurrentUser().getId())) {
                 return ResponseEntity.status(403).body(MessageResponse.error("Không có quyền truy cập"));
            }

            // Xem chi tiết thì bắt buộc phải có tin nhắn -> Truyền TRUE
            return ResponseEntity.ok(MessageResponse.success("Lấy chi tiết thành công", convertToDTO(session, true)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }
}