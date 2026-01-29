package app.gamification.controller;

import app.auth.dto.response.MessageResponse;
import app.gamification.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService service;

    /**
     * Cộng điểm theo action, có refType/refId để chống cộng trùng.
     * Ví dụ:
     * POST /api/gamification/points/award?userId=1&action=APPLY_JOB&points=10&refType=JOB&refId=100
     */
    @PostMapping("/points/award")
    public ResponseEntity<MessageResponse> awardPoints(
            @RequestParam Long userId,
            @RequestParam String action,
            @RequestParam int points,
            @RequestParam(required = false) String refType,
            @RequestParam(required = false) Long refId
    ) {
        boolean awarded = service.awardPoints(userId, action, points, refType, refId);

        if (!awarded) {
            return ResponseEntity.ok(
                    MessageResponse.success("Bỏ qua: điểm đã được cộng trước đó", false)
            );
        }

        return ResponseEntity.ok(
                MessageResponse.success("Cộng điểm thành công", true)
        );
    }

    /**
     * (Tuỳ chọn) Lấy tổng điểm hiện tại của 1 user.
     * Nếu bạn chưa viết service.getMyPoints thì tạm thời bỏ endpoint này.
     */
    @GetMapping("/points")
    public ResponseEntity<MessageResponse> getPoints(@RequestParam Long userId) {
        return ResponseEntity.ok(
                MessageResponse.success("Thông tin điểm", service.getUserPoints(userId))
        );
    }
}
