import api from "./api";
import { CandidateResponse, CandidateProfile } from "@/types/candidate";

//

/**
 * Định nghĩa cấu trúc dữ liệu cho yêu cầu ứng tuyển
 */
interface ApplyJobRequest {
  jobId: number;
  coverLetter?: string;
  cvUrl?: string;
}

// API Upload CV
export const uploadCV = async (file: File): Promise<CandidateProfile> => {
  const formData = new FormData();
  formData.append("file", file);

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
  const response = await api.get("/candidate/recommendations/all");
  return response.data.data;
};

export const getRecentJobs = async () => {
  const response = await api.get("/candidate/recommendations/recent");
  return response.data.data;
};

export const getMatchingJobs = async () => {
  const response = await api.get("/candidate/recommendations/matching");
  return response.data.data;
};

/**
 * Cập nhật hàm applyJob để nhận Object thay vì chỉ nhận jobId
 * Điều này cho phép gửi thêm coverLetter và cvUrl lên Backend
 */
export const applyJob = async (data: ApplyJobRequest) => {
  const response = await api.post("/applications/apply", data);
  return response.data;
};

// API Lấy danh sách việc đã ứng tuyển
export const getMyApplications = async () => {
  const response = await api.get("/applications/me");
  return response.data.data;
};

export const updateProfile = async (data: any) => {
  const response = await api.put("/candidate/profile/me", data);
  return response.data.data;
};

// Hàm gọi API tính điểm nhanh (Batch)
export const getBatchScores = async (jobIds: number[]) => {
  const response = await api.post(`/matching/candidate/batch-scores`, jobIds);
  return response.data;
};

// Hàm gọi API phân tích CV
export const getJobAnalysisResult = async (jobId: number) => {
  try {
    const response = await api.get(`/matching/candidate/preview/${jobId}`);
    return response.data;
  } catch (error) {
    console.error("Lỗi lấy kết quả phân tích:", error);
    throw error;
  }
};

export const cancelApplication = async (applicationId: number) => {
  const response = await api.delete(`/applications/${applicationId}`);
  return response.data;
};