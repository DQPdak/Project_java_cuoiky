package app.recruitment.repository;

import app.recruitment.entity.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import app.recruitment.entity.JobPosting;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
List<JobPosting> findByRecruiterId(Long recruiterId);
List<JobPosting> findByTitleContainingIgnoreCase(String keyword);
List<JobPosting> findByStatus(JobStatus status);

List<JobPosting> findTop10ByStatusOrderByCreatedAtDesc(JobStatus status);

    @Query("SELECT j FROM JobPosting j WHERE " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND j.status = 'OPEN'") // Chỉ tìm job đang mở
    List<JobPosting> searchJobs(@Param("keyword") String keyword);
}