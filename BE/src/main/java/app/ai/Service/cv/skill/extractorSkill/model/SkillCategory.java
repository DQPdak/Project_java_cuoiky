package app.ai.service.cv.skill.extractorskill.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
/**
 * CHỨC NĂNG:
 * - Đại diện cho bảng 'skill_category' trong cơ sở dữ liệu
 * - Lưu trữ thông tin về nhóm kỹ năng (ví dụ: PROGRAMMING, DATABASE, ...)
 * - Mỗi nhóm kỹ năng có thể chứa nhiều kỹ năng cụ thể (quan hệ 1-nhiều với Skill)
 */

@Entity
@Table(name = "skill_category")
@Data
public class SkillCategory {

    // Khóa chính tự động tăng
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên nhóm kỹ năng, không trùng lặp
    @Column(unique = true, nullable = false)
    private String name;

    // Danh sách các kỹ năng thuộc nhóm này
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Skill> skills = new HashSet<>();
}
