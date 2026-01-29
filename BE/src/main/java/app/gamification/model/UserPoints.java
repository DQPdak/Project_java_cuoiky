package app.gamification.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_points")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPoints {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (totalPoints == null) totalPoints = 0;
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
