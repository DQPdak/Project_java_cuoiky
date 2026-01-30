package app.recruitment.service;

import app.recruitment.dto.request.JobApplicationRequest;
import app.recruitment.dto.response.JobApplicationResponse;
import app.recruitment.entity.JobApplication;
import app.recruitment.entity.enums.ApplicationStatus;

import java.util.List;
import java.util.Optional;

public interface JobApplicationService {
    
    // 1. Ứng viên nộp đơn
    JobApplication apply(Long candidateId, JobApplicationRequest request);

    // 2. Recruiter cập nhật trạng thái (đầy đủ thông tin người duyệt + ghi chú)
    // Hàm này đang được Controller sử dụng chính
    JobApplication updateStatus(Long recruiterId, Long applicationId, ApplicationStatus newStatus, String recruiterNote);
    
    List<JobApplicationResponse> listByJob(Long jobId);
    
    // [QUAN TRỌNG] Đổi listByStudent -> listByCandidateId để khớp với Service Impl
    List<JobApplication> listByCandidateId(Long candidateId);

    // 5. Lấy danh sách DTO theo Candidate (trả về thẳng cho Frontend)
    List<JobApplicationResponse> getApplicationsByCandidateId(Long candidateId);

    List<JobApplicationResponse> scanAndSuggestCandidates(Long jobId);
    // 6. Lấy chi tiết đơn
    Optional<JobApplication> getById(Long id);
    
    JobApplicationResponse getDetail(Long id);
}
