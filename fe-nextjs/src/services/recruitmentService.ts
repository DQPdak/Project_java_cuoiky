// Đường dẫn: fe-nextjs/src/services/recruitmentService.ts

import api from './api';
import { JobPosting, JobCreateRequest, CandidateApplication, ApplicationStatus } from '@/types/recruitment';

export interface DashboardStats {
    totalActiveJobs: number;
    totalCandidates: number;
    newCandidatesToday: number;
    pipelineStats: Record<string, number>;
}

export const recruitmentService = {
  // 1. Lấy danh sách tin tuyển dụng
  getMyJobs: async (): Promise<JobPosting[]> => {
    const res = await api.get('/recruiter/jobs/me'); 
    return res.data;
  },

  // 2. Tạo tin tuyển dụng mới
  createJob: async (data: JobCreateRequest): Promise<JobPosting> => {
    // SỬA: Đường dẫn đúng theo Controller Java
    const res = await api.post('/recruiter/jobs', data);
    return res.data;
  },

  // 3. Lấy danh sách ứng viên (Pipeline) của 1 Job
  getJobPipeline: async (jobId: number): Promise<CandidateApplication[]> => {
    const res = await api.get(`/recruitment/applications/job/${jobId}`);
    return res.data;
  },

  // 4. Cập nhật trạng thái ứng viên
  updateStatus: async (appId: number, status: ApplicationStatus) => {
    await api.patch(`/recruitment/applications/${appId}/status`, { status });
  },

  // 5. Tìm kiếm ứng viên bằng AI
  searchCandidates: async (query: string) => {
    const res = await api.post('/recruitment/search/match-description', query, {
        headers: { "Content-Type": "text/plain" } // Backend nhận String body, cần header này
    });
    return res.data;
  },
  // 6. Lấy thống kê Dashboard (Mới thêm)
  getDashboardStats: async (): Promise<DashboardStats> => {
    const response = await api.get('/recruiter/dashboard/stats');
    return response.data;
  },
};