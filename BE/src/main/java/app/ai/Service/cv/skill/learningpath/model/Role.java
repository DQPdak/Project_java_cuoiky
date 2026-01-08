package app.ai.service.cv.skill.learningpath.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

// Model đại diện cho vị trí là việc trong công ty

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    /**
     * Quan hệ 1-N với LearningStep. 
     * Một Role có nhiều bước học tập.
     * sequenceOrder dùng để sắp xếp thứ tự các bước khi load từ DB.
     */
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    private List<LearningStep> steps;
}
