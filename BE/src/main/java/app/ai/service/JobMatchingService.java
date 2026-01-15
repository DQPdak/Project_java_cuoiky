package app.ai.service;

import app.ai.service.cv.gemini.GeminiService;
import app.ai.service.cv.gemini.dto.MatchResult;
import app.ai.service.cv.gemini.dto.analysis.CareerAdviceResult;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.recruitment.entity.CVAnalysisResult;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.JobPosting;
import app.recruitment.repository.CVAnalysisResultRepository;
import app.recruitment.repository.JobApplicationRepository;
import app.recruitment.repository.JobPostingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMatchingService {

    private final GeminiService geminiService;
    private final JobApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateProfileRepository profileRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final CVAnalysisResultRepository analysisRepository;

    /**
     * LUỒNG 1: Preview cho ứng viên dựa trên file CV (đã upload và trích xuất text)
     * Có lưu Cache để tiết kiệm Token.
     */
    @Transactional
    public MatchResult matchCandidateWithJobAI(Long userId, String cvContent, Long jobId, String cvUrl) {
        // A. Kiểm tra Cache
        Optional<CVAnalysisResult> existing = analysisRepository.findByUserIdAndJobPostingId(userId, jobId);
        if (existing.isPresent()) {
            try {
                return objectMapper.readValue(existing.get().getAnalysisDetails(), MatchResult.class);
            } catch (Exception e) {
                log.warn("Lỗi đọc cache AI, sẽ phân tích lại...");
            }
        }

        // B. Gọi AI (Dùng hàm Unified mới)
        JobPosting job = jobPostingRepository.findById(jobId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        
        // [UPDATE] Gọi hàm matchCVWithJob với 3 tham số
        MatchResult result = geminiService.matchCVWithJob(
            cvContent, 
            job.getDescription(), 
            job.getRequirements()
        );

        // C. Lưu Cache
        try {
            CVAnalysisResult entity = existing.orElse(new CVAnalysisResult());
            entity.setUser(user);
            entity.setJobPosting(job);
            entity.setMatchPercentage(result.getMatchPercentage());
            entity.setCvUrlUsed(cvUrl);
            entity.setAnalysisDetails(objectMapper.writeValueAsString(result));
            entity.setAnalyzedAt(java.time.LocalDateTime.now());
            
            analysisRepository.save(entity);
        } catch (Exception e) {
            log.error("Không lưu được kết quả AI: " + e.getMessage());
        }

        return result;
    }

    /**
     * LUỒNG 2: Preview cho ứng viên dựa trên Profile Database (Không dùng file PDF)
     */
    public MatchResult previewMatch(Long candidateId, Long jobId) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        CandidateProfile profile = profileRepository.findByUserId(candidateId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa cập nhật hồ sơ/CV"));

        try {
            // Build dữ liệu Profile thành chuỗi JSON (coi như cvText)
            String candidateData = buildCandidateDataForAI(profile);

            // [UPDATE] Gọi hàm matchCVWithJob
            return geminiService.matchCVWithJob(
                candidateData, 
                job.getDescription(), 
                job.getRequirements()
            );
        } catch (Exception e) {
            log.error("Lỗi preview match: ", e);
            throw new RuntimeException("Lỗi AI chi tiết: " + e.getMessage());
        }
    }

    /**
     * LUỒNG 3: Sàng lọc cho Recruiter (Bulk Screening)
     * Chấm điểm hàng loạt ứng viên và lưu vào JobApplication.
     */
    @Async
    @Transactional
    public void screenApplications(Long jobId) {
        log.info("Bắt đầu sàng lọc hồ sơ cho Job ID: {}", jobId);

        List<JobApplication> applications = applicationRepository.findByJobPostingId(jobId);
        if (applications.isEmpty()) return;

        JobPosting job = jobPostingRepository.findById(jobId).orElseThrow();

        for (JobApplication app : applications) {
            // Chỉ tính những đơn chưa có điểm
            if (app.getMatchScore() == null || app.getMatchScore() == 0) {
                try {
                    CandidateProfile profile = profileRepository.findByUserId(app.getCandidate().getId()).orElse(null);

                    if (profile != null) {
                        String candidateData = buildCandidateDataForAI(profile);
                        
                        // [UPDATE] Gọi hàm matchCVWithJob thay vì matchJob (đã xóa)
                        MatchResult result = geminiService.matchCVWithJob(
                            candidateData, 
                            job.getDescription(), 
                            job.getRequirements()
                        );

                        // Map dữ liệu từ DTO sang Entity
                        app.setMatchScore(result.getMatchPercentage());
                        app.setAiEvaluation(result.getEvaluation()); // Nhận xét Tiếng Việt
                        
                        app.setMatchedSkillsCount(result.getMatchedSkillsCount());
                        app.setMissingSkillsCount(result.getMissingSkillsCount());
                        app.setExtraSkillsCount(result.getExtraSkillsCount());
                        app.setTotalRequiredSkills(result.getTotalRequiredSkills());

                        // Lưu danh sách skill thiếu vào DB (JSON String)
                        if (result.getMissingSkillsList() != null) {
                            String jsonMissing = objectMapper.writeValueAsString(result.getMissingSkillsList());
                            app.setMissingSkillsList(jsonMissing);
                        }
                        
                        // Nếu muốn lưu cả Learning Path vào DB luôn (tùy chọn)
                        // app.setLearningPath(objectMapper.writeValueAsString(convertMatchToAdvice(result)));

                        applicationRepository.save(app);
                        log.info("Đã chấm điểm xong đơn ID: {}", app.getId());
                    }
                } catch (Exception e) {
                    log.error("Lỗi chấm điểm đơn ID {}: {}", app.getId(), e.getMessage());
                }
            }
        }
        log.info("Hoàn tất sàng lọc Job ID: {}", jobId);
    }

    /**
     * LUỒNG 4: Lấy danh sách xếp hạng (Ranking)
     */
    public List<JobApplication> getRankedApplications(Long jobId, Integer minScore) {
        int scoreThreshold = (minScore != null) ? minScore : 0;
        return applicationRepository.findByJobPostingIdAndMatchScoreGreaterThanEqualOrderByMatchScoreDesc(
                jobId,
                scoreThreshold
        );
    }

    /**
     * LUỒNG 5: Gợi ý lộ trình (Career Advice)
     * Logic: Gọi hàm All-in-One, sau đó trích xuất phần Lộ trình.
     */
    @Transactional
    public CareerAdviceResult getCareerAdvice(Long applicationId) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        // 1. Kiểm tra cache trong DB
        if (app.getLearningPath() != null && !app.getLearningPath().isEmpty()) {
             try {
                 return objectMapper.readValue(app.getLearningPath(), CareerAdviceResult.class);
             } catch (Exception e) {
                 log.warn("Dữ liệu lộ trình cũ lỗi, tạo lại...");
             }
        }

        // 2. Gọi AI nếu chưa có
        try {
            Long candidateId = app.getCandidate().getId();
            CandidateProfile profile = profileRepository.findByUserId(candidateId)
                    .orElseThrow(() -> new RuntimeException("Ứng viên chưa cập nhật hồ sơ."));

            JobPosting job = app.getJobPosting();
            String candidateData = buildCandidateDataForAI(profile);

            // [UPDATE] Gọi hàm All-in-One
            MatchResult matchResult = geminiService.matchCVWithJob(
                candidateData, 
                job.getDescription(), 
                job.getRequirements()
            );

            // [UPDATE] Chuyển đổi MatchResult -> CareerAdviceResult
            CareerAdviceResult adviceResult = convertMatchToAdvice(matchResult);

            // Lưu vào DB
            String jsonResult = objectMapper.writeValueAsString(adviceResult);
            app.setLearningPath(jsonResult);
            applicationRepository.save(app);

            return adviceResult;

        } catch (Exception e) {
            log.error("Lỗi getCareerAdvice ID {}: ", applicationId, e);
            throw new RuntimeException("Không thể tạo lộ trình: " + e.getMessage());
        }
    }

    // --- PRIVATE HELPER ---

    private String buildCandidateDataForAI(CandidateProfile profile) throws Exception {
        Map<String, Object> aiInputMap = new HashMap<>();
        aiInputMap.put("fullName", profile.getFullName());
        aiInputMap.put("aboutMe", profile.getAboutMe());
        aiInputMap.put("education", profile.getEducationJson());

        if (profile.getSkills() != null) {
            aiInputMap.put("skills", new ArrayList<>(profile.getSkills()));
        }

        if (profile.getExperiences() != null) {
            var expList = profile.getExperiences().stream().map(exp -> {
                Map<String, Object> eMap = new HashMap<>();
                eMap.put("companyName", exp.getCompany());
                eMap.put("role", exp.getRole());
                eMap.put("description", exp.getDescription());
                eMap.put("startDate", exp.getStartDate());
                eMap.put("endDate", exp.getEndDate());
                return eMap;
            }).collect(Collectors.toList());
            aiInputMap.put("experiences", expList);
        }
        return objectMapper.writeValueAsString(aiInputMap);
    }
    
    // Helper chuyển đổi kết quả gộp sang DTO Advice
    private CareerAdviceResult convertMatchToAdvice(MatchResult matchResult) {
        CareerAdviceResult advice = new CareerAdviceResult();
        advice.setMissingSkills(matchResult.getMissingSkillsList());
        advice.setLearningPath(matchResult.getLearningPath());
        advice.setCareerAdvice(matchResult.getCareerAdvice());
        advice.setEstimatedDuration("4 tuần (Tham khảo)"); // Có thể update prompt để AI trả về trường này sau
        return advice;
    }
}