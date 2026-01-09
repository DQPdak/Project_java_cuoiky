package app.ai.service.cv.skill.component.extractorskill.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.ai.service.cv.skill.component.extractorskill.model.Skill;

import java.util.Optional;
import java.util.List;
/**
 * CHỨC NĂNG:
 * - Giao tiếp với bảng 'skill' trong database
 * - Cung cấp các phương thức truy vấn kỹ năng theo tên hoặc theo nhóm
 * - Spring Boot sẽ tự động sinh class thực thi từ interface này
 */

public interface ISkillRepository extends JpaRepository<Skill, Long> {

    // Tìm kỹ năng theo tên (không phân biệt hoa thường)
    Optional<Skill> findByNameIgnoreCase(String name);

    // Tìm danh sách kỹ năng theo tên nhóm kỹ năng
    
    List<Skill> findByCategoryNameIgnoreCase(String categoryName);
}


