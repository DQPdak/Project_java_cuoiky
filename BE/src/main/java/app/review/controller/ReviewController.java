package app.review.controller;

import app.review.dto.ReviewRequest;
import app.review.service.ReviewService;
import app.util.SecurityUtils; // Sử dụng file utility có sẵn của bạn
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // API gửi đánh giá (Yêu cầu đăng nhập)
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        // Lấy email từ token, sau đó tìm ID user (Bạn có thể điều chỉnh SecurityUtils để lấy thẳng ID)
        // Ở đây giả định SecurityUtils.getCurrentUserLogin() trả về email
        String userEmail = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Unauthorized"));
        
        // Bạn cần method tìm User ID từ Email hoặc lấy ID từ Principal
        // Ví dụ: Long userId = userService.findByEmail(userEmail).getId();
        // Để demo nhanh tôi giả sử bạn đã inject UserService để lấy ID
        
        // *LƯU Ý*: Hãy đảm bảo bạn lấy đúng userId của người đang đăng nhập
        // Code ví dụ:
        // Long userId = authService.getUserIdByEmail(userEmail); 
        
        // return ResponseEntity.ok(reviewService.addReview(userId, request));
        return ResponseEntity.ok("Vui lòng tích hợp logic lấy UserID"); 
    }

    // API lấy danh sách đánh giá (Public)
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getCompanyReviews(@PathVariable Long companyId) {
        return ResponseEntity.ok(reviewService.getReviewsByCompany(companyId));
    }

    // API lấy điểm trung bình (Public)
    @GetMapping("/company/{companyId}/average")
    public ResponseEntity<?> getAverageRating(@PathVariable Long companyId) {
        return ResponseEntity.ok(reviewService.getAverageRating(companyId));
    }
}