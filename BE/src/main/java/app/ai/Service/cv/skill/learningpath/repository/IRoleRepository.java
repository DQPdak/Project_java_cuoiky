package app.ai.service.cv.skill.learningpath.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

import app.ai.service.cv.skill.learningpath.model.Role;

// Quản lý truy vấn của Role
public interface IRoleRepository extends JpaRepository<Role, Long> {
    // Tìm kiếm Role theo tên (không phân biệt hoa thường)
    Optional<Role> findByNameIgnoreCase(String name);
    
    /**
     * Sử dụng JOIN FETCH để giải quyết vấn đề N+1 query 
     * khi cần lấy tất cả Role kèm theo các Step của chúng.
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.steps")
    List<Role> findAllWithSteps();
}
