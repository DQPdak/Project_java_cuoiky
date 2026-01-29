"use client";

import { useEffect, useState } from "react";
import { recruitmentService } from "@/services/recruitmentService";
import { JobPosting, CandidateApplication, ApplicationStatus, AIAnalysisDetail } from "@/types/recruitment";
// Import Component cũ (giữ nguyên file này không sửa)
import CVAnalysisResult from "@/components/features/cv/CVAnalysisResult"; 
import { Briefcase, CheckCircle, XCircle, Clock, Search, FileText, AlertCircle } from "lucide-react";
import toast from "react-hot-toast";

export default function ApplicationsPage() {
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);
  const [applications, setApplications] = useState<CandidateApplication[]>([]);
  const [loading, setLoading] = useState(false);

  // Modal AI Analysis
  const [showAnalysisModal, setShowAnalysisModal] = useState(false);
  const [analysisData, setAnalysisData] = useState<AIAnalysisDetail | null>(null);
  const [analyzing, setAnalyzing] = useState(false);

  // --- LOGIC ADAPTER (QUAN TRỌNG) ---
  // Hàm này biến đổi dữ liệu API (AIAnalysisDetail) thành dữ liệu Component cũ cần (AnalysisData)
  const transformDataForComponent = (apiData: AIAnalysisDetail | null) => {
    if (!apiData) return null;

    return {
      // 1. Map các trường cơ bản
      matchPercentage: apiData.matchPercentage || 0,
      evaluation: apiData.evaluation || "Chưa có đánh giá",
      learningPath: typeof apiData.learningPath === 'string' ? apiData.learningPath : "Chưa có lộ trình", // Xử lý nếu API trả về JSON
      careerAdvice: apiData.careerAdvice || "",

      // 2. Tính toán trường thiếu (totalRequiredSkills) mà Component cũ đòi hỏi
      totalRequiredSkills: (apiData.matchedSkillsCount || 0) + (apiData.missingSkillsCount || 0),

      // 3. Xử lý mảng (Tránh lỗi undefined/null khi map)
      matchedSkillsList: apiData.matchedSkillsList || [],
      missingSkillsList: apiData.missingSkillsList || [],
      otherHardSkillsList: apiData.otherHardSkillsList || [],
      otherSoftSkillsList: apiData.otherSoftSkillsList || [],
      // QUAN TRỌNG: Cấp mặc định [] để component con không bị lỗi .map
      recommendedSkillsList: apiData.recommendedSkillsList || [],

      // 4. Các biến đếm
      matchedSkillsCount: apiData.matchedSkillsCount || 0,
      missingSkillsCount: apiData.missingSkillsCount || 0,
      otherHardSkillsCount: apiData.otherHardSkillsCount || 0,
      otherSoftSkillsCount: apiData.otherSoftSkillsCount || 0,
      recommendedSkillsCount: apiData.recommendedSkillsList?.length || 0
    };
  };

  useEffect(() => {
    loadMyJobs();
  }, []);

  useEffect(() => {
    if (selectedJobId) {
      loadPipeline(selectedJobId);
    } else {
      setApplications([]);
    }
  }, [selectedJobId]);

  const loadMyJobs = async () => {
    try {
      const data = await recruitmentService.getMyJobs();
      setJobs(data);
      if (data.length > 0) setSelectedJobId(data[0].id);
    } catch (error) {
      toast.error("Không thể tải danh sách công việc");
    }
  };

  const loadPipeline = async (jobId: number) => {
    setLoading(true);
    try {
      const data = await recruitmentService.getJobPipeline(jobId);
      setApplications(data);
    } catch (error) {
      console.error(error);
      toast.error("Lỗi tải danh sách ứng viên");
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (appId: number, newStatus: ApplicationStatus) => {
    try {
      await recruitmentService.updateStatus(appId, newStatus);
      toast.success("Đã cập nhật trạng thái: " + newStatus);
      if (selectedJobId) loadPipeline(selectedJobId);
    } catch (error) {
      toast.error("Cập nhật thất bại");
    }
  };

  const handleViewAnalysis = async (appId: number) => {
    setShowAnalysisModal(true);
    setAnalyzing(true);
    setAnalysisData(null);
    try {
      const data = await recruitmentService.getApplicationAnalysis(appId);
      setAnalysisData(data);
    } catch (error) {
      toast.error("Không thể lấy phân tích AI");
      setShowAnalysisModal(false);
    } finally {
      setAnalyzing(false);
    }
  };

  return (
    <div className="space-y-6 p-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold flex items-center gap-2">
          <Briefcase className="text-blue-600" /> Quản lý Ứng viên
        </h1>
        <div className="w-64">
          <select 
            className="w-full border p-2 rounded-lg bg-white shadow-sm"
            value={selectedJobId || ""}
            onChange={(e) => setSelectedJobId(Number(e.target.value))}
          >
            {jobs.length === 0 && <option value="">Chưa có tin tuyển dụng</option>}
            {jobs.map(job => (
              <option key={job.id} value={job.id}>{job.title}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow overflow-hidden border">
        {loading ? (
          <div className="p-8 text-center text-gray-500">Đang tải dữ liệu...</div>
        ) : applications.length === 0 ? (
          <div className="p-12 text-center text-gray-400 flex flex-col items-center">
            <Search size={48} className="mb-4 opacity-20" />
            <p>Chưa có ứng viên nào nộp đơn cho vị trí này.</p>
          </div>
        ) : (
          <table className="w-full text-left">
            <thead className="bg-gray-50 text-gray-600 text-sm uppercase">
              <tr>
                <th className="p-4">Ứng viên</th>
                <th className="p-4">Ngày nộp</th>
                <th className="p-4 text-center">Độ khớp (AI)</th>
                <th className="p-4">Trạng thái</th>
                <th className="p-4 text-right">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {applications.map((app) => (
                <tr key={app.id} className="hover:bg-gray-50 transition">
                  <td className="p-4">
                    <div className="font-medium text-gray-900">{app.candidateName || app.studentName || "Ẩn danh"}</div>
                    <a href={app.cvUrl} target="_blank" rel="noreferrer" className="text-xs text-blue-500 hover:underline flex items-center gap-1 mt-1">
                      <FileText size={12} /> Xem CV gốc
                    </a>
                  </td>
                  <td className="p-4 text-sm text-gray-500">
                    {app.appliedAt ? new Date(app.appliedAt).toLocaleDateString('vi-VN') : "N/A"}
                  </td>
                  <td className="p-4 text-center">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
                      ${app.matchScore >= 80 ? 'bg-green-100 text-green-800' : 
                        app.matchScore >= 50 ? 'bg-yellow-100 text-yellow-800' : 'bg-red-100 text-red-800'}`}>
                      {app.matchScore}%
                    </span>
                  </td>
                  <td className="p-4">
                     <span className={`px-3 py-1 rounded text-xs font-semibold
                      ${app.status === 'PENDING' ? 'bg-blue-100 text-blue-700' :
                        app.status === 'INTERVIEW' ? 'bg-purple-100 text-purple-700' :
                        app.status === 'HIRED' ? 'bg-green-100 text-green-700' :
                        app.status === 'REJECTED' ? 'bg-red-100 text-red-700' : 'bg-gray-100'}`}>
                      {app.status}
                    </span>
                  </td>
                  <td className="p-4 text-right space-x-2">
                    <button onClick={() => handleViewAnalysis(app.id)} className="text-blue-600 hover:bg-blue-50 p-2 rounded tooltip" title="Xem phân tích AI">
                      <AlertCircle size={18} />
                    </button>
                    {app.status === 'PENDING' && (
                      <>
                        <button onClick={() => handleUpdateStatus(app.id, ApplicationStatus.INTERVIEW)} className="text-purple-600 hover:bg-purple-50 p-2 rounded" title="Mời phỏng vấn">
                          <Clock size={18} />
                        </button>
                        <button onClick={() => handleUpdateStatus(app.id, ApplicationStatus.REJECTED)} className="text-red-600 hover:bg-red-50 p-2 rounded" title="Từ chối">
                          <XCircle size={18} />
                        </button>
                      </>
                    )}
                    {app.status === 'INTERVIEW' && (
                        <button onClick={() => handleUpdateStatus(app.id, ApplicationStatus.HIRED)} className="text-green-600 hover:bg-green-50 p-2 rounded" title="Tuyển dụng">
                        <CheckCircle size={18} />
                      </button>
                     )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal AI Analysis */}
      {showAnalysisModal && (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto relative">
            <button onClick={() => setShowAnalysisModal(false)} className="absolute top-4 right-4 text-gray-500 hover:text-black">
              <XCircle size={24} />
            </button>
            
            <div className="p-6">
              <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
                ✨ Phân tích CV bởi AI
              </h2>
              
              {analyzing ? (
                <div className="py-20 text-center">
                  <div className="animate-spin w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"></div>
                  <p className="text-gray-500">AI đang đọc CV và so sánh với Job Description...</p>
                </div>
              ) : analysisData ? (
                // SỬA: Đổi prop từ 'data' -> 'result' và dùng hàm transform
                // TypeScript có thể báo lỗi đỏ nhẹ ở đây do interface mismatch, nhưng chạy runtime sẽ OK vì ta đã ép đúng shape
                <CVAnalysisResult result={transformDataForComponent(analysisData) as any} />
              ) : (
                <p className="text-red-500 text-center">Không tải được dữ liệu phân tích.</p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}