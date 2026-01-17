package app.ai.repository;

import app.ai.models.InterviewMessage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IInterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {
    // Lấy toàn bộ tin nhắn của một cuộc phỏng vấn, sắp xếp theo thứ tự thời gian (cũ trước, mới sau)
    // Để hiển thị lại đoạn chat cho đúng thứ tự
    List<InterviewMessage> findBySessionIdOrderBySentAtAsc(Long sessionId);
}