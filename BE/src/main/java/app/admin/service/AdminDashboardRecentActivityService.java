package app.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import app.admin.dto.response.AdminDashboardRecentActivityResponse;
import app.admin.repository.AdminDashboardRecentActivityRepository;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminDashboardRecentActivityService {

    private final AdminDashboardRecentActivityRepository adminActivityRepository;

    public List<AdminDashboardRecentActivityResponse> getRecentActivities(int limit) {
        if (limit <= 0) limit = 5;

        List<Object[]> rows = adminActivityRepository.findRecentApplicationActivities(limit);

        List<AdminDashboardRecentActivityResponse> result = new ArrayList<>();
        Instant now = Instant.now();

        for (Object[] r : rows) {
            Long applicationId = ((Number) r[0]).longValue();
            String candidateName = (String) r[1];
            String companyName = (String) r[2];

            Instant createdAt;
            Object tsObj = r[3];
            if (tsObj instanceof Timestamp ts) {
                createdAt = ts.toInstant();
            } else if (tsObj instanceof Instant i) {
                createdAt = i;
            } else {
                // fallback
                createdAt = now;
            }

            String message = String.format("%s vừa ứng tuyển vào %s", candidateName, companyName);

            result.add(AdminDashboardRecentActivityResponse.builder()
                    .refId(applicationId)
                    .message(message)
                    .createdAt(createdAt)
                    .timeAgo(toTimeAgoVi(createdAt, now))
                    .build());
        }
        return result;
    }

    private String toTimeAgoVi(Instant createdAt, Instant now) {
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        if (minutes < 1) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";

        long hours = ChronoUnit.HOURS.between(createdAt, now);
        if (hours < 24) return hours + " giờ trước";

        long days = ChronoUnit.DAYS.between(createdAt, now);
        return days + " ngày trước";
    }
}
