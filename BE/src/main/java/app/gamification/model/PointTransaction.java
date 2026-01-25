package app.gamification.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "point_transactions",
    uniqueConstraints = @UniqueConstraint(
        name = "ux_point_tx_unique",
        columnNames = {"user_id", "action", "ref_type", "ref_id"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "ref_type", length = 50)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
