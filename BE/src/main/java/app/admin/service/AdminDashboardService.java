package app.admin.service;

import org.springframework.stereotype.Service;
import ut.edu.admin.dto.ApplicationsByDayResponse;
import ut.edu.admin.dto.DashboardSummaryResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDashboardService {

    public DashboardSummaryResponse getSummary() {
        // TODO: thay bằng query DB thật
        return DashboardSummaryResponse.builder()
                .totalUsers(0)
                .totalJobs(0)
                .totalApplications(0)
                .totalArticles(0)
                .build();
    }

    public ApplicationsByDayResponse getApplicationsByDay(int days) {
        if (days <= 0 || days > 90) days = 7;

        // TODO: thay bằng query DB thật
        List<ApplicationsByDayResponse.DailyCount> series = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            series.add(ApplicationsByDayResponse.DailyCount.builder()
                    .date(d.toString())
                    .count(0)
                    .build());
        }

        return ApplicationsByDayResponse.builder()
                .days(days)
                .series(series)
                .build();
    }
}
