package app.recruitment.entity;

import app.auth.model.User;
import app.recruitment.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- QUAN HỆ ---

    // Job Posting (Lưu ý: name="job_id" để khớp với các câu SQL test trước đó)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false) 
    private JobPosting jobPosting;

    // Ứng viên (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    // --- THÔNG TIN HỒ SƠ ---
    @Column(length = 1000)
    private String cvUrl; // Link CV tại thời điểm nộp

    @Column(columnDefinition = "TEXT")
    private String coverLetter; // Thư giới thiệu (nếu có)

    // --- KẾT QUẢ AI MATCHING (QUAN TRỌNG) ---
    // Các trường này ban đầu sẽ là NULL hoặc 0.
    // Sau khi Recruiter bấm "Sàng lọc", dữ liệu sẽ được điền vào đây.

    @Column(name = "match_score")
    private Integer matchScore; // Điểm % (0-100)

    @Column(columnDefinition = "TEXT")
    private String aiEvaluation; // Nhận xét của AI

    // Thống kê chi tiết để Recruiter liếc mắt là thấy ngay
    private Integer matchedSkillsCount;  // Số skill trùng
    private Integer missingSkillsCount;  // Số skill thiếu
    private Integer extraSkillsCount;    // Số skill thừa
    private Integer totalRequiredSkills; // Tổng yêu cầu của Job

    // Lưu danh sách skill thiếu dạng text (JSON hoặc CSV) để hiển thị frontend
    // Ví dụ: "Docker, Kubernetes, AWS"
    @Column(columnDefinition = "TEXT")
    private String missingSkillsList; 

    // --- QUẢN LÝ TRẠNG THÁI ---

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING; // Mặc định là PENDING (Chờ duyệt)

    @Column(columnDefinition = "TEXT")
    private String recruiterNote; // Ghi chú của người tuyển dụng

    // --- AUDIT ---
    
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        if (status == null) status = ApplicationStatus.PENDING;
        // Mặc định điểm số là 0 khi mới nộp
        if (matchScore == null) matchScore = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}