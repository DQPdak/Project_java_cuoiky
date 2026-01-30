"use client";

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  ArrowLeft, Mail, Phone, Calendar, Download, 
  CheckCircle, XCircle, FileText, Briefcase, User 
} from 'lucide-react';
import { recruitmentService } from "@/services/recruitmentService";
import { ApplicationStatus, AIAnalysisDetail } from "@/types/recruitment";

// Helper convert list string từ backend (nếu BE trả về string dạng "A, B, C")
const parseSkillString = (str?: string | string[]) => {
    if (!str) return [];
    if (Array.isArray(str)) return str;
    return str.split(',').map(s => s.trim()).filter(s => s.length > 0);
};

export default function ApplicationDetailPage() {
  const params = useParams();
  const router = useRouter();
  // Ép kiểu ID về number vì params.id là string
  const id = Number(params.id);

  const [appDetail, setAppDetail] = useState<AIAnalysisDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    if (id) fetchDetail();
  }, [id]);

  const fetchDetail = async () => {
    try {
      // Gọi đúng hàm đang có trong service của bạn
      const data = await recruitmentService.getApplicationAnalysis(id);
      setAppDetail(data);
    } catch (error) {
      console.error("Lỗi tải chi tiết:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus: ApplicationStatus) => {
    if (!confirm(`Xác nhận chuyển trạng thái sang: ${newStatus}?`)) return;
    setUpdating(true);
    try {
      // Gọi đúng hàm updateStatus trong service của bạn
      await recruitmentService.updateApplicationStatus(id, newStatus);
      // Cập nhật lại UI
      setAppDetail(prev => prev ? { ...prev, status: newStatus } : null);
      alert("Cập nhật thành công!");
    } catch (error) {
      console.error("Lỗi cập nhật:", error);
      alert("Cập nhật thất bại.");
    } finally {
      setUpdating(false);
    }
  };

  if (loading) return <div className="p-8 text-center">Đang tải hồ sơ...</div>;
  if (!appDetail) return <div className="p-8 text-center text-red-500">Không tìm thấy dữ liệu.</div>;

  // Xử lý dữ liệu hiển thị
  const matchedSkills = parseSkillString(appDetail.matchedSkillsList);
  const missingSkills = parseSkillString(appDetail.missingSkillsList);
  const status = appDetail.status || ApplicationStatus.PENDING;

  return (
    <div className="min-h-screen bg-gray-50 pb-10">
      {/* Header */}
      <div className="bg-white border-b px-6 py-4 shadow-sm">
        <Link href="/dashboard-recruiter" className="text-gray-500 hover:text-blue-600 flex items-center mb-4 text-sm">
          <ArrowLeft size={16} className="mr-1" /> Quay lại Dashboard
        </Link>

        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 bg-blue-100 rounded-full flex items-center justify-center text-blue-700 font-bold text-xl">
              {(appDetail.studentName || 'U').charAt(0).toUpperCase()}
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">{appDetail.studentName}</h1>
              <p className="text-gray-500 flex items-center text-sm">
                 <Briefcase size={14} className="mr-1"/> Ứng tuyển: <span className="text-blue-600 font-medium ml-1">{appDetail.jobTitle}</span>
              </p>
            </div>
          </div>

          <div className="flex gap-2">
            {/* Các nút thao tác dựa trên trạng thái hiện tại */}
            {status === ApplicationStatus.PENDING && (
               <>
                 <button disabled={updating} onClick={() => handleStatusChange(ApplicationStatus.REJECTED)} className="px-4 py-2 border border-red-200 text-red-600 rounded-lg hover:bg-red-50 text-sm font-medium flex items-center">
                    <XCircle size={16} className="mr-2"/> Từ chối
                 </button>
                 <button disabled={updating} onClick={() => handleStatusChange(ApplicationStatus.SCREENING)} className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm font-medium flex items-center">
                    <CheckCircle size={16} className="mr-2"/> Duyệt hồ sơ
                 </button>
               </>
            )}
            {status === ApplicationStatus.SCREENING && (
                 <button disabled={updating} onClick={() => handleStatusChange(ApplicationStatus.INTERVIEW)} className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 text-sm font-medium flex items-center">
                    <Calendar size={16} className="mr-2"/> Mời phỏng vấn
                 </button>
            )}
             {/* Hiển thị trạng thái hiện tại nếu đã xử lý */}
             {status === ApplicationStatus.REJECTED && <span className="px-4 py-2 bg-red-100 text-red-800 rounded-lg font-medium">Đã từ chối</span>}
             {status === ApplicationStatus.HIRED && <span className="px-4 py-2 bg-green-100 text-green-800 rounded-lg font-medium">Đã tuyển dụng</span>}
          </div>
        </div>
      </div>

      <div className="container mx-auto px-6 mt-6 grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Cột trái: Thông tin chính & CV */}
        <div className="lg:col-span-2 space-y-6">
            {/* AI Evaluation Box */}
            <div className="bg-gradient-to-r from-indigo-50 to-blue-50 p-6 rounded-xl border border-indigo-100">
                <div className="flex justify-between items-center mb-3">
                    <h3 className="font-bold text-indigo-900 flex items-center gap-2">✨ AI Đánh giá sơ bộ</h3>
                    <span className="text-2xl font-bold text-indigo-600">{appDetail.matchScore || 0}% Match</span>
                </div>
                <p className="text-gray-700 text-sm mb-4">{appDetail.aiEvaluation || appDetail.evaluation || "Chưa có đánh giá chi tiết."}</p>
                
                <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                        <p className="font-semibold text-green-700 mb-2">✅ Kỹ năng phù hợp:</p>
                        <div className="flex flex-wrap gap-2">
                            {matchedSkills.length > 0 ? matchedSkills.map((s, i) => (
                                <span key={i} className="bg-green-100 text-green-800 px-2 py-1 rounded text-xs">{s}</span>
                            )) : <span className="text-gray-500 italic">Không tìm thấy</span>}
                        </div>
                    </div>
                    <div>
                        <p className="font-semibold text-red-700 mb-2">⚠️ Kỹ năng còn thiếu:</p>
                        <div className="flex flex-wrap gap-2">
                             {missingSkills.length > 0 ? missingSkills.map((s, i) => (
                                <span key={i} className="bg-red-100 text-red-800 px-2 py-1 rounded text-xs">{s}</span>
                            )) : <span className="text-gray-500 italic">Đầy đủ</span>}
                        </div>
                    </div>
                </div>
            </div>

            {/* CV Viewer */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden h-[800px] flex flex-col">
                <div className="bg-gray-50 px-4 py-3 border-b border-gray-200 flex justify-between items-center">
                    <h3 className="font-semibold text-gray-700 flex items-center"><FileText size={16} className="mr-2"/> CV Preview</h3>
                    {appDetail.cvUrl && (
                        <a href={appDetail.cvUrl} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline text-sm flex items-center">
                            <Download size={14} className="mr-1"/> Tải xuống
                        </a>
                    )}
                </div>
                <div className="flex-1 bg-gray-100 flex items-center justify-center">
                    {appDetail.cvUrl ? (
                        <iframe src={appDetail.cvUrl} className="w-full h-full" title="CV"></iframe>
                    ) : (
                        <div className="text-gray-400">Không có bản xem trước CV</div>
                    )}
                </div>
            </div>
        </div>

        {/* Cột phải: Thông tin liên hệ */}
        <div className="space-y-6">
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                <h3 className="font-bold text-gray-800 mb-4 pb-2 border-b">Thông tin liên hệ</h3>
                <div className="space-y-4">
                    <div className="flex items-start">
                        <Mail className="text-gray-400 mt-0.5 mr-3" size={18}/>
                        <div>
                            <p className="text-xs text-gray-500 uppercase font-medium">Email</p>
                            <a href={`mailto:${appDetail.email}`} className="text-blue-600 hover:underline text-sm break-all">
                                {appDetail.email || "---"}
                            </a>
                        </div>
                    </div>
                    <div className="flex items-start">
                        <Phone className="text-gray-400 mt-0.5 mr-3" size={18}/>
                        <div>
                            <p className="text-xs text-gray-500 uppercase font-medium">Số điện thoại</p>
                            <p className="text-gray-900 text-sm">{appDetail.phone || "---"}</p>
                        </div>
                    </div>
                    <div className="flex items-start">
                        <Calendar className="text-gray-400 mt-0.5 mr-3" size={18}/>
                        <div>
                            <p className="text-xs text-gray-500 uppercase font-medium">Ngày ứng tuyển</p>
                            <p className="text-gray-900 text-sm">
                                {appDetail.appliedAt ? new Date(appDetail.appliedAt).toLocaleDateString('vi-VN') : '---'}
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
                <h3 className="font-bold text-gray-800 mb-4 pb-2 border-b">Ghi chú tuyển dụng</h3>
                <div className="bg-yellow-50 p-3 rounded-lg border border-yellow-100 text-sm text-gray-700 min-h-[100px]">
                    {appDetail.recruiterNote || "Chưa có ghi chú nào."}
                </div>
            </div>
        </div>
      </div>
    </div>
  );
}