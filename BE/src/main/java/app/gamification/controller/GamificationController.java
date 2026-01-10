package app.gamification.controller;

import app.auth.dto.response.MessageResponse;
import app.gamification.model.UserPoints;
import app.gamification.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService service;

    @PostMapping("/add")
    public ResponseEntity<MessageResponse> add(@RequestParam Long userId,
                                               @RequestParam int points) {
        UserPoints up = service.addPoints(userId, points);
        return ResponseEntity.ok(
            MessageResponse.success("Cộng điểm thành công", up)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<MessageResponse> me(@RequestParam Long userId) {
        return ResponseEntity.ok(
            MessageResponse.success("Thông tin điểm", service.me(userId))
        );
    }
}
