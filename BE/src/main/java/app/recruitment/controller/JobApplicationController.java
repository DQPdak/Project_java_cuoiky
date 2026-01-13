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

@RestController
@RequestMapping("/api/recruiter/applications")
@RequiredArgsConstructor
@Slf4j
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final app.auth.repository.UserRepository userRepository;
    private final JobApplicationService applicationService;
    private final RecruitmentMapper mapper;

    @PostMapping("/apply")
    public ResponseEntity<JobApplicationResponse> apply(
            @Valid @RequestBody JobApplicationRequest request
    ) {
        Long studentId = getCurrentUserId();
        JobApplication created = applicationService.apply(studentId, request);
        return ResponseEntity.status(201).body(mapper.toJobApplicationResponse(created));
    }

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

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<JobApplicationResponse>> listByJob(@PathVariable Long jobId) {
        List<JobApplicationResponse> list = applicationService.listByJob(jobId)
                .stream().map(mapper::toJobApplicationResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // Candidate's own applications
    @GetMapping("/student/me")
    public ResponseEntity<List<JobApplicationResponse>> listByStudent() {
        Long studentId = getCurrentUserId();
        List<JobApplicationResponse> list = applicationService.listByStudent(studentId)
                .stream().map(mapper::toJobApplicationResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getById(@PathVariable Long id) {
        JobApplication app = applicationService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
        return ResponseEntity.ok(mapper.toJobApplicationResponse(app));
    }

    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications() {
        // Lấy User ID từ Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(MessageResponse.success(
            "Lấy danh sách ứng tuyển thành công", 
            jobApplicationService.getApplicationsByCandidateId(user.getId())
        ));
    }
    // Mock current user id (temporary). Replace with SecurityContext lookup.
    private Long getCurrentUserId() {
        return 1L;
    }
}