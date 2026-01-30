package app.admin.controller;

import app.admin.dto.request.CreateViolationReportRequest;
import app.admin.dto.response.ViolationReportResponse;
import app.admin.service.ViolationReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ViolationReportController {

    private final ViolationReportService reportService;

    // Bạn lấy reporterId từ SecurityContext/JWT (khuyến khích), ở đây demo truyền thẳng
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ViolationReportResponse create(
            @RequestParam Long reporterId,
            @Valid @RequestBody CreateViolationReportRequest req
    ) {
        return reportService.create(reporterId, req);
    }
}
