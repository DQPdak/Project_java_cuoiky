package app.admin.controller;

import app.admin.dto.response.DashboardSummaryResponse;
import app.admin.service.AdminDashboardService;
import app.auth.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService service;

    @GetMapping("/summary")
    public ResponseEntity<MessageResponse> summary() {
        return ResponseEntity.ok(
            MessageResponse.success("Dashboard summary", service.summary())
        );
    }
}
