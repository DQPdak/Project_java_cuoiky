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
    private final CandidateProfileRepository profileRepository; // Inject thêm để lấy CV từ profile an toàn hơn
    
    @Override
    @Transactional
    public JobApplication apply(Long candidateId, JobApplicationRequest request) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + candidateId));
        
        // Check role (nếu cần thiết)
        // if (candidate.getUserRole() != UserRole.CANDIDATE) ...

        JobPosting job = jobRepo.findById(request.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + request.getJobId()));

        if (appRepo.existsByCandidateIdAndJobPostingId(candidateId, job.getId())) {
            throw new IllegalArgumentException("Bạn đã ứng tuyển công việc này rồi.");
        }

        // --- LOGIC LẤY LINK CV ---
        String finalCvUrl = request.getCvUrl();
        
        // Nếu user không gửi link CV trong request, thử lấy từ Profile gốc
        if (finalCvUrl == null || finalCvUrl.isEmpty()) {
            CandidateProfile profile = profileRepository.findByUserId(candidateId).orElse(null);
            if (profile != null && profile.getCvFilePath() != null) {
                finalCvUrl = profile.getCvFilePath(); // Lấy từ biến cvFilePath của Entity CandidateProfile
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

    @Override
    public List<JobApplication> listByJob(Long jobId) {
        return appRepo.findByJobPostingId(jobId);
    }

    // [FIX] Override đúng tên hàm trong Interface
    @Override
    public List<JobApplication> listByCandidateId(Long candidateId) {
        return appRepo.findByCandidateId(candidateId);
    }

    // [FIX] Override đúng tên hàm trong Interface
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
                    
                    // [FIX LỖI 2] Convert Enum sang String bằng .name()
                    .status(app.getStatus().name()) 
                    
                    .appliedAt(app.getAppliedAt())
                    .recruiterNote(app.getRecruiterNote())
                    .matchScore(app.getMatchScore())
                    .aiEvaluation(app.getAiEvaluation())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<JobApplication> getById(Long id) {
        return appRepo.findById(id);
    }
}