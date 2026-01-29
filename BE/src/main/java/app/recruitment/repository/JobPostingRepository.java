package app.recruitment.repository;

import app.recruitment.entity.JobPosting;
import app.recruitment.entity.enums.JobStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByRecruiterId(Long recruiterId);
    List<JobPosting> findByTitleContainingIgnoreCase(String keyword);

    List<JobPosting> findByStatus(JobStatus status);

    // ✅ thêm EntityGraph ở đây
    @EntityGraph(attributePaths = {"company"})
    Page<JobPosting> findByStatus(JobStatus status, Pageable pageable);

    // ✅ thêm EntityGraph cho findAll paging (admin ALL đang gọi findAll(pageable))
    @Override
    @EntityGraph(attributePaths = {"company"})
    Page<JobPosting> findAll(Pageable pageable);

    List<JobPosting> findTop10ByStatusOrderByCreatedAtDesc(JobStatus status);

    @Query("SELECT j FROM JobPosting j WHERE " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND j.status = 'PUBLISHED'")
    List<JobPosting> searchJobs(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT j FROM JobPosting j LEFT JOIN FETCH j.extractedSkills WHERE j.id IN :ids")
    List<JobPosting> findAllByIdsWhithSkills(@Param("ids") List<Long> ids);
}
