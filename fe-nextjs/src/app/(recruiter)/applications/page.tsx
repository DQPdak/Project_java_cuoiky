"use client";

import { useEffect, useState } from "react";
import * as recruiterService from "@/services/recruiterService";
import CVAnalysisResult from "@/components/features/cv/CVAnalysisResult";
import {
  Briefcase,
  Sparkles,
  Eye,
  CheckCircle,
  XCircle,
  Clock,
  ChevronRight,
  AlertCircle,
  Download,
  X,
  FileText,
  Loader2,
  Filter,
} from "lucide-react";
import toast, { Toaster } from "react-hot-toast";

// --- HELPER: Xử lý chuỗi kỹ năng thành mảng an toàn ---
const safeSplit = (input: any): string[] => {
  if (Array.isArray(input)) return input;
  if (typeof input === "string" && input.trim().length > 0) {
    // Tách dấu phẩy, xóa khoảng trắng thừa và lọc phần tử rỗng
    return input
      .split(",")
      .map((s) => s.trim())
      .filter((s) => s !== "");
  }
  return [];
};

// --- 1. MODAL XEM CV & DUYỆT ---
const CVDetailModal = ({ app, onClose, onUpdateStatus }: any) => {
  if (!app) return null;
  console.log("CVDetailModal app:", app.cvUrl);
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="bg-white w-full max-w-7xl h-[90vh] rounded-2xl shadow-2xl flex flex-col overflow-hidden">
        {/* Header Modal */}
        <div className="flex justify-between items-center px-6 py-4 border-b border-gray-200 bg-white">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 font-bold text-lg">
              {app.studentName.charAt(0).toUpperCase()}
            </div>
            <div>
              <h2 className="text-lg font-bold text-gray-800">
                {app.studentName}
              </h2>
              <p className="text-sm text-gray-500">
                Ứng tuyển vị trí:{" "}
                <span className="font-medium text-gray-700">
                  {app.jobTitle}
                </span>
              </p>
            </div>
            {/* Badge Điểm số */}
            <div
              className={`ml-4 px-3 py-1 rounded-full text-sm font-bold border flex items-center gap-1 ${
                app.matchScore >= 50
                  ? "bg-green-50 text-green-700 border-green-200"
                  : "bg-yellow-50 text-yellow-700 border-yellow-200"
              }`}
            >
              <Sparkles size={14} /> {app.matchScore}% Phù hợp
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition"
          >
            <X size={24} />
          </button>
        </div>

        {/* Body: PDF Viewer */}
        <div className="flex-1 bg-gray-50 p-6 overflow-hidden flex flex-col">
          {app.cvUrl ? (
            <div className="w-full h-full flex flex-col">
              <div className="flex-1 bg-gray-200 rounded-xl border border-gray-300 overflow-hidden relative shadow-inner">
                {/* Dùng object tag để hỗ trợ xem PDF tốt hơn iframe Google Docs */}
                <iframe
                  src={`https://docs.google.com/gview?url=${app.cvUrl}&embedded=true`}
                  className="w-full h-full absolute inset-0"
                ></iframe>
              </div>

              <div className="mt-3 flex justify-between items-center px-1">
                <p className="text-sm text-gray-500 italic">
                  * Nếu không thấy nội dung, hãy mở file gốc.
                </p>
                <a
                  href={app.cvUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center gap-2 text-blue-600 hover:underline text-sm font-semibold transition bg-blue-50 px-3 py-1.5 rounded-lg hover:bg-blue-100"
                >
                  <Download size={16} /> Xem file gốc / Tải về ↗
                </a>
              </div>
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-full text-gray-400">
              <FileText size={48} className="mb-4 opacity-50" />
              <p>Ứng viên chưa cập nhật CV</p>
            </div>
          )}
        </div>

        {/* Footer: Action Bar */}
        <div className="px-6 py-4 bg-white border-t border-gray-200 flex justify-between items-center shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)]">
          <div className="text-sm text-gray-500">
            Trạng thái:{" "}
            <span
              className={`font-bold ${
                app.status === "APPROVED"
                  ? "text-green-600"
                  : app.status === "REJECTED"
                    ? "text-red-600"
                    : "text-yellow-600"
              }`}
            >
              {app.status}
            </span>
          </div>
          <div className="flex gap-3">
            <button
              onClick={onClose}
              className="px-5 py-2.5 rounded-lg text-gray-600 font-medium hover:bg-gray-100 transition"
            >
              Đóng
            </button>

            {app.status === "PENDING" && (
              <>
                <button
                  onClick={() => onUpdateStatus(app.id, "REJECTED")}
                  className="px-5 py-2.5 rounded-lg border border-red-200 text-red-600 font-semibold hover:bg-red-50 flex items-center gap-2 transition"
                >
                  <XCircle size={18} /> Từ chối
                </button>
                <button
                  onClick={() => onUpdateStatus(app.id, "APPROVED")}
                  className="px-6 py-2.5 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700 shadow-lg shadow-blue-200 flex items-center gap-2 transition transform hover:-translate-y-0.5"
                >
                  <CheckCircle size={18} /> Duyệt phỏng vấn
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// --- 2. MODAL AI ANALYSIS (Đã sửa lỗi map function & NaN) ---
const AnalysisModal = ({ app, onClose }: any) => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (app) {
      setLoading(true);
      // Gọi API lấy dữ liệu chi tiết
      recruiterService
        .getApplicationAnalysis(app.id)
        .then((res: any) => {
          // Chuẩn hóa dữ liệu trả về từ API
          const mappedData = {
            ...res,
            // 1. Map điểm số (tránh NaN)
            matchPercentage: res.matchScore ?? res.matchPercentage ?? 0,
            evaluation: res.aiEvaluation || "Chưa có đánh giá chi tiết.",

            // 2. Map các danh sách kỹ năng (Tránh lỗi .map is not a function)
            // Sử dụng hàm safeSplit cho TẤT CẢ các trường list
            matchedSkillsList: safeSplit(res.matchedSkillsList),
            missingSkillsList: safeSplit(res.missingSkillsList),
            otherHardSkillsList: safeSplit(res.otherHardSkillsList),
            otherSoftSkillsList: safeSplit(res.otherSoftSkillsList),
            recommendedSkillsList: safeSplit(res.recommendedSkillsList),

            // 3. Map số lượng (nếu API trả về null thì đếm từ mảng đã split)
            matchedSkillsCount:
              res.matchedSkillsCount || safeSplit(res.matchedSkillsList).length,
            missingSkillsCount:
              res.missingSkillsCount || safeSplit(res.missingSkillsList).length,
            otherHardSkillsCount:
              res.otherHardSkillsCount ||
              safeSplit(res.otherHardSkillsList).length,
            otherSoftSkillsCount:
              res.otherSoftSkillsCount ||
              safeSplit(res.otherSoftSkillsList).length,
            recommendedSkillsCount:
              res.recommendedSkillsCount ||
              safeSplit(res.recommendedSkillsList).length,

            // 4. Map thông tin chung
            candidateName:
              res.candidateName || res.studentName || app.studentName,
            jobTitle: res.jobTitle || app.jobTitle,
          };

          setData(mappedData);
        })
        .catch((err) => {
          console.error("Lỗi lấy dữ liệu AI:", err);
          // Fallback data an toàn nếu API lỗi
          setData({
            matchPercentage: app.matchScore || 0,
            evaluation: app.aiEvaluation || "Chưa có đánh giá chi tiết.",

            // Vẫn dùng safeSplit cho dữ liệu có sẵn từ danh sách
            missingSkillsList: safeSplit(app.missingSkillsList),
            matchedSkillsList: [],
            otherHardSkillsList: [],
            otherSoftSkillsList: [],
            recommendedSkillsList: [],

            matchedSkillsCount: 0,
            missingSkillsCount: safeSplit(app.missingSkillsList).length,
            otherHardSkillsCount: 0,
            otherSoftSkillsCount: 0,
            recommendedSkillsCount: 0,

            jobTitle: app.jobTitle,
            candidateName: app.studentName,
          });
        })
        .finally(() => setLoading(false));
    }
  }, [app]);

  if (!app) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-in fade-in">
      <div className="bg-white w-full max-w-5xl max-h-[90vh] rounded-2xl shadow-2xl flex flex-col overflow-hidden">
        <div className="flex justify-between items-center px-6 py-4 border-b border-gray-100 bg-white z-10">
          <h3 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <Sparkles className="text-purple-600" /> Phân tích AI Chi tiết
          </h3>
          <button onClick={onClose}>
            <X className="text-gray-400 hover:text-gray-600" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6 bg-gray-50 custom-scrollbar">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-20 text-gray-500">
              <Loader2 className="w-10 h-10 animate-spin text-purple-600 mb-3" />
              <p>Đang tải dữ liệu phân tích...</p>
            </div>
          ) : (
            <CVAnalysisResult result={data} />
          )}
        </div>
      </div>
    </div>
  );
};

