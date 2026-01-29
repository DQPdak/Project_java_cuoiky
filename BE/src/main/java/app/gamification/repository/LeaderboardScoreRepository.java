package app.gamification.repository;

import app.gamification.model.LeaderboardScore;
import app.gamification.dto.response.LeaderboardEntryResponse;
import app.gamification.dto.response.LeaderboardMeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LeaderboardScoreRepository extends JpaRepository<LeaderboardScore, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        insert into leaderboard_scores(user_id, role, period_type, period_key, score, updated_at)
        values (:userId, :role, :periodType, :periodKey, :points, now())
        on conflict (user_id, role, period_type, period_key)
        do update set
          score = leaderboard_scores.score + excluded.score,
          updated_at = now()
    """, nativeQuery = true)
    void upsertAddScore(
        @Param("userId") Long userId,
        @Param("role") String role,
        @Param("periodType") String periodType,
        @Param("periodKey") String periodKey,
        @Param("points") int points
    );

    /**
     * Top N (join users để lấy full_name)
     * Lưu ý: tuỳ DB bạn đặt cột tên là full_name hay fullName.
     * Mình assume users.full_name (theo đoạn lỗi bạn từng gửi).
     */
    @Query(value = """
        select
          ls.user_id as userId,
          u.full_name as fullName,
          ls.score as score,
          dense_rank() over (order by ls.score desc, ls.updated_at asc) as rank
        from leaderboard_scores ls
        join users u on u.id = ls.user_id
        where ls.role = :role
          and ls.period_type = :periodType
          and ls.period_key = :periodKey
        order by ls.score desc, ls.updated_at asc
        limit :limit
    """, nativeQuery = true)
    List<LeaderboardEntryResponse> top(
        @Param("role") String role,
        @Param("periodType") String periodType,
        @Param("periodKey") String periodKey,
        @Param("limit") int limit
    );

    @Query(value = """
        select *
        from (
          select
            ls.user_id as userId,
            u.full_name as fullName,
            ls.score as score,
            dense_rank() over (order by ls.score desc, ls.updated_at asc) as rank
          from leaderboard_scores ls
          join users u on u.id = ls.user_id
          where ls.role = :role
            and ls.period_type = :periodType
            and ls.period_key = :periodKey
        ) t
        where t.userId = :userId
    """, nativeQuery = true)
    LeaderboardMeResponse me(
        @Param("userId") Long userId,
        @Param("role") String role,
        @Param("periodType") String periodType,
        @Param("periodKey") String periodKey
    );
}
