package app.recruitment.service;

import app.ai.service.cv.gemini.dto.MatchResult;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.gamification.service.LeaderboardService;
import app.recruitment.dto.request.JobApplicationRequest;
import app.recruitment.dto.response.JobApplicationResponse;
import app.recruitment.entity.CVAnalysisResult;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.JobPosting;
import app.recruitment.entity.enums.ApplicationStatus;
import app.recruitment.repository.CVAnalysisResultRepository;
import app.recruitment.repository.JobApplicationRepository;
import app.recruitment.repository.JobPostingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final CandidateProfileRepository profileRepository;

    // Inject thêm để lấy kết quả AI đã cache
    private final CVAnalysisResultRepository analysisResultRepo;
    private final ObjectMapper objectMapper;

    // ✅ Leaderboard
    private final LeaderboardService leaderboardService;

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

        // --- BUILD JOB APPLICATION ---
        JobApplication.JobApplicationBuilder appBuilder = JobApplication.builder()
                .jobPosting(job)
                .candidate(candidate)
                .cvUrl(finalCvUrl)
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.PENDING);

        // --- [LOGIC MỚI] KIỂM TRA & COPY KẾT QUẢ AI TỪ BẢNG ANALYSIS ---
        Optional<CVAnalysisResult> existingAnalysis =
                analysisResultRepo.findByUserIdAndJobPostingId(candidateId, job.getId());

        if (existingAnalysis.isPresent()) {
            CVAnalysisResult analysis = existingAnalysis.get();
            log.info("Found existing AI analysis for user {} and job {}. Copying data...", candidateId, job.getId());

            // 1. Copy điểm số %
            appBuilder.matchScore(analysis.getMatchPercentage());

            // 2. Parse JSON chi tiết
            if (analysis.getAnalysisDetails() != null) {
                try {
                    MatchResult matchResult =
                            objectMapper.readValue(analysis.getAnalysisDetails(), MatchResult.class);

                    if (matchResult != null) {
                        appBuilder.aiEvaluation(matchResult.getEvaluation());

                        appBuilder.matchedSkillsCount(matchResult.getMatchedSkillsCount());
                        appBuilder.missingSkillsCount(matchResult.getMissingSkillsCount());
                        appBuilder.otherHardSkillsCount(matchResult.getOtherHardSkillsCount());
                        appBuilder.otherSoftSkillsCount(matchResult.getOtherSoftSkillsCount());

                        if (matchResult.getMissingSkillsList() != null) {
                            appBuilder.missingSkillsList(String.join(", ", matchResult.getMissingSkillsList()));
                        }
                        if (matchResult.getMatchedSkillsList() != null) {
                            appBuilder.matchedSkillsList(String.join(", ", matchResult.getMatchedSkillsList()));
                        }
                        if (matchResult.getOtherHardSkillsList() != null) {
                            appBuilder.otherHardSkillsList(String.join(", ", matchResult.getOtherHardSkillsList()));
                        }
                        if (matchResult.getOtherSoftSkillsList() != null) {
                            appBuilder.otherSoftSkillsList(String.join(", ", matchResult.getOtherSoftSkillsList()));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse analysisDetails JSON. Error: {}", e.getMessage());
                }
            }
        } else {
            log.info("No existing AI analysis found. Setting default score to 0.");
            appBuilder.matchScore(0);
        }

        // ✅ Lưu application trước để lấy applicationId làm refId
        JobApplication saved = appRepo.save(appBuilder.build());

        // ✅ CỘNG ĐIỂM LEADERBOARD khi apply thành công
        // user_role lấy từ User entity (đúng users.user_role)
        String userRole = String.valueOf(candidate.getUserRole()); // enum/string đều ok
        boolean added = leaderboardService.addPoints(
                candidateId,
                userRole,
                "APPLY",
                10,
                saved.getId()
        );
        log.info("Leaderboard addPoints APPLY for candidateId={} appId={} result={}",
                candidateId, saved.getId(), added);

        return saved;
    }

    // 2. Logic Recruiter duyệt đơn
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

        JobApplication saved = appRepo.save(application);

        // ✅ CỘNG ĐIỂM LEADERBOARD cho recruiter khi xử lý đơn
        int points = switch (newStatus) {
            case SCREENING -> 1;
            case INTERVIEW -> 3;
            case OFFER -> 5;
            case REJECTED -> 1;
            default -> 0; // APPLIED, PENDING
        };

        if (points > 0) {
            Long recruiterUserId = saved.getJobPosting().getRecruiter().getId();

            String recruiterRole = String.valueOf(
                    userRepository.findById(recruiterUserId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + recruiterUserId))
                            .getUserRole()
            );

            String actionType = "APP_STATUS_" + newStatus.name();

            boolean added = leaderboardService.addPoints(
                    recruiterUserId,
                    recruiterRole,
                    actionType,
                    points,
                    saved.getId() // refId = applicationId
            );

            log.info("Leaderboard addPoints {} (+{}) recruiterId={} appId={} result={}",
                    actionType, points, recruiterUserId, saved.getId(), added);
        }

        return saved;
    }
    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationResponse> listByJob(Long jobId) {
        List<JobApplication> apps = appRepo.findByJobPostingId(jobId);

        return apps.stream()
                .map(app -> JobApplicationResponse.builder()
                        .id(app.getId())
                        .jobId(app.getJobPosting().getId())
                        .jobTitle(app.getJobPosting().getTitle())
                        .companyName(app.getJobPosting().getCompany().getName())
                        .studentId(app.getCandidate().getId())
                        .studentName(app.getCandidate().getFullName())
                        .cvUrl(app.getCvUrl())
                        .status(app.getStatus().name())
                        .appliedAt(app.getAppliedAt())
                        .matchScore(app.getMatchScore())
                        .aiEvaluation(app.getAiEvaluation())
                        .missingSkillsList(app.getMissingSkillsList())
                        .recruiterNote(app.getRecruiterNote())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<JobApplication> listByCandidateId(Long candidateId) {
        return appRepo.findByCandidateId(candidateId);
    }

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

    @Override
    public Optional<JobApplication> getById(Long id) {
        return appRepo.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public JobApplicationResponse getDetail(Long id) {
        JobApplication app = appRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ ID: " + id));

        return JobApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJobPosting().getId())
                .jobTitle(app.getJobPosting().getTitle())
                .studentId(app.getCandidate().getId())
                .studentName(app.getCandidate().getFullName())
                .cvUrl(app.getCvUrl())
                .status(app.getStatus().name())
                .appliedAt(app.getAppliedAt())
                .matchScore(app.getMatchScore() != null ? app.getMatchScore() : 0)
                .aiEvaluation(app.getAiEvaluation() != null ? app.getAiEvaluation() : "Chưa có đánh giá")
                .missingSkillsList(app.getMissingSkillsList())
                .matchedSkillsList(app.getMatchedSkillsList())
                .otherHardSkillsList(app.getOtherHardSkillsList())
                .otherSoftSkillsList(app.getOtherSoftSkillsList())
                .matchedSkillsCount(app.getMatchedSkillsCount() != null ? app.getMatchedSkillsCount() : 0)
                .missingSkillsCount(app.getMissingSkillsCount() != null ? app.getMissingSkillsCount() : 0)
                .otherHardSkillsCount(app.getOtherHardSkillsCount() != null ? app.getOtherHardSkillsCount() : 0)
                .otherSoftSkillsCount(app.getOtherSoftSkillsCount() != null ? app.getOtherSoftSkillsCount() : 0)
                .recruiterNote(app.getRecruiterNote())
                .build();
    }
}
