package app.ai.service;

import app.ai.service.cv.gemini.GeminiService;
import app.ai.service.cv.gemini.dto.MatchResult;
import app.ai.service.cv.gemini.dto.analysis.CareerAdviceResult;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.JobPosting;
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

    // --- PHẦN 1: TÍNH TOÁN (WRITE) ---

    /**
     * LUỒNG 1: Preview cho ứng viên (Không lưu DB)
     */
    public MatchResult previewMatch(Long candidateId, Long jobId) {
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        CandidateProfile profile = profileRepository.findByUserId(candidateId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa cập nhật hồ sơ/CV"));

        try {
            // Sử dụng hàm helper để build dữ liệu chuẩn (đầy đủ Skills)
            String candidateData = buildCandidateDataForAI(profile);
            String jobData = job.getDescription() + "\n" + job.getRequirements();

            // Gọi AI
            return geminiService.matchJob(candidateData, jobData);
        } catch (Exception e) {
            log.error("Lỗi preview match: ", e);
            throw new RuntimeException("Lỗi AI chi tiết: " + e.getMessage());
        }
    }

    /**
     * LUỒNG 2: Sàng lọc cho Recruiter (Lưu DB)
     * [ĐÃ SỬA] Áp dụng logic build manual map để không bị mất Skills
     */
    @Async
    @Transactional
    public void screenApplications(Long jobId) {
        log.info("Bắt đầu sàng lọc hồ sơ cho Job ID: {}", jobId);

        List<JobApplication> applications = applicationRepository.findByJobPostingId(jobId);
        if (applications.isEmpty()) return;

        JobPosting job = jobPostingRepository.findById(jobId).orElseThrow();
        String jobData = job.getDescription() + "\n" + job.getRequirements();

        for (JobApplication app : applications) {
            // Chỉ tính những đơn chưa có điểm (hoặc muốn tính lại thì bỏ điều kiện này)
            if (app.getMatchScore() == null || app.getMatchScore() == 0) {
                try {
                    CandidateProfile profile = profileRepository.findByUserId(app.getCandidate().getId()).orElse(null);

                    if (profile != null) {
                        // [QUAN TRỌNG] Dùng hàm helper để lấy đủ Skills
                        String candidateData = buildCandidateDataForAI(profile);
                        
                        // Gọi AI
                        MatchResult result = geminiService.matchJob(candidateData, jobData);

                        // Map dữ liệu từ DTO sang Entity
                        app.setMatchScore(result.getMatchPercentage());
                        app.setAiEvaluation(result.getEvaluation());
                        app.setMatchedSkillsCount(result.getMatchedSkillsCount());
                        app.setMissingSkillsCount(result.getMissingSkillsCount());
                        app.setExtraSkillsCount(result.getExtraSkillsCount());
                        app.setTotalRequiredSkills(result.getTotalRequiredSkills());

                        // Convert List<String> thành JSON String để lưu DB
                        if (result.getMissingSkillsList() != null) {
                            String jsonMissing = objectMapper.writeValueAsString(result.getMissingSkillsList());
                            app.setMissingSkillsList(jsonMissing);
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

    // --- PHẦN 2: LẤY DỮ LIỆU (READ) ---

    /**
     * LUỒNG 3: Lấy danh sách xếp hạng
     */
    public List<JobApplication> getRankedApplications(Long jobId, Integer minScore) {
        int scoreThreshold = (minScore != null) ? minScore : 0;
        return applicationRepository.findByJobPostingIdAndMatchScoreGreaterThanEqualOrderByMatchScoreDesc(
                jobId,
                scoreThreshold
        );
    }

    // --- PRIVATE HELPER (TRÁNH LẶP CODE) ---

    /**
     * Hàm dùng chung để chuyển Profile thành JSON cho AI.
     * Khắc phục lỗi @JsonIgnore làm mất Skills.
     */
    private String buildCandidateDataForAI(CandidateProfile profile) throws Exception {
        Map<String, Object> aiInputMap = new HashMap<>();

        // 1. Thông tin cơ bản
        aiInputMap.put("fullName", profile.getFullName());
        aiInputMap.put("aboutMe", profile.getAboutMe());
        aiInputMap.put("education", profile.getEducationJson());

        // 2. Lấy Skills (Fix lỗi Lazy Loading & JsonIgnore)
        if (profile.getSkills() != null) {
            // New ArrayList để ép Hibernate load dữ liệu ngay lập tức
            aiInputMap.put("skills", new ArrayList<>(profile.getSkills()));
        }

        // 3. Lấy Kinh nghiệm (Map thủ công để sạch dữ liệu)
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

        String json = objectMapper.writeValueAsString(aiInputMap);
        // log.info("DATA GỬI AI (Sample): {}", json); // Uncomment nếu muốn debug
        return json;
    }

    /**
     * LUỒNG MỚI: Gợi ý lộ trình (User bấm nút mới chạy)
     * Lưu kết quả vào DB để lần sau user bấm không mất tiền gọi lại AI
     */
    @Transactional
    public CareerAdviceResult getCareerAdvice(Long applicationId) {
        // 1. Lấy thông tin đơn ứng tuyển
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));

        // 2. Kiểm tra xem đã có lộ trình trong DB chưa (Tiết kiệm tiền!)
        if (app.getLearningPath() != null && !app.getLearningPath().isEmpty()) {
             try {
                 // Nếu có rồi thì lấy từ DB ra trả về luôn
                 return objectMapper.readValue(app.getLearningPath(), CareerAdviceResult.class);
             } catch (Exception e) {
                 log.warn("Dữ liệu lộ trình cũ bị lỗi format, hệ thống sẽ tạo lại mới...");
             }
        }

        // 3. Nếu chưa có, chuẩn bị dữ liệu gọi AI
        try {
            // [FIX LỖI QUAN TRỌNG]
            // Không gọi app.getCandidate().getProfile() vì User entity chưa map.
            // Dùng Repository tìm Profile theo User ID.
            Long candidateId = app.getCandidate().getId();
            CandidateProfile profile = profileRepository.findByUserId(candidateId)
                    .orElseThrow(() -> new RuntimeException("Ứng viên chưa cập nhật hồ sơ, không thể gợi ý lộ trình."));

            JobPosting job = app.getJobPosting();

            // Sử dụng hàm helper đã viết trước đó để tạo JSON sạch
            String candidateData = buildCandidateDataForAI(profile);
            String jobData = job.getDescription() + "\n" + job.getRequirements();

            // 4. Gọi AI
            CareerAdviceResult result = geminiService.suggestCareerPath(candidateData, jobData);

            // 5. Lưu vào DB (Convert Object -> JSON String)
            String jsonResult = objectMapper.writeValueAsString(result);
            app.setLearningPath(jsonResult); // Lưu vào cột TEXT trong DB
            applicationRepository.save(app);

            return result;

        } catch (Exception e) {
            log.error("Lỗi getCareerAdvice ID {}: ", applicationId, e);
            throw new RuntimeException("Không thể tạo lộ trình lúc này: " + e.getMessage());
        }
    }
}