// --- 3. TRANG CHÍNH ---
export default function ApplicationsPage() {
  const [jobs, setJobs] = useState<recruiterService.RecruiterJob[]>([]);
  const [currentApplications, setCurrentApplications] = useState<
    recruiterService.RecruiterApplication[]
  >([]);

  // State quản lý lựa chọn: Mặc định là null, sẽ set thành job đầu tiên khi fetch xong
  const [selectedJobId, setSelectedJobId] = useState<number | null>(null);

  const [activeTab, setActiveTab] = useState<"all" | "matching">("all");
  const [loading, setLoading] = useState(false);

  // State Modals
  const [viewCVApp, setViewCVApp] = useState<any>(null);
  const [analyzeApp, setAnalyzeApp] = useState<any>(null);

  // 1. Fetch Danh sách Jobs
  useEffect(() => {
    const fetchJobs = async () => {
      try {
        const jobsData = await recruiterService.getMyJobs();
        setJobs(jobsData || []);

        // Tự động chọn Job đầu tiên nếu có
        if (jobsData && jobsData.length > 0) {
          setSelectedJobId(jobsData[0].id);
        }
      } catch (error) {
        console.error("Lỗi lấy danh sách Job:", error);
      }
    };
    fetchJobs();
  }, []);

  // 2. Fetch Danh sách Hồ sơ (Chỉ chạy khi có selectedJobId hợp lệ)
  useEffect(() => {
    if (!selectedJobId) return;

    const fetchApps = async () => {
      setLoading(true);
      try {
        // Chỉ gọi API lấy theo Job ID
        const appsData =
          await recruiterService.getApplicationsByJob(selectedJobId);
        setCurrentApplications(appsData || []);
      } catch (error) {
        console.error("Lỗi lấy hồ sơ:", error);
        setCurrentApplications([]);
      } finally {
        setLoading(false);
      }
    };
    fetchApps();
  }, [selectedJobId]);

  // Handle Update Status
  const handleStatusUpdate = async (id: number, newStatus: string) => {
    if (
      !confirm(
        `Xác nhận ${newStatus === "APPROVED" ? "DUYỆT" : "LOẠI"} hồ sơ này?`,
      )
    )
      return;
    try {
      await recruiterService.updateApplicationStatus(id, newStatus);

      // Cập nhật UI ngay lập tức
      setCurrentApplications((prev) =>
        prev.map((app) =>
          app.id === id ? { ...app, status: newStatus as any } : app,
        ),
      );

      // Nếu đang mở modal xem CV của user này thì đóng lại
      if (viewCVApp && viewCVApp.id === id) {
        setViewCVApp(null);
      }

      toast.success("Cập nhật thành công!");
    } catch (e) {
      toast.error("Lỗi cập nhật trạng thái");
    }
  };

  // Logic lọc và sắp xếp
  const displayApplications = currentApplications
    .filter((app) => {
      if (activeTab === "matching") return (app.matchScore || 0) >= 50;
      return true;
    })
    .sort((a, b) => (b.matchScore || 0) - (a.matchScore || 0));

  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-700 bg-green-50 border-green-200";
    if (score >= 50) return "text-blue-700 bg-blue-50 border-blue-200";
    return "text-gray-600 bg-gray-50 border-gray-200";
  };

  return (
    <div className="h-[calc(100vh-64px)] bg-gray-50 flex overflow-hidden">
      <Toaster position="top-center" />

      {/* --- RENDER MODALS --- */}
      {viewCVApp && (
        <CVDetailModal
          app={viewCVApp}
          onClose={() => setViewCVApp(null)}
          onUpdateStatus={handleStatusUpdate}
        />
      )}
      {analyzeApp && (
        <AnalysisModal app={analyzeApp} onClose={() => setAnalyzeApp(null)} />
      )}

      {/* LEFT SIDEBAR: List Jobs */}
      <div className="w-1/3 min-w-[300px] max-w-[400px] bg-white border-r border-gray-200 flex flex-col">
        <div className="p-5 border-b border-gray-100">
          <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <Briefcase className="w-5 h-5 text-blue-600" /> Vị trí đang tuyển
          </h2>
          <p className="text-xs text-gray-500 mt-1">
            Chọn công việc để xem hồ sơ
          </p>
        </div>

        <div className="flex-1 overflow-y-auto p-3 space-y-2">
          {jobs.length === 0 && (
            <div className="text-center py-10 text-gray-400 text-sm">
              Chưa có tin tuyển dụng nào
            </div>
          )}

          {jobs.map((job) => (
            <button
              key={job.id}
              onClick={() => setSelectedJobId(job.id)}
              className={`w-full text-left p-4 rounded-lg border transition-all group ${
                selectedJobId === job.id
                  ? "bg-white border-blue-500 ring-1 ring-blue-500 shadow-md z-10"
                  : "bg-white border-gray-200 hover:border-blue-300 hover:shadow-sm"
              }`}
            >
              <div className="flex justify-between items-start mb-1">
                <h3
                  className={`font-semibold text-sm line-clamp-2 ${
                    selectedJobId === job.id ? "text-blue-700" : "text-gray-900"
                  }`}
                >
                  {job.title}
                </h3>
                {selectedJobId === job.id && (
                  <ChevronRight className="w-4 h-4 text-blue-500" />
                )}
              </div>
              <div className="flex items-center gap-2 text-xs text-gray-500 mt-2">
                <span
                  className={`px-2 py-0.5 rounded text-[10px] ${
                    job.status === "PUBLISHED"
                      ? "bg-green-100 text-green-700"
                      : "bg-gray-100 text-gray-600"
                  }`}
                >
                  {job.status}
                </span>
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* RIGHT CONTENT: List Applications */}
      <div className="flex-1 flex flex-col min-w-0 bg-gray-50/50">
        <div className="bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center shadow-sm z-10">
          <div>
            <h1 className="text-xl font-bold text-gray-900">
              {jobs.find((j) => j.id === selectedJobId)?.title ||
                "Chi tiết công việc"}
            </h1>
            {selectedJobId && (
              <div className="flex gap-4 mt-1 text-sm">
                <button
                  onClick={() => setActiveTab("all")}
                  className={`transition-colors ${
                    activeTab === "all"
                      ? "text-blue-600 font-bold border-b-2 border-blue-600"
                      : "text-gray-500"
                  }`}
                >
                  Tất cả ({currentApplications.length})
                </button>
                <div className="w-px h-4 bg-gray-300 my-auto"></div>
                <button
                  onClick={() => setActiveTab("matching")}
                  className={`flex items-center gap-1 transition-colors ${
                    activeTab === "matching"
                      ? "text-purple-600 font-bold border-b-2 border-purple-600"
                      : "text-gray-500"
                  }`}
                >
                  <Sparkles className="w-3.5 h-3.5" /> AI Đề xuất (
                  {currentApplications.filter((a) => a.matchScore >= 50).length}
                  )
                </button>
              </div>
            )}
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-6">
          {loading ? (
            <div className="space-y-4">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="h-32 bg-white rounded-lg animate-pulse shadow-sm"
                />
              ))}
            </div>
          ) : !selectedJobId ? (
            <div className="h-full flex flex-col items-center justify-center text-gray-400">
              <Briefcase size={48} className="mb-4 opacity-20" />
              <p>Vui lòng chọn một công việc bên trái</p>
            </div>
          ) : displayApplications.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center text-gray-400">
              <Filter size={48} className="mb-4 opacity-20" />
              <p>Chưa có hồ sơ nào cho vị trí này</p>
            </div>
          ) : (
            <div className="space-y-4">
              {displayApplications.map((app) => (
                <div
                  key={app.id}
                  className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm hover:shadow-md transition-all group"
                >
                  <div className="flex items-start gap-5">
                    {/* Avatar */}
                    <div className="w-14 h-14 rounded-full bg-gradient-to-br from-blue-50 to-blue-100 flex items-center justify-center text-blue-700 font-bold text-xl shrink-0 border border-blue-200">
                      {app.studentName.charAt(0).toUpperCase()}
                    </div>

                    {/* Content */}
                    <div className="flex-1 min-w-0">
                      <div className="flex justify-between items-start">
                        <div>
                          <h3 className="text-lg font-bold text-gray-900 group-hover:text-blue-600 transition-colors">
                            {app.studentName}
                          </h3>
                          <div className="flex items-center gap-3 mt-2">
                            {/* Score Badge */}
                            <div
                              className={`flex items-center gap-1 px-3 py-1 rounded-full text-xs font-bold border ${getScoreColor(
                                app.matchScore,
                              )}`}
                            >
                              <Sparkles className="w-3 h-3" /> {app.matchScore}%
                            </div>
                            <span className="text-xs text-gray-400 flex items-center gap-1">
                              <Clock className="w-3 h-3" />{" "}
                              {new Date(app.appliedAt).toLocaleDateString(
                                "vi-VN",
                              )}
                            </span>

                            {/* Status Badge */}
                            <span
                              className={`px-2 py-0.5 rounded text-[10px] font-medium 
                                ${
                                  app.status === "PENDING"
                                    ? "bg-yellow-100 text-yellow-800"
                                    : app.status === "APPROVED"
                                      ? "bg-green-100 text-green-800"
                                      : "bg-red-100 text-red-800"
                                }`}
                            >
                              {app.status === "PENDING"
                                ? "Chờ duyệt"
                                : app.status === "APPROVED"
                                  ? "Đã nhận"
                                  : "Đã loại"}
                            </span>
                          </div>
                        </div>

                        {/* BUTTONS GROUP */}
                        <div className="flex gap-2">
                          <button
                            onClick={() => setAnalyzeApp(app)}
                            className="flex items-center gap-2 px-3 py-2 bg-purple-50 text-purple-700 rounded-lg text-sm font-semibold hover:bg-purple-100 border border-purple-200 transition"
                          >
                            <Sparkles size={16} /> AI Phân tích
                          </button>
                          <button
                            onClick={() => setViewCVApp(app)}
                            className="flex items-center gap-2 px-3 py-2 bg-white text-gray-700 rounded-lg text-sm font-semibold hover:bg-gray-50 border border-gray-300 transition shadow-sm"
                          >
                            <Eye size={16} /> Xem CV
                          </button>
                        </div>
                      </div>

                      {/* AI Brief Summary Inline */}
                      <div className="mt-3 bg-gray-50 rounded-lg p-3 text-sm border border-gray-100">
                        <p className="text-gray-700 italic line-clamp-1">
                          <Sparkles className="w-3 h-3 inline mr-1 text-purple-500" />
                          "{app.aiEvaluation || "Chưa có nhận xét"}"
                        </p>
                        {app.missingSkillsList && (
                          <div className="flex items-start gap-2 mt-1 pt-1 border-t border-gray-200">
                            <AlertCircle className="w-3 h-3 text-red-500 mt-0.5 shrink-0" />
                            <p className="text-xs">
                              <span className="font-semibold text-gray-500 uppercase mr-1">
                                Kỹ năng thiếu:
                              </span>
                              <span className="text-gray-800 font-medium">
                                {app.missingSkillsList}
                              </span>
                            </p>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
