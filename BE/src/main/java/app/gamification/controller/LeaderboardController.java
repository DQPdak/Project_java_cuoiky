package app.gamification.controller;

import app.gamification.service.LeaderboardService;
import app.gamification.dto.response.LeaderboardEntryResponse;
import app.gamification.dto.response.LeaderboardMeResponse;
import app.gamification.dto.response.LeaderboardLogResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/admin/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    // GET /api/leaderboard?role=CANDIDATE&period=WEEK&periodKey=2026-W05&limit=50
    @GetMapping
    public ResponseEntity<?> top(
        @RequestParam(defaultValue = "CANDIDATE") String role,
        @RequestParam(defaultValue = "WEEK") String period,
        @RequestParam(required = false) String periodKey,
        @RequestParam(defaultValue = "50") int limit
    ) {
        // auto periodKey nếu không truyền
        if (periodKey == null || periodKey.isBlank()) {
            ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
            LeaderboardService.PeriodType pt = LeaderboardService.PeriodType.valueOf(period.toUpperCase(Locale.ROOT));
            periodKey = leaderboardService.currentPeriodKey(pt, zone);
        }

        List<LeaderboardEntryResponse> data = leaderboardService.getTop(role, period, periodKey, limit);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("data", data);
        res.put("meta", Map.of("role", role, "period", period, "periodKey", periodKey, "limit", limit));
        return ResponseEntity.ok(res);
    }

    // GET /api/leaderboard/me?userId=123&role=CANDIDATE&period=WEEK&periodKey=2026-W05
    @GetMapping("/me")
    public ResponseEntity<?> me(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "CANDIDATE") String role,
        @RequestParam(defaultValue = "WEEK") String period,
        @RequestParam(required = false) String periodKey
    ) {
        if (periodKey == null || periodKey.isBlank()) {
            ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
            LeaderboardService.PeriodType pt = LeaderboardService.PeriodType.valueOf(period.toUpperCase(Locale.ROOT));
            periodKey = leaderboardService.currentPeriodKey(pt, zone);
        }

        LeaderboardMeResponse data = leaderboardService.getMe(userId, role, period, periodKey);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("data", data); // có thể null nếu user chưa có điểm
        res.put("meta", Map.of("role", role, "period", period, "periodKey", periodKey));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/logs")
        public ResponseEntity<?> logs(@RequestParam(defaultValue = "5") int limit) {
        List<LeaderboardLogResponse> data = leaderboardService.getRecentLogs(limit);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
