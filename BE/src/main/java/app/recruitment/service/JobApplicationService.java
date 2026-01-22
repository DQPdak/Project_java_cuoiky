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

    // 3. Lấy danh sách Entity theo Job (dùng cho nội bộ Service hoặc Mapper)
    List<JobApplication> listByJob(Long jobId);

    // 4. Lấy danh sách Entity theo Candidate
    List<JobApplication> listByCandidateId(Long candidateId);

    // 5. Lấy danh sách DTO theo Candidate (trả về thẳng cho Frontend)
    List<JobApplicationResponse> getApplicationsByCandidateId(Long candidateId);

    // 6. Lấy chi tiết đơn
    Optional<JobApplication> getById(Long id);

    // 7. Lấy danh sách DTO theo Job (Tiện hơn hàm số 3 khi trả về API)
    List<JobApplicationResponse> getApplicationsByJobId(Long jobId);

    // 8. Cập nhật trạng thái nhanh (Dùng cho System hoặc AI tự động duyệt/loại, không cần RecruiterId)
    void updateApplicationStatus(Long applicationId, ApplicationStatus newStatus);

    // 9. Xóa đơn ứng tuyển (Cho phép ứng viên rút đơn)
    void deleteApplication(Long candidateId, Long applicationId);
}
