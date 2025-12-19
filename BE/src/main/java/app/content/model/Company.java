package app.content.model;

import jakarta.persistence.*;
import lombok.*;
import app.auth.entity.User;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<Job> jobs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}