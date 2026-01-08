package app.ai.service.cv.skill.learningpath.model;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "learning_step")
@Data
public class LearningStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skillName;      // Tên kỹ năng (Java, Docker...)
    private String description;    // Mô tả mục tiêu học tập
    private int sequenceOrder;     // Thứ tự trong lộ trình (1, 2, 3...)
    private boolean isCore;        // Kỹ năng bắt buộc (true) hay mở rộng (false)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
}
