import api from "./api";

// --- INTERFACES ---

// 1. Interface cho Job
export interface RecruiterJob {
  id: number;
  title: string;
  status: string; // PUBLISHED, CLOSED, DRAFT...
}

// 2. Interface cho Application
export interface RecruiterApplication {
  id: number;
  jobId: number;
  jobTitle: string;
  companyName?: string;
  studentId: number;
  studentName: string;
  cvUrl: string;
  status: "PENDING" | "APPROVED" | "REJECTED" | "INTERVIEW";
  appliedAt: string;
  recruiterNote?: string;

  // Các trường AI tóm tắt
  matchScore: number;
  aiEvaluation: string;
  missingSkillsList: string;
}

// 3. Interface cho chi tiết phân tích AI (cho Modal)
export interface AIAnalysisDetail {
  matchPercentage: number;
  evaluation: string;

  matchedSkillsCount: number;
  missingSkillsCount: number;
  otherHardSkillsCount?: number;
  otherSoftSkillsCount?: number;

  matchedSkillsList: string[];
  missingSkillsList: string[];
  otherHardSkillsList?: string[];
  otherSoftSkillsList?: string[];
  recommendedSkillsList?: string[];

  learningPath?: any;
  careerAdvice?: string;
  jobTitle?: string;
  candidateName?: string;
}

// --- API CALLS ---

// 1. Lấy danh sách Job của Recruiter (Sidebar)
export const getMyJobs = async (): Promise<RecruiterJob[]> => {
  // Endpoint: GET /api/recruiter/jobs/me
  const response = await api.get("/recruiter/jobs/me");
  return response.data;
};

// 2. Lấy danh sách Ứng viên theo Job ID (Main Content)
// ĐÂY LÀ HÀM DUY NHẤT ĐỂ LẤY DANH SÁCH ỨNG VIÊN
export const getApplicationsByJob = async (
  jobId: number,
): Promise<RecruiterApplication[]> => {
  // Endpoint: GET /api/applications/job/{jobId}
  const response = await api.get(`/applications/job/${jobId}`);
  // Backend trả về List<JobApplicationResponse> trực tiếp (Array)
  return response.data;
};

// 3. Lấy chi tiết phân tích AI (Modal AI)
export const getApplicationAnalysis = async (
  applicationId: number,
): Promise<AIAnalysisDetail> => {
  // Endpoint: GET /api/applications/{id}/analysis
  const response = await api.get(`/applications/${applicationId}/analysis`);
  // Xử lý data trả về tùy theo format của BE (MessageResponse hay Direct Object)
  return response.data.data || response.data;
};

// 4. Cập nhật trạng thái hồ sơ (Duyệt/Loại)
export const updateApplicationStatus = async (
  id: number,
  status: string,
  note?: string,
) => {
  // Endpoint: PUT /api/applications/{id}/status
  const response = await api.put(`/applications/${id}/status`, null, {
    params: {
      newStatus: status,
      recruiterNote: note,
    },
  });
  return response.data;
};
