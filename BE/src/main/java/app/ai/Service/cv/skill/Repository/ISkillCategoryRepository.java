package app.ai.Service.cv.skill.Repository;

import app.ai.Service.cv.skill.Model.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * CHỨC NĂNG:
 * - Giao tiếp với bảng 'skill_category' trong database
 * - Cung cấp phương thức truy vấn nhóm kỹ năng theo tên
 * - Spring Boot sẽ tự động sinh class thực thi từ interface này
 */

public interface ISkillCategoryRepository extends JpaRepository<SkillCategory, Long> {

    // Tìm nhóm kỹ năng theo tên (không phân biệt hoa thường)
    Optional<SkillCategory> findByNameIgnoreCase(String name);
}
