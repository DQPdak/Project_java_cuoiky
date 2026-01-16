package app.ai.repository;

import app.ai.models.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IInterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    // Lấy lịch sử phỏng vấn của 1 user (Mới nhất lên đầu)
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}