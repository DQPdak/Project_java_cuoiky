package app.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import app.auth.model.User;

@Entity
@Table(name = "applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Ứng viên (Candidate)
    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    private String cvUrl;
    
    private String status; // PENDING, ACCEPTED, REJECTED
    
    private Double aiMatchingScore; // Điểm AI chấm

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
}