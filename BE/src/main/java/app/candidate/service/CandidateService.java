package app.candidate.service;

import app.ai.service.cv.CVAnalysisService;
import app.ai.service.cv.gemini.dto.ExperienceDTO;
import app.ai.service.cv.gemini.dto.GeminiResponse;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.candidate.dto.request.CandidateProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final CVAnalysisService cvAnalysisService;
    // Đã xóa ObjectMapper vì Hibernate 6 tự động xử lý JSON

    @Transactional
    public CandidateProfile uploadAndAnalyzeCV(Long userId, MultipartFile file) throws Exception {
        // 1. Tìm User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Gọi AI phân tích
        GeminiResponse aiResult = cvAnalysisService.analyzeCV(file);

        // 3. Lấy hoặc tạo Profile mới
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElse(CandidateProfile.builder().user(user).build());

        // 4. Cập nhật dữ liệu
        updateProfileFromAI(profile, aiResult);

        return candidateProfileRepository.save(profile);
    }

    private void updateProfileFromAI(CandidateProfile profile, GeminiResponse result) {
        try {
            // --- A. Cập nhật Contact ---
            if (result.getContact() != null) {
                profile.setPhoneNumber(result.getContact().getPhoneNumber());
                // Nếu Entity có email thì set: profile.setEmail(...)
            }

            // --- B. Cập nhật Skills ---
            // Vì Entity khai báo List<String> + @JdbcTypeCode(SqlTypes.JSON)
            // Nên ta gán trực tiếp List vào, không cần convert sang String
            if (result.getSkills() != null) {
                profile.setSkills(result.getSkills());
            }

            // --- C. Cập nhật Experience ---
            // Entity khai báo List<Map<String, Object>>, ta cần tạo List Map này
            if (result.getExperiences() != null) {
                List<Map<String, Object>> expList = new ArrayList<>();

                for (ExperienceDTO exp : result.getExperiences()) {
                    Map<String, Object> jobMap = new HashMap<>();
                    jobMap.put("company", exp.getCompany());
                    jobMap.put("role", exp.getRole());
                    jobMap.put("startDate", exp.getStartDate());
                    jobMap.put("endDate", exp.getEndDate());
                    
                    // Tạo text hiển thị thời gian
                    String duration = (exp.getStartDate() != null ? exp.getStartDate() : "?") 
                                    + " - " 
                                    + (exp.getEndDate() != null ? exp.getEndDate() : "Present");
                    jobMap.put("duration", duration);

                    expList.add(jobMap);
                }

                // Gán trực tiếp List<Map> vào profile
                profile.setExperiences(expList);
            }

            // --- D. About Me ---
            if (profile.getAboutMe() == null || profile.getAboutMe().isEmpty()) {
                profile.setAboutMe("Thông tin được trích xuất tự động từ CV.");
            }
            
            // Lưu đường dẫn file (nếu có logic upload file vật lý, tạm thời để trống hoặc tên file)
            // profile.setCvFilePath("..."); 

        } catch (Exception e) {
            log.error("Lỗi khi map dữ liệu AI sang Profile: ", e);
            // Không ném lỗi để đảm bảo dữ liệu vẫn được lưu dù map thiếu một vài trường
        }
    }

    @Transactional
    public CandidateProfile updateProfile(Long userId, CandidateProfileUpdateRequest request) {
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // Cập nhật thông tin cơ bản
        if (request.getAboutMe() != null) profile.setAboutMe(request.getAboutMe());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) profile.setAddress(request.getAddress()); // Cập nhật địa chỉ

        // --- CẬP NHẬT 2 LINK ---
        if (request.getLinkedInUrl() != null) profile.setLinkedInUrl(request.getLinkedInUrl());
        if (request.getWebsiteUrl() != null) profile.setWebsiteUrl(request.getWebsiteUrl());

        // Cập nhật JSON fields
        if (request.getSkills() != null) profile.setSkills(request.getSkills());
        if (request.getExperiences() != null) profile.setExperiences(request.getExperiences());
        // if (request.getEducations() != null) profile.setEducations(request.getEducations());

        return candidateProfileRepository.save(profile);
    }

    public CandidateProfile getProfile(Long userId) {
        return candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }
}