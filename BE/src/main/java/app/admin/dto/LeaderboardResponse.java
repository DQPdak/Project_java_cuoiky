package app.admin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LeaderboardResponse {
    private List<UserRank> topUsers;

    @Data
    @Builder
    public static class UserRank {
        private String name;
        private int points;
        private String badge;
    }
}