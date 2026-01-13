package app.recruitment.repository;

import app.recruitment.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByJobPostingId(Long jobPostingId);

    List<JobApplication> findByCandidateId(Long candidateId);

    Optional<JobApplication> findByCandidateIdAndJobPostingId(Long candidateId, Long jobPostingId);

    boolean existsByCandidateIdAndJobPostingId(Long candidateId, Long jobPostingId);

    long countByJobPostingId(Long jobPostingId);
    List<JobApplication> findByJobPostingIdAndMatchScoreGreaterThanEqualOrderByMatchScoreDesc(Long jobPostingId, Integer minScore);
}