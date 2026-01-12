package app.recruitment.entity;

import jakarta.persistence.*;
import lombok.*;
import app.auth.model.User;
import app.content.model.Company;
import app.recruitment.entity.enums.JobStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String requirements;
    
    private String salaryRange;
    private String location;
    
    private LocalDateTime expiryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.DRAFT;
    
    // Recruiter (User with role = RECRUITER)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    @ManyToOne(fetch = FetchType.EAGER) // Để EAGER nếu muốn lấy luôn thông tin công ty khi query Job
    @JoinColumn(name = "company_id")
    private Company company;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}