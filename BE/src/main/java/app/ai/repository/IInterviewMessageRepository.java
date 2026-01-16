package app.ai.repository;

import app.ai.models.InterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IInterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {
}