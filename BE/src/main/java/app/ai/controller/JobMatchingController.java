package app.ai.controller;

import app.ai.service.JobMatchingService;
import app.ai.service.cv.gemini.dto.MatchResult;
import app.ai.service.cv.gemini.dto.analysis.CareerAdviceResult;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.recruitment.dto.response.JobApplicationResponse;
import app.recruitment.entity.JobApplication;
import app.recruitment.mapper.RecruitmentMapper;
import app.recruitment.repository.JobApplicationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matching") // Tách biệt hoàn toàn với /job-applications
@RequiredArgsConstructor
@Slf4j
public class JobMatchingController {

    private final JobMatchingService matchingService;
    private final RecruitmentMapper mapper;
    private final UserRepository userRepository;
    private final JobApplicationRepository applicationRepository;

    // ==================== DÀNH CHO ỨNG VIÊN (CANDIDATE) ====================

    /**
     * API 1: Xem trước độ phù hợp với Job (Chưa cần nộp đơn)
     * URL: GET /api/matching/preview/{jobId}
     * Header: Authorization Bearer Token (Của ứng viên)
     */
    @GetMapping("/preview/{jobId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'STUDENT')") // Cho phép ứng viên
    public ResponseEntity<MatchResult> previewMatch(@PathVariable Long jobId) {
        Long candidateId = getCurrentUserId(); // Lấy ID từ Token
        
        // Gọi service tính toán (không lưu DB)
        MatchResult result = matchingService.previewMatch(candidateId, jobId);
        
        return ResponseEntity.ok(result);
    }

    // ==================== DÀNH CHO NHÀ TUYỂN DỤNG (RECRUITER) ====================

    /**
     * API 2: Kích hoạt AI chấm điểm cho tất cả đơn của 1 Job
     * URL: POST /api/matching/screen/{jobId}
     * Tác dụng: Chạy ngầm, Recruiter không phải chờ lâu.
     */
    @PostMapping("/screen/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<?> triggerScreening(@PathVariable Long jobId) {
        // Gọi hàm Async trong service
        matchingService.screenApplications(jobId);
        
        return ResponseEntity.ok("Hệ thống đang tiến hành phân tích CV và chấm điểm. Vui lòng quay lại xem Bảng xếp hạng sau vài phút.");
    }

    /**
     * API 3: Xem bảng xếp hạng ứng viên (Đã sắp xếp điểm cao -> thấp)
     * URL: GET /api/matching/ranking/{jobId}?minScore=70
     * Param: minScore (Tùy chọn, mặc định = 0 lấy hết)
     */
    @GetMapping("/ranking/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<JobApplicationResponse>> getRankedList(
            @PathVariable Long jobId,
            @RequestParam(required = false, defaultValue = "0") Integer minScore
    ) {
        // 1. Gọi Service lấy danh sách đã lọc & sort từ DB
        List<JobApplication> applications = matchingService.getRankedApplications(jobId, minScore);

        // 2. Convert sang Response DTO để trả về Frontend
        List<JobApplicationResponse> response = applications.stream()
                .map(mapper::toJobApplicationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ==================== HÀM HELPER (LẤY ID TỪ TOKEN) ====================
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return user.getId();
    }

    @GetMapping("/advice/{applicationId}")
@PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
public ResponseEntity<?> getCareerAdvice(@PathVariable Long applicationId, Authentication authentication) {
    // 1. Lấy thông tin đơn ứng tuyển từ DB
    JobApplication app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn ứng tuyển"));

    // 2. Lấy Email của người đang gọi API từ Token
    String currentUserEmail = authentication.getName();

    // 3. KIỂM TRA BẢO MẬT
    boolean isCandidateOfThisApp = app.getCandidate().getEmail().equals(currentUserEmail);
    
    // Nếu là Recruiter thì phải là người đăng Job này mới được xem
    boolean isRecruiterOfThisJob = app.getJobPosting().getRecruiter().getEmail().equals(currentUserEmail);

    if (!isCandidateOfThisApp && !isRecruiterOfThisJob) {
        // Nếu không phải ứng viên của đơn này, cũng không phải chủ Job -> Chặn đứng!
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Bạn không có quyền xem gợi ý phát triển của đơn ứng tuyển này.");
    }

    // 4. Nếu vượt qua kiểm tra, mới gọi Service để lấy gợi ý (hoặc gọi AI)
    CareerAdviceResult result = matchingService.getCareerAdvice(applicationId);
    return ResponseEntity.ok(result);
}
}
