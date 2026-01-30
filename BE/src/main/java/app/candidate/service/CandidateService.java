package app.candidate.service;

import app.ai.models.Experience;
import app.ai.service.cv.CVAnalysisService;
import app.ai.service.cv.gemini.dto.ExperienceDTO;
import app.ai.service.cv.gemini.dto.GeminiResponse;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.dto.request.CandidateProfileUpdateRequest;
import app.candidate.dto.response.CandidateProfileResponse;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.recruitment.repository.CVAnalysisResultRepository;
import app.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final CVAnalysisService cvAnalysisService;
    private final CloudinaryService cloudinaryService;
    private final CVAnalysisResultRepository cvAnalysisResultRepository;
    private final ObjectMapper objectMapper;

    /**
     * Dùng @Transactional(readOnly = true) để Hibernate mở cửa kho lấy Skills (Lazy)
     */
    @Transactional(readOnly = true)
    public CandidateProfileResponse getProfileDTO(Long userId) {
        // 1. Lấy Entity (Đang chứa Skills Lazy)
        CandidateProfile p = getProfile(userId);

        // 2. Map Experience Entity -> DTO
        List<ExperienceDTO> expDTOs = new ArrayList<>();
        if (p.getExperiences() != null) {
            expDTOs = p.getExperiences().stream()
                    .map(e -> ExperienceDTO.builder()
                            .company(e.getCompany())
                            .role(e.getRole())
                            .startDate(e.getStartDate())
                            .endDate(e.getEndDate())
                            .description(e.getDescription())
                            .build())
                    .collect(Collectors.toList());
        }

        // 3. Đóng gói vào Response DTO
        // Tại dòng p.getSkills(), vì đang trong Transaction nên Hibernate sẽ lấy dữ liệu thật
        return CandidateProfileResponse.builder()
                .id(p.getId())
                .fullName(p.getFullName())
                .email(p.getEmail())
                .phoneNumber(p.getPhoneNumber())
                .address(p.getAddress())
                .aboutMe(p.getAboutMe())
                .linkedInUrl(p.getLinkedInUrl())
                .websiteUrl(p.getWebsiteUrl())
                .avatarUrl(p.getAvatarUrl())
                .cvFilePath(p.getCvFilePath())
                .skills(p.getSkills() != null ? new ArrayList<>(p.getSkills()) : new ArrayList<>()) 
                .experiences(expDTOs)
                .build();
    }

    /**
     * Upload CV, Phân tích AI và Lưu vào DB
     */
    @Transactional
    public CandidateProfile uploadAndAnalyzeCV(Long userId, MultipartFile file) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Upload file lấy link
        String cvOnlineUrl = cloudinaryService.uploadFile(file);

        // AI phân tích
        GeminiResponse aiResult = cvAnalysisService.analyzeCV(file);

        // Lấy profile cũ hoặc tạo mới
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElse(CandidateProfile.builder()
                        .user(user)
                        .skills(new ArrayList<>())
                        .experiences(new ArrayList<>())
                        .build());

        // Update dữ liệu từ AI
        updateProfileFromAI(profile, aiResult);
        profile.setCvFilePath(cvOnlineUrl);

        // Xóa cache kết quả chấm điểm cũ (vì CV đã thay đổi)
        cvAnalysisResultRepository.deleteByUserId(userId);
        log.info("Đã xóa cache phân tích cũ của user {} do upload CV mới", userId);

        return candidateProfileRepository.save(profile);
    }

    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        CandidateProfile profile = getProfile(userId);

        // Upload lên Cloudinary (Dùng lại service đã có)
        String avatarUrl = cloudinaryService.uploadFile(file);

        // Lưu link vào DB
        profile.setAvatarUrl(avatarUrl);
        candidateProfileRepository.save(profile);

        return avatarUrl;
    }

    /**
     * Cập nhật Profile thủ công từ Form
     */
    @Transactional
    public CandidateProfile updateProfile(Long userId, CandidateProfileUpdateRequest request) {
        // Lấy User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // TÌM HOẶC TẠO MỚI
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElse(CandidateProfile.builder()
                        .user(user)
                        .skills(new ArrayList<>())
                        .experiences(new ArrayList<>())
                        .build());

        // Map các trường cơ bản
        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            profile.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            profile.setEmail(request.getEmail());
        }
        if (request.getAboutMe() != null) profile.setAboutMe(request.getAboutMe());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getLinkedInUrl() != null) profile.setLinkedInUrl(request.getLinkedInUrl());
        if (request.getWebsiteUrl() != null) profile.setWebsiteUrl(request.getWebsiteUrl());

        // Map Skills
        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills());
        }

        // Map Experience (Xóa cũ thêm mới)
        if (request.getExperiences() != null) {
            if (profile.getExperiences() != null) profile.getExperiences().clear();
            else profile.setExperiences(new ArrayList<>());

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

        // ap Education (Lưu dạng JSON String)
        if (request.getEducations() != null) {
            try {
                String educationJson = objectMapper.writeValueAsString(request.getEducations());
                profile.setEducationJson(educationJson);
            } catch (Exception e) {
                log.error("Lỗi parse Education sang JSON", e);
            }
        }

        // Xóa cache chấm điểm cũ để tính lại điểm matching
        cvAnalysisResultRepository.deleteByUserId(userId);

        return candidateProfileRepository.save(profile);
    }

    // --- CÁC HÀM HELPER (Private/Internal) ---

    public CandidateProfile getProfile(Long userId) {
        return candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Chưa có hồ sơ ứng viên cho user: " + userId));
    }
    
    // Hàm này có thể được JobMatchingService sử dụng để lấy skill nhanh (JOIN FETCH)
    public CandidateProfile getProfileForMatching(Long userId) {
        return candidateProfileRepository.findByUserIdWithSkills(userId)
                .orElse(null);
    }

    private void updateProfileFromAI(CandidateProfile profile, GeminiResponse result) {
        try {
            // 1. Map Contact (Thông tin liên hệ)
            if (result.getContact() != null) {
                if (result.getContact().getName() != null) profile.setFullName(result.getContact().getName());
                if (result.getContact().getEmail() != null) profile.setEmail(result.getContact().getEmail());
                if (result.getContact().getPhoneNumber() != null) profile.setPhoneNumber(result.getContact().getPhoneNumber());
                if (result.getContact().getAddress() != null) profile.setAddress(result.getContact().getAddress());
                if (result.getContact().getLinkedIn() != null) profile.setLinkedInUrl(result.getContact().getLinkedIn());
            }

            // 2. Map Skills (Kỹ năng)
            if (result.getSkills() != null && !result.getSkills().isEmpty()) {
                profile.setSkills(new ArrayList<>(result.getSkills()));
            }

            // 3. Map Experience (Kinh nghiệm làm việc)
            if (result.getExperiences() != null) {
                // Xóa danh sách cũ để cập nhật danh sách mới từ CV
                if (profile.getExperiences() != null) profile.getExperiences().clear();
                else profile.setExperiences(new ArrayList<>());

                for (ExperienceDTO dto : result.getExperiences()) {
                    Experience entity = new Experience();
                    entity.setCompany(dto.getCompany());
                    entity.setRole(dto.getRole());
                    entity.setStartDate(dto.getStartDate());
                    entity.setEndDate(dto.getEndDate());
                    entity.setDescription(dto.getDescription());
                    entity.setCandidateProfile(profile); // Set quan hệ 2 chiều
                    profile.getExperiences().add(entity);
                }
            }

            // 4. [MỚI] Map About Me (Giới thiệu bản thân)
            // Lấy trực tiếp từ kết quả AI nếu có
            if (result.getAboutMe() != null && !result.getAboutMe().isEmpty()) {
                profile.setAboutMe(result.getAboutMe());
            }

            // 5. Default About Me (Dự phòng)
            // Chỉ tự sinh câu giới thiệu nếu sau bước 4 mà vẫn chưa có About Me
            if (profile.getAboutMe() == null || profile.getAboutMe().isEmpty()) {
                String name = profile.getFullName() != null ? profile.getFullName() : "Ứng viên";
                profile.setAboutMe("Hồ sơ của " + name + " được trích xuất tự động bởi CareerMate AI.");
            }

        } catch (Exception e) {
            log.error("Lỗi khi map dữ liệu AI sang Profile: ", e);
        }
    }
}