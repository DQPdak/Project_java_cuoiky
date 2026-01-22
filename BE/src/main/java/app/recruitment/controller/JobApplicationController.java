package app.recruitment.controller;

import app.recruitment.dto.response.JobApplicationResponse; // Giả sử đã có DTO này
import app.recruitment.entity.enums.ApplicationStatus;
import app.recruitment.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment/applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    // 1. Xem luồng ứng viên cho một Job cụ thể (kèm điểm phù hợp)
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsByJob(@PathVariable Long jobId) {
        // Service cần join với bảng CVAnalysisResult để lấy điểm matchScore
        return ResponseEntity.ok(jobApplicationService.getApplicationsByJobId(jobId));
    }

    // 2. Cập nhật trạng thái (Sơ tuyển, Phỏng vấn, Offer)
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> statusUpdate) {
        
        String newStatus = statusUpdate.get("status");
        jobApplicationService.updateApplicationStatus(applicationId, ApplicationStatus.valueOf(newStatus));
        
        // Gợi ý mở rộng: Nếu status là "OFFERED", gọi EmailService để gửi email cho ứng viên
        if ("OFFERED".equals(newStatus)) {
            // emailService.sendOfferEmail(...);
        }
        
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
}