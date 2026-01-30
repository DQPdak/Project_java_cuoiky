package app.gamification.repository;

import app.gamification.dto.response.LeaderboardLogResponse;
import app.gamification.model.LeaderboardPointsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface LeaderboardPointsLogRepository extends JpaRepository<LeaderboardPointsLog, Long> {

    @Query("""
        select count(l)
        from LeaderboardPointsLog l
        where l.userId = :userId
          and l.actionType = :actionType
          and l.createdAt >= :fromTime
    """)
    long countTodayActions(Long userId, String actionType, OffsetDateTime fromTime);

    boolean existsByUserIdAndActionTypeAndRefId(Long userId, String actionType, Long refId);

    // --- recent logs for admin view
    @Query(value = """
    select
        l.user_id as userId,
        u.full_name as fullName,
        l.role as role,
        l.action_type as actionType,
        l.points as points,
        l.ref_id as refId,
        l.created_at as createdAt
    from leaderboard_points_log l
    join users u on u.id = l.user_id
    order by l.created_at desc
    limit :limit
    """, nativeQuery = true)
    List<LeaderboardLogResponse> recentLogs(@Param("limit") int limit);
}
