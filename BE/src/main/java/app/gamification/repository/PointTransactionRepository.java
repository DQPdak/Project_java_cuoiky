package app.gamification.repository;

import app.gamification.model.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    boolean existsByUserIdAndActionAndRefTypeAndRefId(Long userId, String action, String refType, Long refId);
}
