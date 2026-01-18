package app.ai.models;

import app.auth.model.User;
import app.recruitment.entity.JobPosting;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "interview_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({
        "password", 
        "roles", 
        "interviewSessions", 
        "jobApplications", 
        "hibernateLazyInitializer", 
        "handler",
        "candidateProfile" 
    })
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id")
    @JsonIgnoreProperties({
        "extractedSkills", 
        "applications", 
        "recruiter",           
        "company",              
        "hibernateLazyInitializer", 
        "handler"
    })
    private JobPosting jobPosting;

    // Trạng thái: "ONGOING" (Đang phỏng vấn), "COMPLETED" (Đã xong)
    private String status; 

    // --- KẾT QUẢ SAU KHI XONG ---
    private Integer finalScore; // Điểm số 0-100
    
    @Column(columnDefinition = "TEXT")
    private String feedback; // Nhận xét tổng quan của AI

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Quan hệ 1-N với tin nhắn (Xóa session là xóa luôn tin nhắn)
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewMessage> messages;
}