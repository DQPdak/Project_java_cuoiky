package app.candidate.service;

import app.ai.models.Experience;
import app.ai.service.cv.CVAnalysisService;
import app.ai.service.cv.gemini.dto.ExperienceDTO;
import app.ai.service.cv.gemini.dto.GeminiResponse;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.dto.request.CandidateProfileUpdateRequest;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.recruitment.repository.CVAnalysisResultRepository; // [IMPORT MỚI]
import app.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final CVAnalysisService cvAnalysisService;
    private final CloudinaryService cloudinaryService; 
    
    // [INJECT MỚI] Repository quản lý kết quả phân tích AI
    private final CVAnalysisResultRepository cvAnalysisResultRepository; 

    @Transactional
    public CandidateProfile uploadAndAnalyzeCV(Long userId, MultipartFile file) throws Exception {
        // 1. Tìm User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 2. Upload file lên Cloudinary trước để lấy link online
        String cvOnlineUrl = cloudinaryService.uploadFile(file);

        // 3. Gọi AI phân tích nội dung file
        GeminiResponse aiResult = cvAnalysisService.analyzeCV(file);

        // 4. Lấy profile cũ để update, hoặc tạo mới nếu chưa có
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElse(CandidateProfile.builder()
                        .user(user)
                        .skills(new ArrayList<>())     
                        .experiences(new ArrayList<>()) 
                        .build());

        // 5. Cập nhật dữ liệu từ kết quả AI vào Entity
        updateProfileFromAI(profile, aiResult);

        // 6. Lưu đường dẫn file online vào profile
        profile.setCvFilePath(cvOnlineUrl);

        // [LOGIC MỚI] Xóa cache cũ vì người dùng vừa nộp CV mới
        // Lần sau họ xem Job, AI sẽ phải chấm lại theo CV mới này
        cvAnalysisResultRepository.deleteByUserId(userId);
        log.info("Đã xóa cache phân tích cũ của user {} do upload CV mới", userId);

        // 7. Lưu tất cả xuống Database
        return candidateProfileRepository.save(profile);
    }

    private void updateProfileFromAI(CandidateProfile profile, GeminiResponse result) {
        try {
            // --- A. Cập nhật Contact (Tên, Email, SĐT...) ---
            if (result.getContact() != null) {
                if (result.getContact().getName() != null && !result.getContact().getName().isEmpty()) {
                    profile.setFullName(result.getContact().getName());
                }
                if (result.getContact().getEmail() != null && !result.getContact().getEmail().isEmpty()) {
                    profile.setEmail(result.getContact().getEmail());
                }
                if (result.getContact().getPhoneNumber() != null && !result.getContact().getPhoneNumber().isEmpty()) {
                    profile.setPhoneNumber(result.getContact().getPhoneNumber());
                }
                if (result.getContact().getAddress() != null) {
                    profile.setAddress(result.getContact().getAddress());
                }
                if (result.getContact().getLinkedIn() != null) {
                    profile.setLinkedInUrl(result.getContact().getLinkedIn());
                }
            }

            // --- B. Cập nhật Skills ---
            if (result.getSkills() != null && !result.getSkills().isEmpty()) {
                profile.setSkills(new ArrayList<>(result.getSkills()));
            }

            // --- C. Cập nhật Experience (Quan trọng) ---
            if (result.getExperiences() != null) {
                if (profile.getExperiences() != null) {
                    profile.getExperiences().clear();
                } else {
                    profile.setExperiences(new ArrayList<>());
                }

                for (ExperienceDTO dto : result.getExperiences()) {
                    Experience entity = new Experience();
                    entity.setCompany(dto.getCompany());
                    entity.setRole(dto.getRole());
                    entity.setStartDate(dto.getStartDate());
                    entity.setEndDate(dto.getEndDate());
                    entity.setDescription(dto.getDescription());
                    entity.setCandidateProfile(profile); 
                    profile.getExperiences().add(entity);
                }
            }

            // --- D. About Me ---
            if (profile.getAboutMe() == null || profile.getAboutMe().isEmpty()) {
                String name = profile.getFullName() != null ? profile.getFullName() : "Ứng viên";
                profile.setAboutMe("Hồ sơ của " + name + " được trích xuất tự động bởi CareerMate AI.");
            }

        } catch (Exception e) {
            log.error("Lỗi khi map dữ liệu AI sang Profile: ", e);
        }
    }

    @Transactional
    public CandidateProfile updateProfile(Long userId, CandidateProfileUpdateRequest request) {
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // Cập nhật thông tin cơ bản
        if (request.getAboutMe() != null) profile.setAboutMe(request.getAboutMe());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) profile.setAddress(request.getAddress()); 
        if (request.getLinkedInUrl() != null) profile.setLinkedInUrl(request.getLinkedInUrl());

        // Cập nhật JSON fields
        if (request.getSkills() != null) profile.setSkills(request.getSkills());
        if (request.getExperiences() != null) {
            if (profile.getExperiences() != null) {
                profile.getExperiences().clear();
            } else {
                profile.setExperiences(new ArrayList<>());
            }

            List<Map<String, Object>> rawExps = request.getExperiences();
            
            for (Map<String, Object> expMap : rawExps) {
                Experience exp = new Experience();
                exp.setCompany((String) expMap.getOrDefault("companyName", ""));
                exp.setRole((String) expMap.getOrDefault("role", ""));
                exp.setDescription((String) expMap.getOrDefault("description", ""));
                exp.setStartDate((String) expMap.getOrDefault("startDate", ""));
                exp.setEndDate((String) expMap.getOrDefault("endDate", ""));
                
                exp.setCandidateProfile(profile);
                profile.getExperiences().add(exp);
            }
        }

        // [LOGIC MỚI] Hồ sơ thay đổi -> Điểm cũ sai -> Xóa cache
        cvAnalysisResultRepository.deleteByUserId(userId);
        log.info("Đã xóa cache phân tích cũ của user {} do update profile", userId);

        return candidateProfileRepository.save(profile);
    }

    public CandidateProfile getProfile(Long userId) {
        return candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
    }
}