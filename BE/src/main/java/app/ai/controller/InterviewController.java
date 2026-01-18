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

    // ... (Giữ nguyên các hàm helper convertToDTO và API getHistory, startInterview đã ổn) ...
    // Bạn có thể copy lại phần convertToDTO và các hàm khác từ file cũ nếu muốn, 
    // ở đây tôi chỉ viết lại hàm CHAT là hàm cần sửa nhất.

    // --- HÀM CHUYỂN ĐỔI (Dùng lại của file cũ) ---
    private InterviewDTO convertToDTO(InterviewSession session, boolean includeMessages) {
        InterviewDTO.InterviewDTOBuilder builder = InterviewDTO.builder()
                .id(session.getId())
                .status(session.getStatus())
                .score(session.getFinalScore())
                .feedback(session.getFeedback())
                .createdAt(session.getCreatedAt())
                .jobId(session.getJobPosting().getId())
                .jobTitle(session.getJobPosting().getTitle())
                .companyName(session.getJobPosting().getCompany() != null ? session.getJobPosting().getCompany().getName() : "Chưa cập nhật")
                .candidateId(session.getUser().getId())
                .candidateName(session.getUser().getFullName());

        if (includeMessages && session.getMessages() != null) {
            builder.messages(session.getMessages().stream().map(msg -> 
                InterviewDTO.MessageDTO.builder()
                    .sender(msg.getSender())
                    .content(msg.getContent())
                    .sentAt(msg.getSentAt())
                    .build()
            ).collect(Collectors.toList()));
        } else {
            builder.messages(null);
        }
        return builder.build();
    }

    // ... (Các API getHistory, startInterview, endInterview giữ nguyên) ...
    
    // 👇👇👇 HÀM CẦN SỬA 👇👇👇
    @PostMapping("/{sessionId}/chat")
    public ResponseEntity<?> chat(@PathVariable Long sessionId, @RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            // reply ở đây là Entity (đang giữ session Lazy)
            InterviewMessage reply = interviewService.processUserMessage(sessionId, message);
            
            // ✅ SỬA: Convert sang DTO ngay lập tức
            InterviewDTO.MessageDTO dto = InterviewDTO.MessageDTO.builder()
                    .sender(reply.getSender())
                    .content(reply.getContent())
                    .sentAt(reply.getSentAt())
                    .build();

            // Trả về DTO, không trả về Entity
            return ResponseEntity.ok(MessageResponse.success("Gửi tin thành công", dto)); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }
    
    // ... (Các API khác giữ nguyên logic cũ) ...
    // Để tiện, tôi dán lại các hàm còn lại ở đây cho bạn copy paste full file luôn cho đỡ lỗi:
    
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam Long jobId) {
        try {
            User user = securityUtils.getCurrentUser();
            List<InterviewSession> history = interviewService.getHistory(jobId, user.getId());
            List<InterviewDTO> dtos = history.stream().map(s -> convertToDTO(s, false)).collect(Collectors.toList());
            return ResponseEntity.ok(MessageResponse.success("Lấy lịch sử thành công", dtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startInterview(@RequestBody Map<String, Long> request) {
        try {
            Long jobId = request.get("jobId");
            User user = securityUtils.getCurrentUser();
            InterviewSession session = interviewService.startInterview(user.getId(), jobId);
            return ResponseEntity.ok(MessageResponse.success("Bắt đầu thành công", convertToDTO(session, true)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> endInterview(@PathVariable Long sessionId) {
        try {
            InterviewSession result = interviewService.endInterview(sessionId);
            return ResponseEntity.ok(MessageResponse.success("Kết thúc phỏng vấn", convertToDTO(result, true)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSessionDetail(@PathVariable Long sessionId) {
        try {
            InterviewSession session = interviewService.getSessionDetail(sessionId);
            if (!session.getUser().getId().equals(securityUtils.getCurrentUser().getId())) {
                 return ResponseEntity.status(403).body(MessageResponse.error("Không có quyền truy cập"));
            }
            return ResponseEntity.ok(MessageResponse.success("Lấy chi tiết thành công", convertToDTO(session, true)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }
}