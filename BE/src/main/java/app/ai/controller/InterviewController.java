package app.ai.controller;

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

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final SecurityUtils securityUtils;

    // --- 1. LẤY LỊCH SỬ PHỎNG VẤN CỦA 1 JOB CỤ THỂ ---
    // API: GET /api/interview/history?jobId=1
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam Long jobId) {
        try {
            User user = securityUtils.getCurrentUser();
            
            // Gọi service lấy danh sách session khớp với User và Job
            List<InterviewSession> history = interviewService.getHistory(jobId, user.getId());
            
            return ResponseEntity.ok(MessageResponse.success("Lấy lịch sử thành công", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    // --- 2. BẮT ĐẦU PHỎNG VẤN ---
    // API: POST /api/interview/start
    // Body: { "jobId": 1 }
    @PostMapping("/start")
    public ResponseEntity<?> startInterview(@RequestBody Map<String, Long> request) {
        try {
            Long jobId = request.get("jobId");
            if (jobId == null) {
                return ResponseEntity.badRequest().body(MessageResponse.error("Thiếu jobId"));
            }

            User user = securityUtils.getCurrentUser();
            InterviewSession session = interviewService.startInterview(user.getId(), jobId);
            
            return ResponseEntity.ok(MessageResponse.success("Bắt đầu phỏng vấn thành công", session));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    // --- 3. CHAT (GỬI TIN NHẮN) ---
    // API: POST /api/interview/{sessionId}/chat
    // Body: { "message": "Em tên là Huy..." }
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

    // --- 4. KẾT THÚC & CHẤM ĐIỂM ---
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

    // --- 5. XEM CHI TIẾT LẠI MỘT BUỔI CŨ ---
    // API: GET /api/interview/{sessionId}
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSessionDetail(@PathVariable Long sessionId) {
        try {
            // 1. Lấy dữ liệu
            InterviewSession session = interviewService.getSessionDetail(sessionId);
            User currentUser = securityUtils.getCurrentUser();

            // 2. [QUAN TRỌNG] Check bảo mật: User hiện tại có phải chủ nhân của session này không?
            // Nếu không phải -> Chặn ngay để tránh lộ thông tin
            if (!session.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403)
                        .body(MessageResponse.error("Bạn không có quyền xem cuộc phỏng vấn này"));
            }

            return ResponseEntity.ok(MessageResponse.success("Lấy chi tiết thành công", session));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }
}