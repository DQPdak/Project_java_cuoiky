package app.recruitment.mapper;

import org.springframework.stereotype.Component;
import app.recruitment.entity.JobPosting;
import app.recruitment.entity.JobApplication;
import app.recruitment.dto.response.JobPostingResponse;
import app.recruitment.dto.response.JobApplicationResponse;

@Component
public class RecruitmentMapper {
    public JobPostingResponse toJobPostingResponse(JobPosting entity) {
        if (entity == null) return null;
        Long recruiterId = null;
        String recruiterName = null;
        if (entity.getRecruiter() != null) {
            recruiterId = entity.getRecruiter().getId();
            recruiterName = entity.getRecruiter().getFullName();
        }
        return JobPostingResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .requirements(entity.getRequirements())
                .salaryRange(entity.getSalaryRange())
                .location(entity.getLocation())
                .expiryDate(entity.getExpiryDate())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .recruiterId(recruiterId)
                .recruiterName(recruiterName)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public JobApplicationResponse toJobApplicationResponse(JobApplication entity) {
        if (entity == null) return null;
        Long studentId = null;
        String studentName = null;
        String jobTitle = null;
        if (entity.getCandidate() != null) {
            studentId = entity.getCandidate().getId();
            studentName = entity.getCandidate().getFullName();
        }
        if (entity.getJobPosting() != null) {
            jobTitle = entity.getJobPosting().getTitle();
        }
        return JobApplicationResponse.builder()
                .id(entity.getId())
                .jobId(entity.getJobPosting() != null ? entity.getJobPosting().getId() : null)
                .jobTitle(jobTitle)
                .studentId(studentId)
                .studentName(studentName)
                .cvUrl(entity.getCvUrl())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .appliedAt(entity.getAppliedAt())
                .recruiterNote(entity.getRecruiterNote())
                .build();
    }
}