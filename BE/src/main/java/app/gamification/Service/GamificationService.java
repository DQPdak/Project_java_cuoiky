package app.gamification.Service;

import app.gamification.model.UserPoints;
import app.gamification.repository.UserPointsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final UserPointsRepository repo;

    public UserPoints addPoints(Long userId, int points) {
        UserPoints up = repo.findByUserId(userId)
                .orElse(UserPoints.builder().userId(userId).totalPoints(0).build());
        up.setTotalPoints(up.getTotalPoints() + points);
        return repo.save(up);
    }

    public UserPoints me(Long userId) {
        return repo.findByUserId(userId).orElse(null);
    }
}
