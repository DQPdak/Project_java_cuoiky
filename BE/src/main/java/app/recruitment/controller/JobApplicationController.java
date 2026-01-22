package app.recruitment.controller;

// 1. Import MessageResponse từ module Auth
import app.auth.dto.response.MessageResponse;
import app.recruitment.dto.response.JobApplicationResponse;
import app.recruitment.entity.enums.ApplicationStatus;
import app.recruitment.service.JobApplicationService;
// 2. Import SecurityUtils để lấy ID người dùng
import app.util.SecurityUtils;

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
    // 3. Inject SecurityUtils vào Controller
    private final SecurityUtils securityUtils;

    // 1. Xem luồng ứng viên cho một Job cụ thể (kèm điểm phù hợp)
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsByJobId(jobId));
    }

    // 2. Cập nhật trạng thái (Sơ tuyển, Phỏng vấn, Offer)
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> statusUpdate) {
        
        String newStatus = statusUpdate.get("status");
        jobApplicationService.updateApplicationStatus(applicationId, ApplicationStatus.valueOf(newStatus));
        
        if ("OFFERED".equals(newStatus)) {
            // Logic gửi mail (để sau)
        }
        
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

    // 3. Hủy đơn ứng tuyển
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelApplication(@PathVariable Long id) {
        // Sử dụng SecurityUtils đã inject để lấy ID (thay vì tự viết hàm)
        Long candidateId = securityUtils.getCurrentUserId();
        
        // Gọi đúng tên biến jobApplicationService (thay vì applicationService)
        jobApplicationService.deleteApplication(candidateId, id);

        // Trả về MessageResponse (đã import ở trên)
        return ResponseEntity.ok(MessageResponse.success("Hủy ứng tuyển thành công", null));
    }
}