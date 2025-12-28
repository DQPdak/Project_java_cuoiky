package app.admin.service;

import org.springframework.stereotype.Service;
import app.admin.dto.response.LeaderboardResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    public LeaderboardResponse getLeaderboard(int limit) {
        if (limit <= 0 || limit > 100) limit = 10;

        // TODO: thay bằng query DB thật
        List<LeaderboardResponse.Item> items = new ArrayList<>();
        for (int i = 1; i <= limit; i++) {
            items.add(LeaderboardResponse.Item.builder()
                    .rank(i)
                    .userId((long) i)
                    .displayName("User " + i)
                    .totalPoints(0)
                    .build());
        }
        return LeaderboardResponse.builder().items(items).build();
    }
}