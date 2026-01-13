package app.recruitment.entity;

import app.auth.model.User;
import app.recruitment.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications") // Tên bảng chuẩn
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- QUAN HỆ ---
    
    // Nộp vào Job nào? (Dùng Entity JobPosting đã gộp ở bước trước)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    // Ai là người nộp? (User có role CANDIDATE)
    // Đổi tên biến student -> candidate cho chuyên nghiệp (vì người đi làm cũng nộp được)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    // --- THÔNG TIN HỒ SƠ ---

    @Column(length = 1000)
    private String cvUrl; // Link file CV (S3, Cloudinary hoặc Local)
    
    // --- KẾT QUẢ TỪ AI (QUAN TRỌNG ĐỂ LỌC > 70%) ---
    
    // Điểm số AI chấm (0 - 100)
    @Column(name = "match_score")
    private Integer matchScore; 

    // Lý do tại sao điểm thấp/cao (AI giải thích)
    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    // Các kỹ năng còn thiếu (Lưu dạng JSON String hoặc text liệt kê)
    @Column(columnDefinition = "TEXT")
    private String missingSkills;

    // --- QUẢN LÝ TUYỂN DỤNG ---

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(columnDefinition = "TEXT")
    private String recruiterNote; // Ghi chú thủ công của HR (Vd: "Ứng viên này giao tiếp tốt")

    // --- AUDIT ---
    
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        if (status == null) status = ApplicationStatus.APPLIED;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}