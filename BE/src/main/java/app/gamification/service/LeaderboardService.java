package app.gamification.service;

import app.gamification.repository.LeaderboardPointsLogRepository;
import app.gamification.repository.LeaderboardScoreRepository;
import app.gamification.dto.response.LeaderboardEntryResponse;
import app.gamification.dto.response.LeaderboardMeResponse;
import app.gamification.dto.response.LeaderboardLogResponse;
import app.gamification.model.LeaderboardPointsLog;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
public class LeaderboardService {

    private final LeaderboardPointsLogRepository logRepo;
    private final LeaderboardScoreRepository scoreRepo;

    public LeaderboardService(LeaderboardPointsLogRepository logRepo, LeaderboardScoreRepository scoreRepo) {
        this.logRepo = logRepo;
        this.scoreRepo = scoreRepo;
    }

    // --- config MVP (bạn chỉnh tuỳ ý) ---
    private static final Map<String, Integer> DAILY_LIMIT = Map.of(
        "LOGIN_DAILY", 1,
        "APPLY", 5,
        "JOB_POST_APPROVED", 10
    );

    public enum PeriodType { WEEK, MONTH, YEAR, ALL_TIME }

    /**
     * Gộp role VIP vào 2 nhóm: CANDIDATE / RECRUITER
     * users.user_role: CANDIDATE, CANDIDATE_VIP, RECRUITER, RECRUITER_VIP
     */
    public String normalizeRoleGroup(String userRole) {
        if (userRole == null) return "UNKNOWN";
        String r = userRole.trim().toUpperCase(Locale.ROOT);
        if (r.startsWith("CANDIDATE")) return "CANDIDATE";
        if (r.startsWith("RECRUITER")) return "RECRUITER";
        return r; // fallback
    }

    private OffsetDateTime startOfToday(ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);
        return today.atStartOfDay(zoneId).toOffsetDateTime();
    }

    public String currentPeriodKey(PeriodType type, ZoneId zoneId) {
        LocalDate now = LocalDate.now(zoneId);
        if (type == PeriodType.ALL_TIME) return "ALL";
        if (type == PeriodType.MONTH) return now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        if (type == PeriodType.YEAR) return now.format(DateTimeFormatter.ofPattern("yyyy"));

        // WEEK: yyyy-'W'ww (ISO week)
        WeekFields wf = WeekFields.ISO;
        int week = now.get(wf.weekOfWeekBasedYear());
        int year = now.get(wf.weekBasedYear());
        return String.format(Locale.ROOT, "%d-W%02d", year, week);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean addPoints(Long userId, String userRoleFromUsersTable, String actionType, int points, Long refId) {
        if (userId == null || actionType == null || points == 0) return false;

        String roleGroup = normalizeRoleGroup(userRoleFromUsersTable);
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

        // 1) limit/ngày
        Integer limit = DAILY_LIMIT.get(actionType);
        if (limit != null) {
            long used = logRepo.countTodayActions(userId, actionType, startOfToday(zone));
            if (used >= limit) return false;
        }

        // 2) insert log
        if (refId != null) {
            if (logRepo.existsByUserIdAndActionTypeAndRefId(userId, actionType, refId)) {
                return false;
            }
        }

        LeaderboardPointsLog log = new LeaderboardPointsLog();
        log.setUserId(userId);
        log.setRole(roleGroup);
        log.setActionType(actionType);
        log.setPoints(points);
        log.setRefId(refId);

        try {
            logRepo.save(log);
        } catch (DataIntegrityViolationException ex) {
            // Nhờ REQUIRES_NEW, exception này chỉ rollback transaction con của addPoints
            // Không làm hỏng transaction cha của việc Apply
            return false;
        }

        // 3) upsert score
        String weekKey = currentPeriodKey(PeriodType.WEEK, zone);
        String monthKey = currentPeriodKey(PeriodType.MONTH, zone);
        String yearKey = currentPeriodKey(PeriodType.YEAR, zone);

        scoreRepo.upsertAddScore(userId, roleGroup, "WEEK", weekKey, points);
        scoreRepo.upsertAddScore(userId, roleGroup, "MONTH", monthKey, points);
        scoreRepo.upsertAddScore(userId, roleGroup, "YEAR", yearKey, points);
        scoreRepo.upsertAddScore(userId, roleGroup, "ALL_TIME", "ALL", points);

        return true;
    }
    
    public List<LeaderboardEntryResponse> getTop(String roleGroup, String periodType, String periodKey, int limit) {
        String role = roleGroup.toUpperCase(Locale.ROOT);
        String pType = periodType.toUpperCase(Locale.ROOT);
        return scoreRepo.top(role, pType, periodKey, limit);
    }

    public LeaderboardMeResponse getMe(Long userId, String roleGroup, String periodType, String periodKey) {
        String role = roleGroup.toUpperCase(Locale.ROOT);
        String pType = periodType.toUpperCase(Locale.ROOT);
        return scoreRepo.me(userId, role, pType, periodKey);
    }

    public List<LeaderboardLogResponse> getRecentLogs(int limit) {
    int safeLimit = Math.max(1, Math.min(limit, 50));
    return logRepo.recentLogs(safeLimit);
}
}
