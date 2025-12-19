package app.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.content.model.Application;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // 1. Lấy tất cả đơn ứng tuyển cho một công việc cụ thể (Dành cho Recruiter xem list ứng viên)
    List<Application> findByJobId(Long jobId);

    // 2. Lấy lịch sử ứng tuyển của một Ứng viên (Dành cho Candidate xem mình đã nộp đâu rồi)
    List<Application> findByCandidateId(Long candidateId);

    // 3. Kiểm tra xem Ứng viên này đã nộp đơn vào Job này chưa
    // (Để hiển thị nút "Đã ứng tuyển" và chặn nộp 2 lần)
    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    // 4. Tìm đơn ứng tuyển cụ thể theo cặp Candidate và Job (nếu cần lấy chi tiết để sửa/xóa)
    Optional<Application> findByCandidateIdAndJobId(Long candidateId, Long jobId);
    
    // 5. Đếm số lượng đơn ứng tuyển cho một Job (để hiển thị thống kê nhanh)
    long countByJobId(Long jobId);
}