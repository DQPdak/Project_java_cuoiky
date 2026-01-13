package app.recruitment.repository;

import app.recruitment.entity.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import app.recruitment.entity.JobPosting;
import app.recruitment.entity.enums.JobStatus;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
List<JobPosting> findByRecruiterId(Long recruiterId);
List<JobPosting> findByTitleContainingIgnoreCase(String keyword);
List<JobPosting> findByStatus(JobStatus status);
}