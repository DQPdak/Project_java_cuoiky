package app.admin.service;

import app.admin.dto.LeaderboardResponse;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class LeaderboardService {

    public LeaderboardResponse getLeaderboard(int limit) {
        // TODO: Code logic lấy dữ liệu thật từ Database sau này
        // Hiện tại trả về dữ liệu giả để hết lỗi
        return LeaderboardResponse.builder()
                .topUsers(new ArrayList<>())
                .build();
    }
}