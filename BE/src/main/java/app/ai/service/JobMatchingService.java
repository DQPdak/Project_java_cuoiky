package app.ai.service;

import app.ai.service.cv.gemini.GeminiService;
import app.ai.service.cv.gemini.dto.MatchResult;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
     * LUỒNG 1: Preview cho ứng viên (Cache -> AI -> Save Cache)
     */
    @Transactional
    public MatchResult matchCandidateWithJobAI(Long userId, String cvContent, Long jobId, String cvUrl) {
        // 1. Kiểm tra Cache
        Optional<CVAnalysisResult> existing = analysisRepository.findByUserIdAndJobPostingId(userId, jobId);
        if (existing.isPresent()) {
            try {
                return objectMapper.readValue(existing.get().getAnalysisDetails(), MatchResult.class);
            } catch (Exception e) {
                log.warn("Lỗi đọc cache AI. Sẽ phân tích lại.");
            }
        }

        // 2. Chuẩn bị dữ liệu
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 3. Gọi AI
        MatchResult result = geminiService.matchCVWithJob(
            cvContent,
            StringUtils.hasText(job.getDescription()) ? job.getDescription() : "",
            StringUtils.hasText(job.getRequirements()) ? job.getRequirements() : ""
        );

        // 4. Lưu Cache
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
            log.error("Không lưu được kết quả AI: {}", e.getMessage());
        }

        return result;
    }

    /**
     * LUỒNG 2: Sàng lọc hồ sơ (Bulk Screening)
     */
    @Async
    @Transactional
    public void screenApplications(Long jobId) {
        log.info("Bắt đầu sàng lọc hồ sơ cho Job ID: {}", jobId);

        List<JobApplication> applications = applicationRepository.findByJobPostingId(jobId);
        if (applications.isEmpty()) return;

        JobPosting job = jobPostingRepository.findById(jobId).orElseThrow();
        String jobDesc = StringUtils.hasText(job.getDescription()) ? job.getDescription() : "";
        String jobReq = StringUtils.hasText(job.getRequirements()) ? job.getRequirements() : "";

        for (JobApplication app : applications) {
            if (app.getMatchScore() == null || app.getMatchScore() == 0) {
                try {
                    CandidateProfile profile = profileRepository.findByUserId(app.getCandidate().getId()).orElse(null);

                    if (profile != null) {
                        String candidateData = buildCandidateDataForAI(profile);

                        // Gọi AI
                        MatchResult result = geminiService.matchCVWithJob(candidateData, jobDesc, jobReq);

                        // Map dữ liệu vào Entity
                        app.setMatchScore(result.getMatchPercentage());
                        app.setAiEvaluation(result.getEvaluation());
                        app.setMatchedSkillsCount(result.getMatchedSkillsCount());
                        app.setMissingSkillsCount(result.getMissingSkillsCount());
                        
                        // [FIX] Tính tổng 2 cột con để lưu vào trường Extra cũ của DB (nếu DB chưa tách cột)
                        int totalExtra = result.getOtherHardSkillsCount() + result.getOtherSoftSkillsCount();
                        app.setExtraSkillsCount(totalExtra); 
                        
                        app.setTotalRequiredSkills(result.getTotalRequiredSkills());

                        // [FIX] Lưu FULL dữ liệu 5 cột vào JSON Map
                        Map<String, Object> fullDataMap = new HashMap<>();
                        fullDataMap.put("learningPath", result.getLearningPath());
                        fullDataMap.put("careerAdvice", result.getCareerAdvice());
                        fullDataMap.put("matchedSkillsList", result.getMatchedSkillsList());
                        fullDataMap.put("missingSkillsList", result.getMissingSkillsList());
                        // 3 cột mới
                        fullDataMap.put("otherHardSkillsList", result.getOtherHardSkillsList());
                        fullDataMap.put("otherSoftSkillsList", result.getOtherSoftSkillsList());
                        fullDataMap.put("recommendedSkillsList", result.getRecommendedSkillsList());
                        
                        app.setLearningPath(objectMapper.writeValueAsString(fullDataMap));

                        // Lưu list missing riêng (nếu cần query)
                        if (result.getMissingSkillsList() != null) {
                            app.setMissingSkillsList(objectMapper.writeValueAsString(result.getMissingSkillsList()));
                        }

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
     * LUỒNG 3: Lấy danh sách xếp hạng
     */
    public List<JobApplication> getRankedApplications(Long jobId, Integer minScore) {
        int scoreThreshold = (minScore != null) ? minScore : 0;
        return applicationRepository.findByJobPostingIdAndMatchScoreGreaterThanEqualOrderByMatchScoreDesc(
                jobId, scoreThreshold
        );
    }

    /**
     * LUỒNG 4: Xem chi tiết (Reconstruct MatchResult từ DB)
     */
    @Transactional
    public MatchResult getApplicationAnalysis(Long applicationId) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        // 1. Kiểm tra DB
        if (StringUtils.hasText(app.getLearningPath())) {
            try {
                Map<String, Object> dataMap = objectMapper.readValue(app.getLearningPath(), new TypeReference<Map<String, Object>>(){});
                
                // [FIX] Builder khớp với DTO 5 cột mới
                return MatchResult.builder()
                        .matchPercentage(app.getMatchScore())
                        .evaluation(app.getAiEvaluation())
                        .totalRequiredSkills(app.getTotalRequiredSkills() != null ? app.getTotalRequiredSkills() : 0)
                        
                        .matchedSkillsList(objectMapper.convertValue(dataMap.get("matchedSkillsList"), new TypeReference<List<String>>(){}))
                        .missingSkillsList(objectMapper.convertValue(dataMap.get("missingSkillsList"), new TypeReference<List<String>>(){}))
                        
                        // Mapping 3 cột mới
                        .otherHardSkillsList(objectMapper.convertValue(dataMap.get("otherHardSkillsList"), new TypeReference<List<String>>(){}))
                        .otherSoftSkillsList(objectMapper.convertValue(dataMap.get("otherSoftSkillsList"), new TypeReference<List<String>>(){}))
                        .recommendedSkillsList(objectMapper.convertValue(dataMap.get("recommendedSkillsList"), new TypeReference<List<String>>(){}))
                        
                        .learningPath((String) dataMap.get("learningPath"))
                        .careerAdvice((String) dataMap.get("careerAdvice"))
                        
                        .matchedSkillsCount(app.getMatchedSkillsCount())
                        .missingSkillsCount(app.getMissingSkillsCount())
                        // Fix count
                        .otherHardSkillsCount(getListSize(dataMap.get("otherHardSkillsList")))
                        .otherSoftSkillsCount(getListSize(dataMap.get("otherSoftSkillsList")))
                        .recommendedSkillsCount(getListSize(dataMap.get("recommendedSkillsList")))
                        .build();

            } catch (Exception e) {
                log.warn("Dữ liệu cũ lỗi format, gọi AI phân tích lại...", e);
            }
        }

        // 2. Nếu chưa có, gọi AI
        try {
            CandidateProfile profile = profileRepository.findByUserId(app.getCandidate().getId())
                    .orElseThrow(() -> new RuntimeException("Ứng viên chưa cập nhật hồ sơ."));

            JobPosting job = app.getJobPosting();
            String candidateData = buildCandidateDataForAI(profile);

            MatchResult result = geminiService.matchCVWithJob(
                candidateData,
                StringUtils.hasText(job.getDescription()) ? job.getDescription() : "",
                StringUtils.hasText(job.getRequirements()) ? job.getRequirements() : ""
            );

            // Lưu DB
            app.setMatchScore(result.getMatchPercentage());
            app.setAiEvaluation(result.getEvaluation());
            app.setMatchedSkillsCount(result.getMatchedSkillsCount());
            app.setMissingSkillsCount(result.getMissingSkillsCount());
            app.setExtraSkillsCount(result.getOtherHardSkillsCount() + result.getOtherSoftSkillsCount()); // Gộp tạm
            app.setTotalRequiredSkills(result.getTotalRequiredSkills());

            // Lưu full JSON
            Map<String, Object> fullDataMap = new HashMap<>();
            fullDataMap.put("learningPath", result.getLearningPath());
            fullDataMap.put("careerAdvice", result.getCareerAdvice());
            fullDataMap.put("matchedSkillsList", result.getMatchedSkillsList());
            fullDataMap.put("missingSkillsList", result.getMissingSkillsList());
            fullDataMap.put("otherHardSkillsList", result.getOtherHardSkillsList());
            fullDataMap.put("otherSoftSkillsList", result.getOtherSoftSkillsList());
            fullDataMap.put("recommendedSkillsList", result.getRecommendedSkillsList());
            
            app.setLearningPath(objectMapper.writeValueAsString(fullDataMap));
            applicationRepository.save(app);

            return result;

        } catch (Exception e) {
            log.error("Lỗi phân tích đơn ID {}: ", applicationId, e);
            throw new RuntimeException("Lỗi phân tích: " + e.getMessage());
        }
    }

    // --- PRIVATE HELPER ---
    private int getListSize(Object listObj) {
        if (listObj instanceof List) {
            return ((List<?>) listObj).size();
        }
        return 0;
    }

    private String buildCandidateDataForAI(CandidateProfile profile) throws Exception {
        Map<String, Object> aiInputMap = new HashMap<>();
        aiInputMap.put("fullName", profile.getFullName());
        aiInputMap.put("aboutMe", StringUtils.hasText(profile.getAboutMe()) ? profile.getAboutMe() : "Không có mô tả"); 
        
        if (StringUtils.hasText(profile.getEducationJson())) {
            try {
                aiInputMap.put("education", objectMapper.readTree(profile.getEducationJson()));
            } catch (Exception e) {
                aiInputMap.put("education", profile.getEducationJson()); 
            }
        }

        if (profile.getSkills() != null) aiInputMap.put("skills", profile.getSkills());

        if (profile.getExperiences() != null) {
            var expList = profile.getExperiences().stream().map(exp -> {
                Map<String, Object> eMap = new HashMap<>();
                eMap.put("companyName", exp.getCompany());
                eMap.put("role", exp.getRole());
                eMap.put("description", exp.getDescription());
                eMap.put("startDate", exp.getStartDate() != null ? exp.getStartDate().toString() : "");
                eMap.put("endDate", exp.getEndDate() != null ? exp.getEndDate().toString() : "Present");
                return eMap;
            }).collect(Collectors.toList());
            aiInputMap.put("experiences", expList);
        }
        return objectMapper.writeValueAsString(aiInputMap);
    }
}