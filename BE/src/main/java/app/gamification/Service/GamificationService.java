package app.gamification.service;

import app.gamification.model.PointTransaction;
import app.gamification.model.UserPoints;
import app.gamification.repository.PointTransactionRepository;
import app.gamification.repository.UserPointsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final PointTransactionRepository txRepo;
    private final UserPointsRepository pointsRepo;

    /**
     * Cộng điểm theo action, đảm bảo KHÔNG cộng trùng cho cùng (userId, action, refType, refId)
     *
     * @return true nếu cộng điểm thành công (tạo transaction mới), false nếu bị trùng (đã cộng trước đó)
     */
    @Transactional
    public boolean awardPoints(Long userId, String action, int points, String refType, Long refId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (action == null || action.isBlank()) throw new IllegalArgumentException("action is required");

        // refType/refId có thể null với các action chỉ cộng 1 lần theo user (VD PROFILE_COMPLETE)
        String safeRefType = (refType == null) ? "" : refType.trim();

        // 1) Tạo transaction (dựa vào UNIQUE INDEX để chống trùng tuyệt đối)
        try {
            PointTransaction tx = PointTransaction.builder()
                    .userId(userId)
                    .action(action.trim())
                    .points(points)
                    .refType(safeRefType.isEmpty() ? null : safeRefType)
                    .refId(refId)
                    .build();
            txRepo.save(tx);
        } catch (DataIntegrityViolationException duplicate) {
            // đã có transaction cùng khóa unique => không cộng nữa
            return false;
        }

        // 2) Upsert tổng điểm
        UserPoints up = pointsRepo.findById(userId).orElse(null);
        if (up == null) {
            up = UserPoints.builder()
                    .userId(userId)
                    .totalPoints(Math.max(0, points)) // tránh âm nếu bạn chưa hỗ trợ trừ
                    .build();
        } else {
            int newTotal = up.getTotalPoints() + points;
            up.setTotalPoints(Math.max(0, newTotal));
        }
        pointsRepo.save(up);

        // 3) Hook badge (làm sau)
        // badgeService.evaluate(userId);

        return true;
    }
    public UserPoints getUserPoints(Long userId) {
    return pointsRepo.findById(userId)
            .orElse(
                UserPoints.builder()
                    .userId(userId)
                    .totalPoints(0)
                    .build()
            );
}
}
