package app.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import app.auth.model.User;
import app.recruitment.entity.JobPosting;

@Entity
@Table(name = "companies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String logoUrl;
    private String website;

    // Liên kết với User (Recruiter quản lý công ty này)
    @OneToOne
    @JoinColumn(name = "recruiter_id")
    private User recruiter;
    
    // Một công ty có nhiều Job
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<JobPosting> jobs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}