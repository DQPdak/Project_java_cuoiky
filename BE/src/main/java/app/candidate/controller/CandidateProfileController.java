package app.candidate.controller;

import app.auth.dto.response.MessageResponse;
import app.candidate.dto.request.CandidateProfileUpdateRequest;
import app.candidate.model.CandidateProfile;
import app.candidate.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import app.auth.model.User;
import app.auth.repository.UserRepository;

@RestController
@RequestMapping("/api/candidate/profile")
@RequiredArgsConstructor
public class CandidateProfileController {

    private final CandidateService candidateService;
    private final UserRepository userRepository; // Dùng tạm để lấy ID từ email

    @PostMapping("/upload-cv")
    public ResponseEntity<?> uploadCV(@RequestParam("file") MultipartFile file) {
        try {
            // Lấy User hiện tại từ Security Context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Xử lý upload và phân tích
            CandidateProfile profile = candidateService.uploadAndAnalyzeCV(user.getId(), file);

            return ResponseEntity.ok(MessageResponse.success("Phân tích CV thành công", profile));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(MessageResponse.error("Lỗi xử lý CV: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            CandidateProfile profile = candidateService.getProfile(user.getId());
            return ResponseEntity.ok(MessageResponse.success("Lấy thông tin thành công", profile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error("Chưa có hồ sơ"));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody CandidateProfileUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            CandidateProfile updatedProfile = candidateService.updateProfile(user.getId(), request);
            return ResponseEntity.ok(MessageResponse.success("Cập nhật hồ sơ thành công", updatedProfile));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error("Lỗi cập nhật: " + e.getMessage()));
        }
    }
}