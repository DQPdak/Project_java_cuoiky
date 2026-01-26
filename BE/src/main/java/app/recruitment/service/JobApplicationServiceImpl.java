package app.recruitment.service;

import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.recruitment.dto.request.JobApplicationRequest;
import app.recruitment.dto.response.JobApplicationResponse;
import app.recruitment.entity.CVAnalysisResult;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.JobPosting;
import app.recruitment.entity.enums.ApplicationStatus;
import app.recruitment.repository.CVAnalysisResultRepository;
import app.recruitment.repository.JobApplicationRepository;
import app.recruitment.repository.JobPostingRepository;
import app.ai.service.cv.gemini.dto.MatchResult;
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
    
    
    @Override
    @Transactional
    public JobApplication apply(Long candidateId, JobApplicationRequest request) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + candidateId));
        
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
        Optional<CVAnalysisResult> existingAnalysis = analysisResultRepo.findByUserIdAndJobPostingId(candidateId, job.getId());

        if (existingAnalysis.isPresent()) {
            CVAnalysisResult analysis = existingAnalysis.get();
            log.info("Found existing AI analysis for user {} and job {}. Copying data...", candidateId, job.getId());

            // 1. Copy điểm số %
            appBuilder.matchScore(analysis.getMatchPercentage());

            // 2. Parse JSON chi tiết để lấy các chỉ số thống kê và list kỹ năng
            if (analysis.getAnalysisDetails() != null) {
                try {
                    MatchResult matchResult = objectMapper.readValue(analysis.getAnalysisDetails(), MatchResult.class);
                    
                    if (matchResult != null) {
                        // --- Nhận xét tóm tắt ---
                        appBuilder.aiEvaluation(matchResult.getEvaluation()); 
                        
                        // --- Các con số thống kê ---
                        appBuilder.matchedSkillsCount(matchResult.getMatchedSkillsCount());
                        appBuilder.missingSkillsCount(matchResult.getMissingSkillsCount());
                        appBuilder.otherHardSkillsCount(matchResult.getOtherHardSkillsCount());
                        appBuilder.otherSoftSkillsCount(matchResult.getOtherSoftSkillsCount());
                        
                        // --- Danh sách kỹ năng (lưu dạng chuỗi để hiển thị nhanh trên bảng Recruiter) ---
                        
                        // Kỹ năng THIẾU
                        if (matchResult.getMissingSkillsList() != null) {
                            appBuilder.missingSkillsList(String.join(", ", matchResult.getMissingSkillsList()));
                        }
                        
                        // Kỹ năng ĐÁP ỨNG
                        if (matchResult.getMatchedSkillsList() != null) {
                            appBuilder.matchedSkillsList(String.join(", ", matchResult.getMatchedSkillsList()));
                        }

                        // Kỹ năng CHUYÊN MÔN KHÁC (Hard Skills)
                        if (matchResult.getOtherHardSkillsList() != null) {
                            appBuilder.otherHardSkillsList(String.join(", ", matchResult.getOtherHardSkillsList()));
                        }

                        // Kỹ năng MỀM KHÁC (Soft Skills)
                        if (matchResult.getOtherSoftSkillsList() != null) {
                            appBuilder.otherSoftSkillsList(String.join(", ", matchResult.getOtherSoftSkillsList()));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse analysisDetails JSON. Error: {}", e.getMessage());
                    // Không throw exception để quy trình apply vẫn tiếp tục dù lỗi parse AI
                }
            }
        } else {
            // Trường hợp chưa có phân tích trong DB -> Để mặc định 0
            log.info("No existing AI analysis found. Setting default score to 0.");
            appBuilder.matchScore(0);
        }

        return appRepo.save(appBuilder.build());
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

    // BE/src/main/java/app/recruitment/service/JobApplicationServiceImpl.java

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
                        
                        // Các trường AI
                        .matchScore(app.getMatchScore())
                        .aiEvaluation(app.getAiEvaluation())
                        .missingSkillsList(app.getMissingSkillsList()) // Giờ dòng này sẽ hết lỗi
                        
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
                    // Các trường AI cơ bản để hiển thị trong list
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
        // 1. Dùng hàm getById (của Repo) để lấy Entity
        JobApplication app = appRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ ID: " + id));

        // 2. Map từ Entity sang DTO (JobApplicationResponse)
        return JobApplicationResponse.builder()
                .id(app.getId())
                .jobId(app.getJobPosting().getId())
                .jobTitle(app.getJobPosting().getTitle())
                .studentId(app.getCandidate().getId())
                .studentName(app.getCandidate().getFullName())
                .cvUrl(app.getCvUrl())
                .status(app.getStatus().name())
                .appliedAt(app.getAppliedAt())
                
                // Map dữ liệu AI
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