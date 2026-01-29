// src/services/recruitmentService.ts
import api from './api';
import { 
  JobPosting, 
  JobCreateRequest, 
  CandidateApplication, 
  ApplicationStatus,
  AIAnalysisDetail,
  CandidateSearchResult, 
} from '@/types/recruitment';

export interface DashboardStats {
    totalActiveJobs: number;
    totalCandidates: number;
    newCandidatesToday: number;
    pipelineStats: Record<string, number>;
}

export interface CompanyProfile {
    id?: number;
    name: string;
    description: string;
    industry: string;
    size: string;
    foundedYear: string; // Chú ý tên biến khớp BE
    website: string;
    address: string;
    phone: string;
    email: string;
    logoUrl?: string;
    coverImageUrl?: string;
}

export const recruitmentService = {
  // --- NHÓM JOB (TIN TUYỂN DỤNG) ---

  // 1. Lấy danh sách tin của tôi
  getMyJobs: async (): Promise<JobPosting[]> => {
    const res = await api.get('/recruiter/jobs/me'); 
    return res.data; // BE trả về List<JobPostingResponse>
  },

  // 2. Tạo tin mới
  createJob: async (data: JobCreateRequest): Promise<JobPosting> => {
    const res = await api.post('/recruiter/jobs', data);
    return res.data;
  },

  // 3. Xóa tin
  deleteJob: async (id: number): Promise<void> => {
    await api.delete(`/recruiter/jobs/${id}`);
  },

  // 4. Tìm kiếm Job (để khớp với BE searchJobs)
  searchMyJobs: async (keyword: string): Promise<any> => {
     const res = await api.get('/recruiter/jobs/search', {
        params: { keyword }
     });
     return res.data.data; // BE trả về MessageResponse nên data nằm trong .data
  },

  // --- NHÓM DASHBOARD ---
  getDashboardStats: async (): Promise<DashboardStats> => {
    const response = await api.get('/recruiter/dashboard/stats');
    return response.data;
  },

  // --- NHÓM ỨNG VIÊN (PIPELINE) ---

  // 5. Lấy danh sách ứng viên của 1 Job
  // QUAN TRỌNG: Kiểm tra lại prefix URL trong JobApplicationController ở BE
  // Giả sử Controller là /api/recruitment/applications hoặc /api/applications
  getJobPipeline: async (jobId: number): Promise<CandidateApplication[]> => {
    // Nếu BE là @RequestMapping("/api/recruitment/applications")
    const res = await api.get(`/recruitment/applications/job/${jobId}`); 
    return res.data;
  },

  // 6. Cập nhật trạng thái ứng viên
  updateStatus: async (appId: number, status: ApplicationStatus, note?: string) => {
    const res = await api.put(`/recruitment/applications/${appId}/status`, null, {
        params: { newStatus: status, recruiterNote: note }
    });
    return res.data;
  },

  // 7. Lấy chi tiết phân tích AI (Modal)
  getApplicationAnalysis: async (applicationId: number): Promise<AIAnalysisDetail> => {
    const res = await api.get(`/recruitment/applications/${applicationId}/analysis`);
    return res.data.data || res.data;
  },
  // 8. Lấy danh sách ứng tuyển của tôi
  getMyApplications: async (): Promise<any[]> => {
      // Backend: @GetMapping("/me")
      const res = await api.get('/recruitment/applications/me');
      return res.data.data; // Backend trả về MessageResponse nên data nằm trong .data
  },
  // 9. Tìm kiếm ứng viên bằng AI (Match Description)
  searchCandidates: async (query: string): Promise<CandidateSearchResult[]> => {
    // Endpoint: POST /api/recruitment/search/match-description
    const res = await api.post('/recruitment/search/match-description', query, {
        headers: { "Content-Type": "text/plain" }, // Backend nhận String nên cần header này

    });
  
    return res.data.data;},

  // --- NHÓM CÔNG TY (RECRUITER COMPANY PROFILE) ---
    getMyCompany: async (): Promise<CompanyProfile> => {
        const res = await api.get('/recruiter/company/me');
        return res.data;
    },

    updateCompany: async (data: CompanyProfile): Promise<CompanyProfile> => {
        const res = await api.put('/recruiter/company/me', data);
        return res.data;
    }
};