import React from "react";
import {
  CheckCircle,
  XCircle,
  AlertCircle,
  BookOpen,
  TrendingUp,
  Award,
  Star,
} from "lucide-react";

// 1. Định nghĩa Interface khớp chính xác với hình ảnh bạn gửi
interface AnalysisData {
  matchPercentage: number;
  evaluation: string;
  careerAdvice: string;
  learningPath: string;

  matchedSkillsList: string[];
  missingSkillsList: string[];
  extraSkillsList: string[]; // Skill thừa/bổ sung

  totalRequiredSkills: number;
  matchedSkillsCount: number;
  missingSkillsCount: number;
  extraSkillsCount: number;
}

interface CVAnalysisResultProps {
  result: AnalysisData | null;
}

export default function CVAnalysisResult({ result }: CVAnalysisResultProps) {
  // Nếu chưa có dữ liệu thì hiện loading hoặc trống
  if (!result) {
    return (
      <div className="text-center p-12 bg-gray-50 rounded-xl border border-dashed border-gray-300">
        <p className="text-gray-400 italic">Đang chờ kết quả phân tích...</p>
      </div>
    );
  }

  // Xác định màu sắc dựa trên điểm số
  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-600 border-green-500";
    if (score >= 50) return "text-yellow-600 border-yellow-500";
    return "text-red-600 border-red-500";
  };

  const scoreColorClass = getScoreColor(result.matchPercentage);

  return (
    <div className="space-y-8 animate-fade-in-up">
      {/* --- PHẦN 1: ĐIỂM SỐ & ĐÁNH GIÁ TỔNG QUAN --- */}
      <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-8 items-center">
        {/* Vòng tròn điểm số */}
        <div className="relative w-40 h-40 flex-shrink-0 flex items-center justify-center">
          <svg className="w-full h-full transform -rotate-90">
            <circle
              cx="80"
              cy="80"
              r="70"
              stroke="#f3f4f6"
              strokeWidth="12"
              fill="transparent"
            />
            <circle
              cx="80"
              cy="80"
              r="70"
              stroke="currentColor"
              strokeWidth="12"
              fill="transparent"
              strokeDasharray="439.8"
              strokeDashoffset={439.8 - (439.8 * result.matchPercentage) / 100}
              strokeLinecap="round"
              className={`transition-all duration-1000 ease-out ${
                scoreColorClass.split(" ")[0]
              }`}
            />
          </svg>
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span
              className={`text-4xl font-bold ${scoreColorClass.split(" ")[0]}`}
            >
              {result.matchPercentage}%
            </span>
            <span className="text-sm text-gray-500 font-medium mt-1">
              Phù hợp
            </span>
          </div>
        </div>

        {/* Text đánh giá */}
        <div className="flex-1 space-y-3">
          <div className="flex items-center gap-2">
            <Award className="text-blue-600" size={24} />
            <h3 className="text-xl font-bold text-gray-800">Đánh giá từ AI</h3>
          </div>
          <div className="bg-blue-50 p-4 rounded-xl border border-blue-100 text-gray-700 leading-relaxed text-sm md:text-base">
            {result.evaluation}
          </div>
        </div>
      </div>

      {/* --- PHẦN 2: CHI TIẾT KỸ NĂNG (3 CỘT) --- */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Cột 1: Kỹ năng ĐẠT (Màu xanh) */}
        <div className="bg-white p-5 rounded-xl border border-green-100 shadow-sm flex flex-col h-full">
          <div className="flex items-center gap-2 mb-4 text-green-700 font-bold border-b border-green-100 pb-3">
            <CheckCircle size={20} />
            <span>Đã đáp ứng ({result.matchedSkillsCount})</span>
          </div>
          <div className="flex flex-wrap gap-2 content-start">
            {result.matchedSkillsList?.length > 0 ? (
              result.matchedSkillsList.map((skill, idx) => (
                <span
                  key={idx}
                  className="px-3 py-1 bg-green-50 text-green-700 rounded-lg text-sm font-semibold border border-green-200 shadow-sm"
                >
                  {skill}
                </span>
              ))
            ) : (
              <span className="text-gray-400 text-sm italic">
                Chưa tìm thấy kỹ năng phù hợp.
              </span>
            )}
          </div>
        </div>

        {/* Cột 2: Kỹ năng THIẾU (Màu đỏ) */}
        <div className="bg-white p-5 rounded-xl border border-red-100 shadow-sm flex flex-col h-full">
          <div className="flex items-center gap-2 mb-4 text-red-700 font-bold border-b border-red-100 pb-3">
            <XCircle size={20} />
            <span>Còn thiếu ({result.missingSkillsCount})</span>
          </div>
          <div className="flex flex-wrap gap-2 content-start">
            {result.missingSkillsList?.length > 0 ? (
              result.missingSkillsList.map((skill, idx) => (
                <span
                  key={idx}
                  className="px-3 py-1 bg-red-50 text-red-700 rounded-lg text-sm font-semibold border border-red-200 shadow-sm"
                >
                  {skill}
                </span>
              ))
            ) : (
              <div className="text-green-600 text-sm flex items-center gap-1 bg-green-50 px-3 py-2 rounded-lg w-full">
                <Star size={16} fill="currentColor" /> Tuyệt vời! Bạn đủ 100% kỹ
                năng.
              </div>
            )}
          </div>
        </div>

        {/* Cột 3: Kỹ năng THỪA/BỔ TRỢ (Màu xám/tím) */}
        <div className="bg-white p-5 rounded-xl border border-purple-100 shadow-sm flex flex-col h-full">
          <div className="flex items-center gap-2 mb-4 text-purple-700 font-bold border-b border-purple-100 pb-3">
            <AlertCircle size={20} />
            <span>Kỹ năng khác ({result.extraSkillsCount})</span>
          </div>
          <div className="flex flex-wrap gap-2 content-start">
            {result.extraSkillsList?.length > 0 ? (
              result.extraSkillsList.map((skill, idx) => (
                <span
                  key={idx}
                  className="px-3 py-1 bg-purple-50 text-purple-700 rounded-lg text-sm font-medium border border-purple-200"
                >
                  {skill}
                </span>
              ))
            ) : (
              <span className="text-gray-400 text-sm italic">
                Không có kỹ năng bổ sung.
              </span>
            )}
          </div>
        </div>
      </div>

      {/* --- PHẦN 3: LỘ TRÌNH & LỜI KHUYÊN --- */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Lộ trình học tập */}
        <div className="bg-gradient-to-br from-indigo-50 to-white p-6 rounded-xl border border-indigo-100 shadow-sm">
          <h3 className="font-bold text-indigo-900 mb-4 flex items-center gap-2 text-lg">
            <BookOpen className="text-indigo-600" size={24} />
            Lộ trình cải thiện
          </h3>
          <div className="text-gray-700 text-sm leading-relaxed whitespace-pre-line">
            {result.learningPath || "Chưa có lộ trình cụ thể."}
          </div>
        </div>

        {/* Lời khuyên sự nghiệp */}
        <div className="bg-gradient-to-br from-orange-50 to-white p-6 rounded-xl border border-orange-100 shadow-sm">
          <h3 className="font-bold text-orange-900 mb-4 flex items-center gap-2 text-lg">
            <TrendingUp className="text-orange-600" size={24} />
            Lời khuyên sự nghiệp
          </h3>
          <div className="text-gray-700 text-sm leading-relaxed whitespace-pre-line">
            {result.careerAdvice || "Chưa có lời khuyên cụ thể."}
          </div>
        </div>
      </div>
    </div>
  );
}
