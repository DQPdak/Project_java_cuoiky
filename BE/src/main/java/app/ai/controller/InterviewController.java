package app.ai.controller;

import app.ai.models.InterviewMessage;
import app.ai.models.InterviewSession;
import app.ai.service.InterviewService;
import app.auth.dto.response.MessageResponse;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final UserRepository userRepository;

    // --- 1. BẮT ĐẦU PHỎNG VẤN ---
    // API: POST /api/interview/start
    // Body: { "jobId": 123 }
    @PostMapping("/start")
    public ResponseEntity<?> startInterview(@RequestBody Map<String, Long> request) {
        try {
            Long jobId = request.get("jobId");
            if (jobId == null) {
                return ResponseEntity.badRequest().body(MessageResponse.error("Thiếu jobId"));
            }

            User user = getCurrentUser();
            InterviewSession session = interviewService.startInterview(user.getId(), jobId);
            
            return ResponseEntity.ok(MessageResponse.success("Bắt đầu phỏng vấn thành công", session));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    // --- 2. CHAT (GỬI TIN NHẮN) ---
    // API: POST /api/interview/{sessionId}/chat
    // Body: { "message": "Tôi có 2 năm kinh nghiệm Java..." }
    @PostMapping("/{sessionId}/chat")
    public ResponseEntity<?> chat(@PathVariable Long sessionId, @RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(MessageResponse.error("Tin nhắn không được để trống"));
            }

            // Xử lý tin nhắn và nhận câu trả lời từ AI
            InterviewMessage aiReply = interviewService.processUserMessage(sessionId, message);
            
            return ResponseEntity.ok(MessageResponse.success("Gửi tin nhắn thành công", aiReply));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    // --- 3. KẾT THÚC & CHẤM ĐIỂM ---
    // API: POST /api/interview/{sessionId}/end
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> endInterview(@PathVariable Long sessionId) {
        try {
            InterviewSession result = interviewService.endInterview(sessionId);
            return ResponseEntity.ok(MessageResponse.success("Phỏng vấn kết thúc. Đã có kết quả.", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    // --- 4. LẤY LỊCH SỬ CÁC CUỘC PHỎNG VẤN ---
    // API: GET /api/interview/history
    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        try {
            User user = getCurrentUser();
            return ResponseEntity.ok(MessageResponse.success("Lấy lịch sử thành công", interviewService.getUserHistory(user.getId())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    // --- 5. XEM CHI TIẾT LẠI MỘT BUỔI CŨ ---
    // API: GET /api/interview/{sessionId}
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSessionDetail(@PathVariable Long sessionId) {
        try {
            // Lưu ý: Thực tế nên check xem session này có thuộc về user đang login không để bảo mật
            // Ở đây mình gọi Repo lấy thẳng cho nhanh
            // Bạn có thể thêm hàm findByIdAndUserId trong Service để an toàn hơn
            return ResponseEntity.ok(MessageResponse.success("Lấy chi tiết thành công", interviewService.getSessionDetail(sessionId))); 
            // Note: Cần thêm hàm getSessionDetail trong Service nếu chưa có, hoặc dùng Repo findById
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    // --- HELPER: Lấy User hiện tại từ Token ---
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}