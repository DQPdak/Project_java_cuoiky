package app.ai.repository;

import app.ai.models.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICandidateRepository extends JpaRepository<Candidate, Long> {
    // Hàm này để kiểm tra xem email đã tồn tại chưa (tránh lưu trùng)
    boolean existsByEmail(String email);
}