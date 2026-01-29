package app.gamification.repository;

import app.gamification.model.UserPoints;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointsRepository extends JpaRepository<UserPoints, Long> { }
