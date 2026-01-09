package app.ai.service.cv.skill.extractorskill.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.ai.service.cv.skill.extractorskill.model.SkillCategory;

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
