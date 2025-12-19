package app.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.content.model.Job;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    // Tìm job theo công ty
    List<Job> findByCompanyId(Long companyId);
    
    // Tìm job theo title (có chứa từ khóa)
    List<Job> findByTitleContainingIgnoreCase(String keyword);
}