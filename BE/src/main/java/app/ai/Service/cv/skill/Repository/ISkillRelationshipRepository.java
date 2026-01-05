package app.ai.Service.cv.skill.Repository;
import app.ai.Service.cv.skill.Model.Skill;
import app.ai.Service.cv.skill.Model.SkillRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * CHỨC NĂNG:
 * - Giao tiếp với bảng 'skill_relationship' trong database
 * - Cung cấp các phương thức truy vấn mối quan hệ giữa các kỹ năng
 * - Spring Boot sẽ tự động sinh class thực thi từ interface này
 */
public interface ISkillRelationshipRepository extends JpaRepository<SkillRelationship, Long> {
    List<SkillRelationship> findBySkill(Skill skill);
}
