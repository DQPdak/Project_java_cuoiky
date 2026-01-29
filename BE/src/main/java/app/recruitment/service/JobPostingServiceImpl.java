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
import org.slf4j.Logger;             // Import Logger
import org.slf4j.LoggerFactory;      // Import LoggerFactory
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostingServiceImpl implements JobPostingService {

    // KHAI BÁO LOG THỦ CÔNG (Sửa lỗi cannot find symbol log)
    private static final Logger log = LoggerFactory.getLogger(JobPostingServiceImpl.class);

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final RecruitmentMapper recruitmentMapper;
    private final GeminiService geminiService;

    @Override
    @Transactional
    public JobPosting create(Long recruiterId, JobPostingRequest request) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter not found: " + recruiterId));
        if (recruiter.getUserRole() != UserRole.RECRUITER) {
            throw new IllegalArgumentException("Only recruiter can create job postings");
        }
        List<String> skills = geminiService.extractSkillsFromJob(request.getDescription(), request.getRequirements());

        JobPosting j = JobPosting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .salaryRange(request.getSalaryRange())
                .location(request.getLocation())
                .expiryDate(request.getExpiryDate())
                .extractedSkills(skills)
                .recruiter(recruiter)
                .status(JobStatus.PENDING)
                .build();
        return jobPostingRepository.save(j);
    }

    @Override
    @Transactional
    public JobPosting update(Long recruiterId, Long jobId, JobPostingRequest request) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new IllegalArgumentException("Unauthorized: cannot edit job of another recruiter");
        }
        List<String> newSkills = geminiService.extractSkillsFromJob(request.getDescription(), request.getRequirements());

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setSalaryRange(request.getSalaryRange());
        job.setLocation(request.getLocation());
        job.setExtractedSkills(newSkills);
        job.setExpiryDate(request.getExpiryDate());

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
    public Optional<JobPosting> getById(Long id) {
        return jobPostingRepository.findById(id);
    }

    @Override
    public List<JobPosting> listByRecruiter(Long recruiterId) {
        return jobPostingRepository.findByRecruiterId(recruiterId);
    }

    @Override
    public List<JobPosting> searchByTitle(String keyword) {
        return jobPostingRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    public List<JobPostingResponse> getAllJobPostings() {
        return jobPostingRepository.findAll().stream()
                .map(recruitmentMapper::toJobPostingResponse)
                .collect(Collectors.toList());
    }

    @Override
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
}