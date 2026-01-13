package app.recruitment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import app.recruitment.service.JobApplicationService;
import app.auth.dto.response.MessageResponse;
import app.recruitment.dto.request.JobApplicationRequest;
import app.recruitment.dto.response.JobApplicationResponse;
import app.recruitment.mapper.RecruitmentMapper;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.enums.ApplicationStatus;
import app.auth.repository.UserRepository;

@RestController
@RequestMapping("/api/applications") // Để chung là applications
@RequiredArgsConstructor
@Slf4j
public class JobApplicationController {

    private final JobApplicationService applicationService;
    private final UserRepository userRepository;
    private final RecruitmentMapper mapper;

    /**
     * ỨNG VIÊN NỘP ĐƠN
     */
    @PostMapping("/apply")
    public ResponseEntity<?> apply(@Valid @RequestBody JobApplicationRequest request) {
        Long candidateId = getCurrentUserId();
        JobApplication created = applicationService.apply(candidateId, request);
        
        return ResponseEntity.status(201).body(MessageResponse.success(
            "Ứng tuyển thành công!", 
            mapper.toJobApplicationResponse(created)
        ));
    }

    /**
     * RECRUITER CẬP NHẬT TRẠNG THÁI (DUYỆT/LOẠI)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<JobApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus newStatus,
            @RequestParam(required = false) String recruiterNote
    ) {
        Long recruiterId = getCurrentUserId();
        JobApplication updated = applicationService.updateStatus(recruiterId, id, newStatus, recruiterNote);
        return ResponseEntity.ok(mapper.toJobApplicationResponse(updated));
    }

    /**
     * CƠ CHẾ LẤY ID TỪ TOKEN (ĐÃ FIX LỖI 1L)
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    /**
     * LẤY DANH SÁCH ĐƠN ĐÃ NỘP CỦA TÔI (DÀNH CHO CANDIDATE)
     * API này cực kỳ quan trọng để Frontend lấy được applicationId gọi sang AI
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyApplications() {
        Long candidateId = getCurrentUserId();
        // Gọi hàm trả về DTO trực tiếp đã viết ở Service
        List<JobApplicationResponse> list = applicationService.getApplicationsByCandidateId(candidateId);
        
        return ResponseEntity.ok(MessageResponse.success(
            "Lấy danh sách ứng tuyển thành công", 
            list
        ));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplicationResponse>> listByJob(@PathVariable Long jobId) {
        List<JobApplicationResponse> list = applicationService.listByJob(jobId)
                .stream().map(mapper::toJobApplicationResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}