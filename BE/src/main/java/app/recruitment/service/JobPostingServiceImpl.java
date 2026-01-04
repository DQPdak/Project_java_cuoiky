package app.recruitment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.recruitment.repository.JobPostingRepository;
import app.recruitment.entity.JobPosting;
import app.recruitment.dto.request.JobPostingRequest;
import app.auth.repository.UserRepository;
import app.auth.entity.User;
import app.auth.entity.enums.UserRole;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostingServiceImpl implements JobPostingService {
        private final JobPostingRepository repo;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public JobPosting create(Long recruiterId, JobPostingRequest request) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter not found: " + recruiterId));
        if (recruiter.getUserRole() != UserRole.RECRUITER) {
            throw new IllegalArgumentException("Only recruiter can create job postings");
        }
    
        JobPosting j = JobPosting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .salaryRange(request.getSalaryRange())
                .location(request.getLocation())
                .expiryDate(request.getExpiryDate())
                .recruiter(recruiter)
                .build();
        return repo.save(j);
    }
    
    @Override
    @Transactional
    public JobPosting update(Long recruiterId, Long jobId, JobPostingRequest request) {
        JobPosting job = repo.findById(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new IllegalArgumentException("Unauthorized: cannot edit job of another recruiter");
        }
        // cập nhật trường cơ bản
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setSalaryRange(request.getSalaryRange());
        job.setLocation(request.getLocation());
        job.setExpiryDate(request.getExpiryDate());
        // nếu request.status không null, set tương ứng (mismatch string -> giữ nguyên)
        if (request.getStatus() != null) {
            try {
                job.setStatus(app.recruitment.entity.enums.JobStatus.valueOf(request.getStatus()));
            } catch (Exception e) {
                log.warn("Invalid job status: {}", request.getStatus());
            }
        }
        return repo.save(job);
    }
    
    @Override
    @Transactional
    public void delete(Long recruiterId, Long jobId) {
        JobPosting job = repo.findById(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        if (!job.getRecruiter().getId().equals(recruiterId)) {
            throw new IllegalArgumentException("Unauthorized: cannot delete job of another recruiter");
        }
        repo.delete(job);
    }
    
    @Override
    public Optional<JobPosting> getById(Long id) {
        return repo.findById(id);
    }
    
    @Override
    public List<JobPosting> listByRecruiter(Long recruiterId) {
        return repo.findByRecruiterId(recruiterId);
    }
    
    @Override
    public List<JobPosting> searchByTitle(String keyword) {
        return repo.findByTitleContainingIgnoreCase(keyword);
    }
}