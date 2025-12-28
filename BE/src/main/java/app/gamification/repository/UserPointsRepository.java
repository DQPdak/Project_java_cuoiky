package app.gamification.repository;

import app.gamification.model.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {
    Optional<UserPoints> findByUserId(Long userId);
}
