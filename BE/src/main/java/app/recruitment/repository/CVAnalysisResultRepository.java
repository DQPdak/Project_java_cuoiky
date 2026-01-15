package app.recruitment.repository;

import app.recruitment.entity.CVAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CVAnalysisResultRepository extends JpaRepository<CVAnalysisResult, Long> {
    
    // Tìm kết quả đã lưu
    Optional<CVAnalysisResult> findByUserIdAndJobPostingId(Long userId, Long jobPostingId);
}