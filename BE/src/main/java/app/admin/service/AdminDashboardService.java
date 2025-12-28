package app.admin.service;

import app.admin.dto.response.DashboardSummaryResponse;
import app.content.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import app.admin.dto.response.ApplicationsByDayResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ArticleRepository articleRepo;

    public DashboardSummaryResponse summary() {
        return DashboardSummaryResponse.builder()
                .totalArticles(articleRepo.count())
                .build();
    }
}
