package app.recruitment.service;

import app.recruitment.dto.request.JobPostingRequest;
import app.recruitment.entity.JobPosting;

import java.util.List;
import java.util.Optional;

public interface JobPostingService {
    JobPosting create(Long recruiterId, JobPostingRequest request);
    JobPosting update(Long recruiterId, Long jobId, JobPostingRequest request);
    void delete(Long recruiterId, Long jobId);
    Optional<JobPosting> getById(Long id);
    List<JobPosting> listByRecruiter(Long recruiterId);
    List<JobPosting> searchByTitle(String keyword);
}