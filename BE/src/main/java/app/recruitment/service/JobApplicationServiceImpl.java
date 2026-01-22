package app.recruitment.service;

import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.recruitment.dto.request.JobApplicationRequest;
import app.recruitment.dto.response.JobApplicationResponse;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.JobPosting;
import app.recruitment.entity.enums.ApplicationStatus;
import app.recruitment.repository.JobApplicationRepository;
import app.recruitment.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.recruitment.repository.JobApplicationRepository;
import app.auth.exception.UnauthorizedException;

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
    private final CandidateProfileRepository profileRepository; 

    // 1. Logic ứng viên nộp đơn
    @Override
    @Transactional
    public JobApplication apply(Long candidateId, JobApplicationRequest request) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + candidateId));
        
        JobPosting job = jobRepo.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + request.getJobId()));

        // Check trùng đơn
        if (appRepo.existsByCandidateIdAndJobPostingId(candidateId, job.getId())) {
            throw new IllegalArgumentException("Bạn đã ứng tuyển công việc này rồi.");
        }

        // Logic CV: Ưu tiên link gửi lên, nếu null thì lấy từ Profile
        String finalCvUrl = request.getCvUrl();
        if (finalCvUrl == null || finalCvUrl.isEmpty()) {
            CandidateProfile profile = profileRepository.findByUserId(candidateId).orElse(null);
            if (profile != null && profile.getCvFilePath() != null) {
                finalCvUrl = profile.getCvFilePath();
            } else {
                throw new IllegalArgumentException("Vui lòng upload CV hoặc cập nhật hồ sơ trước khi ứng tuyển.");
            }
        }

        JobApplication a = JobApplication.builder()
                .jobPosting(job)
                .candidate(candidate)
                .cvUrl(finalCvUrl)
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.PENDING)
                .matchScore(0) 
                .build();

        return appRepo.save(a);
    }

    // 2. Logic Recruiter duyệt đơn (Giữ nguyên: check quyền recruiter)
    @Override
    @Transactional
    public JobApplication updateStatus(Long recruiterId, Long applicationId, ApplicationStatus newStatus, String recruiterNote) {
        JobApplication application = appRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (!application.getJobPosting().getRecruiter().getId().equals(recruiterId)) {
            throw new IllegalArgumentException("Không có quyền chỉnh sửa đơn ứng tuyển này.");
        }

        application.setStatus(newStatus);
        application.setRecruiterNote(recruiterNote);
        return appRepo.save(application);
    }

    // 3. Helper: Lấy List Entity theo Job
    @Override
    public List<JobApplication> listByJob(Long jobId) {
        return appRepo.findByJobPostingId(jobId);
    }

    // 4. Helper: Lấy List Entity theo Candidate
    @Override
    public List<JobApplication> listByCandidateId(Long candidateId) {
        return appRepo.findByCandidateId(candidateId);
    }

    // 5. API: Lấy danh sách DTO cho Candidate (Giữ logic Map chi tiết của bạn)
    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getApplicationsByCandidateId(Long candidateId) {
        List<JobApplication> applications = appRepo.findByCandidateId(candidateId);

        return applications.stream().map(app -> {
            JobPosting job = app.getJobPosting();
            String companyName = (job.getCompany() != null) ? job.getCompany().getName() : "Unknown Company";

            return JobApplicationResponse.builder()
                    .id(app.getId())
                    .jobId(job.getId())
                    .jobTitle(job.getTitle())
                    .companyName(companyName)
                    .studentId(app.getCandidate().getId())
                    .studentName(app.getCandidate().getFullName())
                    .cvUrl(app.getCvUrl())
                    .status(app.getStatus().name()) 
                    .appliedAt(app.getAppliedAt())
                    .recruiterNote(app.getRecruiterNote())
                    .matchScore(app.getMatchScore())
                    .aiEvaluation(app.getAiEvaluation())
                    .build();
        }).collect(Collectors.toList());
    }

    // 6. Helper: Lấy chi tiết
    @Override
    public Optional<JobApplication> getById(Long id) {
        return appRepo.findById(id);
    }


    // 7. API: Lấy danh sách DTO cho Recruiter (Xem theo Job)
    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getApplicationsByJobId(Long jobId) {
        // Tái sử dụng logic map tương tự như Candidate
        List<JobApplication> applications = appRepo.findByJobPostingId(jobId);
        return applications.stream().map(app -> {
            JobPosting job = app.getJobPosting();
            String companyName = (job.getCompany() != null) ? job.getCompany().getName() : "Unknown";
            
            return JobApplicationResponse.builder()
                    .id(app.getId())
                    .jobId(job.getId())
                    .jobTitle(job.getTitle())
                    .companyName(companyName)
                    .studentId(app.getCandidate().getId())
                    .studentName(app.getCandidate().getFullName())
                    .cvUrl(app.getCvUrl())
                    .status(app.getStatus().name())
                    .appliedAt(app.getAppliedAt())
                    .matchScore(app.getMatchScore())
                    .build();
        }).collect(Collectors.toList());
    }

    // 8. Update status đơn giản (dùng cho AI/System tool)
    @Override
    @Transactional
    public void updateApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
        JobApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        app.setStatus(newStatus);
        appRepo.save(app);
    }

    // 9. Xóa đơn (Candidate rút đơn)
    @Override
    @Transactional
    public void deleteApplication(Long candidateId, Long applicationId) {
        JobApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        
        // Kiểm tra quyền sở hữu trước khi xóa
        if (!app.getCandidate().getId().equals(candidateId)) {
             throw new IllegalArgumentException("Bạn không có quyền xóa đơn ứng tuyển này.");
        }
        if (app.getStatus() != ApplicationStatus.PENDING) {
             throw new IllegalArgumentException("Không thể hủy đơn khi đã được Duyệt hoặc Từ chối.");
        }
        appRepo.delete(app);
    }
}