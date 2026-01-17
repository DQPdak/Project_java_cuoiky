package app.ai.repository;

import app.ai.models.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IInterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    // Tìm session theo User ID và Job ID (Mới nhất lên đầu)
    List<InterviewSession> findByUserIdAndJobPostingIdOrderByCreatedAtDesc(Long userId, Long jobPostingId);

    // Tìm tất cả session của user (cái cũ nếu cần dùng chỗ khác)
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}