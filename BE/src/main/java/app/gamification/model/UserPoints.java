package app.gamification.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_points")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    private long totalPoints;
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

