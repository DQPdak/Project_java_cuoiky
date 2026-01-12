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

    // Query tìm kiếm nâng cao cho Candidate (Title hoặc Company Name)
    @Query("SELECT j FROM JobPosting j " +
            "LEFT JOIN j.company c " +
            "WHERE (LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR (c.name IS NOT NULL AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
            "AND j.status = 'OPEN'")
    List<JobPosting> searchJobs(@Param("keyword") String keyword);

    // Lấy 10 việc làm mới nhất
    List<JobPosting> findTop10ByStatusOrderByCreatedAtDesc(JobStatus status);
}