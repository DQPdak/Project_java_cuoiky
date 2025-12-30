package app.ai.Service.cv.skill.Model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * CHỨC NĂNG:
 * - Đại diện cho bảng 'skill' trong cơ sở dữ liệu
 * - Lưu trữ thông tin về từng kỹ năng cụ thể (ví dụ: Java, Python, Git, ...)
 * - Mỗi kỹ năng thuộc về một nhóm kỹ năng (quan hệ nhiều-1 với SkillCategory)
 */

@Entity
@Table(name = "skill")
@Data
public class Skill {

    // Khóa chính tự động tăng
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên kỹ năng, bắt buộc có
    @Column(nullable = false)
    private String name;

    // Nhóm kỹ năng mà kỹ năng này thuộc về
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SkillCategory category;
}
