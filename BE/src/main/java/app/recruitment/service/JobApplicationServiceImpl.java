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
import app.recruitment.dto.response.JobApplicationResponse;
import app.auth.model.User;
import app.auth.model.enums.UserRole;
import app.auth.repository.UserRepository;
import app.recruitment.entity.enums.ApplicationStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                .status(ApplicationStatus.PENDING) // Nên set trạng thái mặc định
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
    @Transactional(readOnly = true) // Quan trọng để fetch dữ liệu từ bảng Job/Company
    public List<JobApplicationResponse> getApplicationsByCandidateId(Long studentId) {
        // 1. Lấy danh sách Entity từ DB
        List<JobApplication> applications = appRepo.findByStudentId(studentId);

        // 2. Convert sang DTO
        return applications.stream().map(app -> {
            JobPosting job = app.getJob();
            // Lấy tên công ty an toàn (tránh null pointer nếu data cũ lỗi)
            String companyName = (job.getCompany() != null) ? job.getCompany().getName() : "Unknown Company";

            return JobApplicationResponse.builder()
                    .id(app.getId())
                    .jobId(job.getId())
                    .jobTitle(job.getTitle())
                    .companyName(companyName) // Set tên công ty
                    .studentId(app.getStudent().getId())
                    .studentName(app.getStudent().getFullName()) // Giả sử User có getFullName
                    .cvUrl(app.getCvUrl())
                    .status(app.getStatus().name()) // Convert Enum sang String
                    .appliedAt(app.getAppliedAt())
                    .recruiterNote(app.getRecruiterNote())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<JobApplication> getById(Long id) {
        return appRepo.findById(id);
    }
}