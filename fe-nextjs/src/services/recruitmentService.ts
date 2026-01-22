// Đường dẫn: fe-nextjs/src/services/recruitmentService.ts

import api from './api'; // Đảm bảo bạn đã có file api.ts cấu hình axios
import { JobPosting, JobCreateRequest, CandidateApplication, ApplicationStatus } from '@/types/recruitment';

export const recruitmentService = {
  // 1. Lấy danh sách tin tuyển dụng
  getMyJobs: async (): Promise<JobPosting[]> => {
    // API này cần khớp với Controller bên BE
    const res = await api.get('/recruitment/jobs/my-jobs');
    return res.data;
  },

  // 2. Tạo tin tuyển dụng mới
  createJob: async (data: JobCreateRequest): Promise<JobPosting> => {
    const res = await api.post('/recruitment/jobs', data);
    return res.data;
  },

  // 3. Lấy danh sách ứng viên (Pipeline) của 1 Job
  getJobPipeline: async (jobId: number): Promise<CandidateApplication[]> => {
    const res = await api.get(`/recruitment/applications/job/${jobId}`);
    return res.data;
  },

  // 4. Cập nhật trạng thái ứng viên (Sơ tuyển -> Phỏng vấn...)
  updateStatus: async (appId: number, status: ApplicationStatus) => {
    await api.patch(`/recruitment/applications/${appId}/status`, { status });
  },

  // 5. Tìm kiếm ứng viên bằng AI
  searchCandidates: async (query: string) => {
    const res = await api.post('/recruitment/search/match-description', query);
    return res.data;
  }
};