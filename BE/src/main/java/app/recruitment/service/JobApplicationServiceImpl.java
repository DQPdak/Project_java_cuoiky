package app.recruitment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.recruitment.repository.JobApplicationRepository;
import app.recruitment.repository.JobPostingRepository;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.JobPosting;
import app.recruitment.dto.request.JobApplicationRequest;
import app.auth.model.User;
import app.auth.model.enums.UserRole;
import app.auth.repository.UserRepository;
import app.recruitment.entity.enums.ApplicationStatus;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository appRepo;
    private final JobPostingRepository jobRepo;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public JobApplication apply(Long studentId, JobApplicationRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        if (student.getUserRole() != UserRole.CANDIDATE) {
            throw new IllegalArgumentException("Only candidates can apply");
        }
        JobPosting job = jobRepo.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + request.getJobId()));

        // Kiểm tra đã ứng tuyển chưa
        if (appRepo.findByJobIdAndStudentId(job.getId(), studentId).isPresent()) {
            throw new IllegalArgumentException("Already applied");
        }

        JobApplication a = JobApplication.builder()
                .job(job)
                .student(student)
                .cvUrl(request.getCvUrl())
                .build();
        return appRepo.save(a);
    }

    @Override
    @Transactional
    public JobApplication updateStatus(Long recruiterId, Long applicationId, ApplicationStatus newStatus, String recruiterNote) {
        JobApplication application = appRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        // chỉ recruiter quản lý job mới được update
        Long ownerId = application.getJob().getRecruiter().getId();
        if (!ownerId.equals(recruiterId)) {
            throw new IllegalArgumentException("Unauthorized: cannot update application of other recruiter's job");
        }

        application.setStatus(newStatus);
        application.setRecruiterNote(recruiterNote);
        return appRepo.save(application);
    }

    @Override
    public List<JobApplication> listByJob(Long jobId) {
        return appRepo.findByJobId(jobId);
    }

    @Override
    public List<JobApplication> listByStudent(Long studentId) {
        return appRepo.findByStudentId(studentId);
    }

    @Override
    public Optional<JobApplication> getById(Long id) {
        return appRepo.findById(id);
    }
}