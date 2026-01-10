package app.recruitment.service;

import app.recruitment.dto.request.JobApplicationRequest;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.enums.ApplicationStatus;

import java.util.List;
import java.util.Optional;

public interface JobApplicationService {
    JobApplication apply(Long studentId, JobApplicationRequest request);
    JobApplication updateStatus(Long recruiterId, Long applicationId, ApplicationStatus newStatus, String recruiterNote);
    List<JobApplication> listByJob(Long jobId);
    List<JobApplication> listByStudent(Long studentId);
    Optional<JobApplication> getById(Long id);
}
