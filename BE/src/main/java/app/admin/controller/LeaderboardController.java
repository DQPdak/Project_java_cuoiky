package app.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.auth.dto.response.MessageResponse;
import app.admin.dto.response.LeaderboardResponse;
import app.admin.service.LeaderboardService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/leaderboard")
    public ResponseEntity<MessageResponse> leaderboard(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("Admin leaderboard requested, limit={}", limit);
        LeaderboardResponse response = leaderboardService.getLeaderboard(limit);
        return ResponseEntity.ok(MessageResponse.success("Lấy leaderboard thành công", response));
    }
}
