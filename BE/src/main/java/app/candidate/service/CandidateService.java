package app.candidate.service;

import app.ai.service.cv.CVAnalysisService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;
    private final CVAnalysisService cvAnalysisService;

    @Transactional
    public CandidateProfile uploadAndAnalyzeCV(Long userId, MultipartFile file) throws Exception {
        // 1. Tìm User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 2. Gọi AI phân tích
        GeminiResponse aiResult = cvAnalysisService.analyzeCV(file);

        // 3. Lấy hoặc tạo Profile mới
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElse(CandidateProfile.builder()
                        .user(user)
                        .skills(new ArrayList<>()) // Init list tránh null
                        .experiences(new ArrayList<>()) // Init list tránh null
                        .build());

        // 4. Cập nhật dữ liệu từ AI vào Entity
        updateProfileFromAI(profile, aiResult);

        // 5. Lưu xuống DB
        return candidateProfileRepository.save(profile);
    }

    private void updateProfileFromAI(CandidateProfile profile, GeminiResponse result) {
        try {
            // --- A. Cập nhật Contact (ĐÃ SỬA) ---
            if (result.getContact() != null) {
                // 1. Cập nhật Tên (Ưu tiên lấy từ CV)
                if (result.getContact().getName() != null && !result.getContact().getName().isEmpty()) {
                    profile.setFullName(result.getContact().getName());
                }
                
                // 2. Cập nhật Email (Lấy từ CV)
                if (result.getContact().getEmail() != null && !result.getContact().getEmail().isEmpty()) {
                    profile.setEmail(result.getContact().getEmail());
                }

                // 3. Cập nhật SĐT
                if (result.getContact().getPhoneNumber() != null && !result.getContact().getPhoneNumber().isEmpty()) {
                    profile.setPhoneNumber(result.getContact().getPhoneNumber());
                }
                
                // 4. Cập nhật Address/LinkedIn (nếu có)
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

            // --- C. Cập nhật Experience ---
            // ... (Giữ nguyên logic Experience của bạn, nó đã đúng) ...

            // --- D. About Me ---
            if (profile.getAboutMe() == null || profile.getAboutMe().isEmpty()) {
                // Nếu AI lấy được tên thì dùng tên, không thì dùng mặc định
                String name = profile.getFullName() != null ? profile.getFullName() : "Ứng viên";
                profile.setAboutMe("Hồ sơ của " + name + " được tạo tự động bởi CareerMate AI.");
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
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
    }
}