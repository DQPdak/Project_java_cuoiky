package app.recruitment.service;

import app.recruitment.dto.request.JobApplicationRequest;
import app.recruitment.dto.response.JobApplicationResponse;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.enums.ApplicationStatus;

import java.util.List;
import java.util.Optional;

public interface JobApplicationService {
    // Đổi studentId -> candidateId
    JobApplication apply(Long candidateId, JobApplicationRequest request);
    
    JobApplication updateStatus(Long recruiterId, Long applicationId, ApplicationStatus newStatus, String recruiterNote);
    
    List<JobApplication> listByJob(Long jobId);
    
    // [QUAN TRỌNG] Đổi listByStudent -> listByCandidateId để khớp với Service Impl
    List<JobApplication> listByCandidateId(Long candidateId);
    
    // Đổi tham số studentId -> candidateId
    List<JobApplicationResponse> getApplicationsByCandidateId(Long candidateId);
    
    Optional<JobApplication> getById(Long id);

    void deleteApplication(Long candidateId, Long applicationId);
}
