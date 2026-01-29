// src/services/recruitmentService.ts
import api from './api';
import { 
  JobPosting, 
  JobCreateRequest, 
  CandidateApplication, 
  ApplicationStatus,
  AIAnalysisDetail,
  CandidateSearchResult, 
} from '@/types/recruitment'; //

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
    foundedYear: string; 
    website: string;
    address: string;
    phone: string;
    email: string;
    logoUrl?: string;
    coverImageUrl?: string;
}

export const recruitmentService = {
  // --- NHÓM JOB (TIN TUYỂN DỤNG) ---
  getMyJobs: async (): Promise<JobPosting[]> => {
    const res = await api.get('/recruiter/jobs/me'); 
    return res.data; 
  },

  createJob: async (data: JobCreateRequest): Promise<JobPosting> => {
    const res = await api.post('/recruiter/jobs', data);
    return res.data;
  },

  deleteJob: async (id: number): Promise<void> => {
    await api.delete(`/recruiter/jobs/${id}`);
  },

  searchMyJobs: async (keyword: string): Promise<any> => {
     const res = await api.get('/recruiter/jobs/search', {
        params: { keyword }
     });
     return res.data.data;
  },

  // --- NHÓM DASHBOARD ---
  getDashboardStats: async (): Promise<DashboardStats> => {
    const response = await api.get('/recruiter/dashboard/stats');
    return response.data;
  },

  getRecentApplications: async (): Promise<CandidateApplication[]> => {
    const response = await api.get('/recruiter/dashboard/recent-applications');
    return response.data;
  },

  // --- NHÓM ỨNG VIÊN (PIPELINE) ---
  getJobPipeline: async (jobId: number): Promise<CandidateApplication[]> => {
    const res = await api.get(`/recruitment/applications/job/${jobId}`); 
    return res.data;
  },

  updateStatus: async (appId: number, status: ApplicationStatus, note?: string) => {
    const res = await api.put(`/recruitment/applications/${appId}/status`, null, {
        params: { newStatus: status, recruiterNote: note }
    });
    return res.data;
  },

  getApplicationAnalysis: async (applicationId: number): Promise<AIAnalysisDetail> => {
    const res = await api.get(`/recruitment/applications/${applicationId}/analysis`);
    return res.data.data || res.data;
  },

  getMyApplications: async (): Promise<any[]> => {
      const res = await api.get('/recruitment/applications/me');
      return res.data.data; 
  },

  searchCandidates: async (query: string): Promise<CandidateSearchResult[]> => {
    const res = await api.post('/recruitment/search/match-description', query, {
        headers: { "Content-Type": "text/plain" },
    });
    return res.data.data;
  },

  // --- NHÓM CÔNG TY ---
  getMyCompany: async (): Promise<CompanyProfile> => {
      const res = await api.get('/recruiter/company/me');
      return res.data;
  },

  updateCompany: async (data: CompanyProfile): Promise<CompanyProfile> => {
      const res = await api.put('/recruiter/company/me', data);
      return res.data;
  },

  // --- NHÓM DÀNH CHO ỨNG VIÊN (CANDIDATE) ---
  
  // Sửa từ /recruitment/ thành /recruiter/ để khớp Backend
  getJobDetail: async (id: number): Promise<JobPosting> => {
    const res = await api.get(`/recruiter/jobs/public/${id}`);
    return res.data;
  },
  
  checkApplicationStatus: async (jobId: number): Promise<string | null> => {
      try {
          const res = await api.get(`/recruitment/applications/check/${jobId}`);
          return res.data.status; 
      } catch (e) {
          return null;
      }
  }
};