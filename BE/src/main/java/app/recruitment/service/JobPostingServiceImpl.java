package app.recruitment.service;

import app.ai.service.cv.gemini.GeminiService;
import app.auth.model.User;
import app.auth.model.enums.UserRole;
import app.auth.repository.UserRepository;
import app.recruitment.dto.request.JobPostingRequest;
import app.recruitment.dto.response.JobPostingResponse;
import app.recruitment.entity.JobPosting;
import app.recruitment.entity.enums.JobStatus;
import app.recruitment.mapper.RecruitmentMapper;
import app.recruitment.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher; // MỚI: Import Publisher
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// --- MỚI: Import Event & Enum Action ---
import app.gamification.event.PointEvent;
import app.gamification.model.UserPointAction;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostingServiceImpl implements JobPostingService {

    private static final Logger log = LoggerFactory.getLogger(JobPostingServiceImpl.class);

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final RecruitmentMapper recruitmentMapper;
    private final GeminiService geminiService;
    
    // --- THAY ĐỔI: Dùng EventPublisher thay vì LeaderboardService ---
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public JobPosting create(Long recruiterId, JobPostingRequest request) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter not found: " + recruiterId));
        
        // 1. Kiểm tra quyền
        if (recruiter.getUserRole() != UserRole.RECRUITER && recruiter.getUserRole() != UserRole.RECRUITER_VIP) {
             throw new RuntimeException("Only recruiter can create job postings");
        }

        // 2. Xử lý AI an toàn (Safe AI call)
        List<String> skills = new ArrayList<>();
        try {
            skills = geminiService.extractSkillsFromJob(request.getDescription(), request.getRequirements());
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất kỹ năng bằng AI (Vẫn tiếp tục tạo Job): {}", e.getMessage());
        }

        // 3. Convert LocalDate -> LocalDateTime
        LocalDateTime expiryDateTime = request.getExpiryDate().atTime(LocalTime.MAX);

        // 4. Tạo Entity
        JobPosting j = JobPosting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .salaryRange(request.getSalaryRange())
                .location(request.getLocation())
                .expiryDate(expiryDateTime)
                .extractedSkills(skills)
                .recruiter(recruiter)
                .status(JobStatus.PENDING)
                .build();

        JobPosting savedJob = jobPostingRepository.save(j);

        // --- MỚI: Bắn Event tính điểm ---
        try {
            eventPublisher.publishEvent(new PointEvent(
                this,
                recruiterId,
                "RECRUITER",
                UserPointAction.JOB_POST_APPROVED, // Sử dụng Enum chuẩn
                savedJob.getId() // RefId là ID của Job
            ));
        } catch (Exception e) {
            // Log lỗi nhưng không làm fail luồng tạo Job
            log.error("Lỗi bắn event tính điểm JOB_POST_APPROVED: {}", e.getMessage());
        }
        // -------------------------------------------------------

        return savedJob;
    }

    @Override
    @Transactional
    public JobPosting update(Long recruiterId, Long jobId, JobPostingRequest request) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new IllegalArgumentException("Unauthorized: cannot edit job of another recruiter");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setSalaryRange(request.getSalaryRange());
        job.setLocation(request.getLocation());

        if (request.getExpiryDate() != null) {
            job.setExpiryDate(request.getExpiryDate().atTime(LocalTime.MAX));
        }

        try {
            List<String> newSkills = geminiService.extractSkillsFromJob(request.getDescription(), request.getRequirements());
            job.setExtractedSkills(newSkills);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật kỹ năng bằng AI (Giữ nguyên kỹ năng cũ): {}", e.getMessage());
        }

        if (request.getStatus() != null) {
            try {
                job.setStatus(JobStatus.valueOf(request.getStatus()));
            } catch (Exception e) {
                log.warn("Invalid job status: {}", request.getStatus());
            }
        }
        return jobPostingRepository.save(job);
    }

    @Override
    @Transactional
    public void delete(Long recruiterId, Long jobId) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new IllegalArgumentException("Unauthorized: cannot delete job of another recruiter");
        }
        jobPostingRepository.delete(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobPosting> getById(Long id) {
        return jobPostingRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPostingResponse> listByRecruiter(Long recruiterId) {
        List<JobPosting> jobs = jobPostingRepository.findByRecruiterId(recruiterId);
        return jobs.stream()
                .map(recruitmentMapper::toJobPostingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> searchByTitle(String keyword) {
        return jobPostingRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPostingResponse> getAllJobPostings() {
        return jobPostingRepository.findAll().stream()
                .map(recruitmentMapper::toJobPostingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPostingResponse> searchJobs(String keyword) {
        List<JobPosting> jobs;
        if (keyword == null || keyword.trim().isEmpty()) {
            jobs = jobPostingRepository.findTop10ByStatusOrderByCreatedAtDesc(JobStatus.PUBLISHED);
        } else {
            jobs = jobPostingRepository.searchJobs(keyword.trim());
        }
        return jobs.stream()
                .map(recruitmentMapper::toJobPostingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobPostingResponse getJobDetailPublic(Long id) {
        JobPosting job = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

        return recruitmentMapper.toJobPostingResponse(job);
    }
}