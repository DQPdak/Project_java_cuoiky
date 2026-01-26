import api from "./api";
import { CandidateResponse, CandidateProfile } from "@/types/candidate";

// API Upload CV
export const uploadCV = async (file: File): Promise<CandidateProfile> => {
  const formData = new FormData();
  formData.append("file", file);

  // Header 'Content-Type': 'multipart/form-data' thường được axios tự động set khi thấy FormData
  const response = await api.post<CandidateResponse>(
    "/candidate/profile/upload-cv",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    },
  );

  return response.data.data;
};

// API Lấy thông tin hồ sơ hiện tại
export const getMyProfile = async (): Promise<CandidateProfile> => {
  const response = await api.get<CandidateResponse>("/candidate/profile/me");
  return response.data.data;
};

export const getAllJobs = async () => {
  // Giả sử endpoint BE là /recruitment/jobs/active hoặc /recruitment/jobs
  // Bạn cần kiểm tra lại Controller BE để có endpoint chính xác
  const response = await api.get("/candidate/recommendations/all");
  return response.data.data;
};

export const getRecentJobs = async () => {
  const response = await api.get("/candidate/recommendations/recent");
  return response.data.data;
};

export const getMatchingJobs = async () => {
  // Gọi API: /api/candidate/recommendations/matching
  const response = await api.get("/candidate/recommendations/matching");
  return response.data.data;
};

export const applyJob = async (jobId: number) => {
  // Gửi request POST, body có thể cần thêm coverLetter nếu muốn
  const response = await api.post("/applications/apply", {
    jobId: jobId,
  });
  return response.data;
};

// API Lấy danh sách việc đã ứng tuyển (để hiển thị trạng thái)
export const getMyApplications = async () => {
  // Giả định endpoint BE là /job-applications/my-application
  // Bạn cần kiểm tra lại file JobApplicationController.java để chắc chắn đường dẫn
  const response = await api.get("/applications/me");
  return response.data.data;
};

export const updateProfile = async (data: any) => {
  const response = await api.put("/candidate/profile/me", data);
  return response.data.data;
};

// Hàm gọi API tính điểm nhanh (Batch)
export const getBatchScores = async (jobIds: number[]) => {
  // Gọi endpoint: /api/matching/candidate/batch-scores
  const response = await api.post(`/matching/candidate/batch-scores`, jobIds);
  return response.data; // Trả về Map<Long, Integer>: { "101": 85, "102": 70 }
};

// Hàm gọi API phân tích CV
export const getJobAnalysisResult = async (jobId: number) => {
  try {
    // Lưu ý: Endpoint này gọi AI nên có thể mất 3-5 giây
    const response = await api.get(`/matching/candidate/preview/${jobId}`);

    // Backend trả về MatchResult object
    return response.data;
  } catch (error) {
    console.error("Lỗi lấy kết quả phân tích:", error);
    throw error;
  }
};
