package app.candidate.controller;

import app.auth.dto.response.MessageResponse;
import app.auth.model.User;
import app.auth.repository.UserRepository;
import app.candidate.service.JobRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidate/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final JobRecommendationService recommendationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getRecommendations() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var jobs = recommendationService.getRecommendedJobs(user.getId());
            return ResponseEntity.ok(MessageResponse.success("Gợi ý việc làm thành công", jobs));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(MessageResponse.error(e.getMessage()));
        }
    }
}