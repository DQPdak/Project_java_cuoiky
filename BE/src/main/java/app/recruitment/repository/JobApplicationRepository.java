package app.recruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.recruitment.entity.JobApplication;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByStudentId(Long studentId);
    Optional<JobApplication> findByJobIdAndStudentId(Long jobId, Long studentId);
}