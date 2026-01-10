"use client";

import { useState, ChangeEvent } from "react";
import { uploadCV } from "@/services/candidateService";
import { CandidateProfile } from "@/types/candidate";
import toast, { Toaster } from "react-hot-toast";

export default function CVAnalysisPage() {
  const [profile, setProfile] = useState<CandidateProfile | null>(null);
  const [uploading, setUploading] = useState(false);

  const handleFileUpload = async (e: ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;

    const file = e.target.files[0];
    if (!file.name.match(/\.(pdf|docx)$/)) {
        toast.error("Chỉ chấp nhận file PDF hoặc DOCX");
        return;
    }

    setUploading(true);
    const toastId = toast.loading("AI đang phân tích CV của bạn...");

    try {
      const newProfile = await uploadCV(file);
      setProfile(newProfile);
      toast.success("Phân tích thành công!", { id: toastId });
    } catch (error) {
      console.error(error);
      toast.error("Lỗi khi tải lên CV", { id: toastId });
    } finally {
      setUploading(false);
      e.target.value = "";
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-8">
      <Toaster />
      
      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Phân tích CV với AI 🤖</h1>
        <p className="text-gray-500 mt-2">
          Tải lên CV của bạn để hệ thống tự động trích xuất kỹ năng và đánh giá mức độ phù hợp.
        </p>
      </div>

      {/* Khu vực Upload */}
      <div className="bg-blue-50 border-2 border-dashed border-blue-200 rounded-xl p-10 text-center hover:bg-blue-100 transition-colors">
        <div className="space-y-4">
            <div className="text-6xl">📄</div>
            <h3 className="text-lg font-medium text-blue-900">
                Kéo thả hoặc chọn file CV của bạn
            </h3>
            <p className="text-sm text-blue-600">Hỗ trợ định dạng PDF, DOCX (Tối đa 5MB)</p>
            
            <label className={`
                cursor-pointer inline-flex items-center px-6 py-3 rounded-full 
                font-medium text-white shadow-lg transition-all
                ${uploading ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'}
            `}>
                {uploading ? "Đang xử lý..." : "Chọn File từ máy tính"}
                <input 
                    type="file" 
                    className="hidden" 
                    accept=".pdf,.docx"
                    onChange={handleFileUpload}
                    disabled={uploading}
                />
            </label>
        </div>
      </div>

      {/* Kết quả phân tích */}
      {profile && (
        <div className="animate-fade-in-up space-y-6">
            <h2 className="text-xl font-bold text-gray-800 border-l-4 border-blue-600 pl-3">
                Kết quả phân tích
            </h2>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Thông tin cá nhân */}
                <div className="bg-white p-6 rounded-lg shadow border border-gray-100">
                    <h3 className="font-semibold text-gray-700 mb-3">Thông tin liên hệ</h3>
                    <ul className="space-y-2 text-sm">
                        <li className="flex justify-between">
                            <span className="text-gray-500">Số điện thoại:</span>
                            <span className="font-medium">{profile.phoneNumber || "N/A"}</span>
                        </li>
                        <li className="flex justify-between">
                            <span className="text-gray-500">LinkedIn:</span>
                            <a href="#" className="text-blue-600 hover:underline">Xem hồ sơ</a>
                        </li>
                    </ul>
                </div>

                {/* Kỹ năng */}
                <div className="bg-white p-6 rounded-lg shadow border border-gray-100">
                    <h3 className="font-semibold text-gray-700 mb-3">Kỹ năng phát hiện</h3>
                    <div className="flex flex-wrap gap-2">
                        {profile.skills?.map((skill, i) => (
                            <span key={i} className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-xs font-bold">
                                {skill}
                            </span>
                        ))}
                    </div>
                </div>

                {/* Kinh nghiệm */}
                <div className="bg-white p-6 rounded-lg shadow border border-gray-100 md:col-span-2">
                    <h3 className="font-semibold text-gray-700 mb-3">Kinh nghiệm làm việc</h3>
                    {profile.experiences?.map((exp, i) => (
                        <div key={i} className="mb-4 last:mb-0 p-3 bg-gray-50 rounded">
                             <div className="font-medium text-gray-900">
                                 Tổng số năm kinh nghiệm: <span className="text-blue-600">{exp.totalYears} năm</span>
                             </div>
                             <div className="text-sm text-gray-600 mt-1">
                                 Đánh giá cấp độ: {exp.level}
                             </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
      )}
    </div>
  );
}