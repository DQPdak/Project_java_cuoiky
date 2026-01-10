package app.ai.service.cv.skill.component.extractorskill.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import app.ai.service.cv.skill.component.extractorskill.model.SkillCategory;
import app.ai.service.cv.skill.component.recomment.learningpath.model.Role;

import java.util.List;
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

     /**
     * Sử dụng JOIN FETCH để giải quyết vấn đề N+1 query 
     * khi cần lấy tất cả Category kèm theo các Skill của chúng.
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.steps")
    List<Role> findAllWithSteps();
}
