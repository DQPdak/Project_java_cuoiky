package app.gamification.repository;

import app.gamification.model.LeaderboardPointsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;

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
}
