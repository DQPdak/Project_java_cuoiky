package app.ai.service.cv.skill.skillrelation.model;

import app.ai.service.cv.skill.extractorSkill.model.Skill;
import jakarta.persistence.*;
import lombok.Data;
/**
 * CHỨC NĂNG:
 * - Đại diện cho bảng 'skill_relationship' trong cơ sở dữ liệu
 * - Lưu trữ mối quan hệ giữa các kỹ năng (ví dụ: kỹ năng A liên quan đến kỹ năng B)
 * - Mỗi mối quan hệ liên kết hai kỹ năng với nhau (quan hệ nhiều-1 với Skill cho cả hai trường skill và relatedSkill)
*/
@Entity
@Table(name = "skill_relationship")
@Data
public class SkillRelationship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ManyToOne
    @JoinColumn(name = "related_skill_id", nullable = false)
    private Skill relatedSkill;
}