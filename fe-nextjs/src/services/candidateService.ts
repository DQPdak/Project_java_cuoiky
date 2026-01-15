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
    }
  );

  return response.data.data;
};

// API Lấy thông tin hồ sơ hiện tại
export const getMyProfile = async (): Promise<CandidateProfile> => {
  const response = await api.get<CandidateResponse>("/candidate/profile/me");
  return response.data.data;
};

export const getRecommendedJobs = async () => {
  const response = await api.get("/candidate/recommendations");
  return response.data.data; // Trả về List Job
};

export const applyJob = async (jobId: number) => {
  // Gửi request POST, body có thể cần thêm coverLetter nếu muốn
  const response = await api.post("/recruitment/applications", {
    jobId: jobId,
  });
  return response.data;
};

// API Lấy danh sách việc đã ứng tuyển (để hiển thị trạng thái)
export const getMyApplications = async () => {
  const response = await api.get("/recruitment/applications/my-applications"); // Bạn cần tạo endpoint này ở BE nếu chưa có
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
