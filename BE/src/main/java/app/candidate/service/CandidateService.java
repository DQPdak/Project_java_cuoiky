package app.candidate.service;

import app.ai.models.Experience;
import app.ai.service.cv.CVAnalysisService;
import app.ai.service.cv.gemini.dto.ExperienceDTO;
import app.ai.service.cv.gemini.dto.GeminiResponse;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.model.CandidateProfile;
import app.candidate.repository.CandidateProfileRepository;
import app.service.CloudinaryService; // Import Service Cloudinary vừa tạo
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final CVAnalysisService cvAnalysisService;
    private final CloudinaryService cloudinaryService; // Inject Service Cloudinary

    @Transactional
    public CandidateProfile uploadAndAnalyzeCV(Long userId, MultipartFile file) throws Exception {
        // 1. Tìm User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 2. Upload file lên Cloudinary trước để lấy link online
        // (Nếu lỗi upload thì sẽ dừng luôn, không tốn tiền gọi AI)
        String cvOnlineUrl = cloudinaryService.uploadFile(file);

        // 3. Gọi AI phân tích nội dung file
        GeminiResponse aiResult = cvAnalysisService.analyzeCV(file);

        // 4. Lấy profile cũ để update, hoặc tạo mới nếu chưa có
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElse(CandidateProfile.builder()
                        .user(user)
                        .skills(new ArrayList<>())     // Init list tránh null pointer
                        .experiences(new ArrayList<>()) // Init list tránh null pointer
                        .build());

        // 5. Cập nhật dữ liệu từ kết quả AI vào Entity
        updateProfileFromAI(profile, aiResult);

        // 6. Lưu đường dẫn file online vào profile
        profile.setCvFilePath(cvOnlineUrl);

        // 7. Lưu tất cả xuống Database
        return candidateProfileRepository.save(profile);
    }

    private void updateProfileFromAI(CandidateProfile profile, GeminiResponse result) {
        try {
            // --- A. Cập nhật Contact (Tên, Email, SĐT...) ---
            if (result.getContact() != null) {
                // Tên
                if (result.getContact().getName() != null && !result.getContact().getName().isEmpty()) {
                    profile.setFullName(result.getContact().getName());
                }
                // Email
                if (result.getContact().getEmail() != null && !result.getContact().getEmail().isEmpty()) {
                    profile.setEmail(result.getContact().getEmail());
                }
                // Số điện thoại
                if (result.getContact().getPhoneNumber() != null && !result.getContact().getPhoneNumber().isEmpty()) {
                    profile.setPhoneNumber(result.getContact().getPhoneNumber());
                }
                // Địa chỉ & LinkedIn
                if (result.getContact().getAddress() != null) {
                    profile.setAddress(result.getContact().getAddress());
                }
                if (result.getContact().getLinkedIn() != null) {
                    profile.setLinkedInUrl(result.getContact().getLinkedIn());
                }
            }

            // --- B. Cập nhật Skills ---
            if (result.getSkills() != null && !result.getSkills().isEmpty()) {
                // Thay thế toàn bộ skill cũ bằng skill mới từ CV
                profile.setSkills(new ArrayList<>(result.getSkills()));
            }

            // --- C. Cập nhật Experience (Quan trọng) ---
            if (result.getExperiences() != null) {
                // 1. Xóa danh sách kinh nghiệm cũ để tránh trùng lặp
                if (profile.getExperiences() != null) {
                    profile.getExperiences().clear();
                } else {
                    profile.setExperiences(new ArrayList<>());
                }

                // 2. Map từng item từ DTO (AI) sang Entity (DB)
                for (ExperienceDTO dto : result.getExperiences()) {
                    Experience entity = new Experience();
                    
                    entity.setCompany(dto.getCompany());
                    entity.setRole(dto.getRole());
                    entity.setStartDate(dto.getStartDate());
                    entity.setEndDate(dto.getEndDate());
                    entity.setDescription(dto.getDescription());
                    
                    // QUAN TRỌNG: Gắn quan hệ ngược (Foreign Key)
                    entity.setCandidateProfile(profile); 
                    
                    // Thêm vào list
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
            // Không throw exception ở đây để đảm bảo các dữ liệu khác (như file path) vẫn được lưu
        }
    }

    public CandidateProfile getProfile(Long userId) {
        return candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
    }
}