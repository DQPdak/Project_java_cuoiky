import {
  CheckCircle,
  XCircle,
  Award,
  Sparkles,
  Lightbulb,
  BookOpen,
  TrendingUp,
  Zap,
} from "lucide-react";

// Định nghĩa Interface khớp chính xác với DTO MatchResult 5 cột từ Backend
interface AnalysisData {
  matchPercentage: number;
  totalRequiredSkills: number;
  evaluation: string;
  learningPath: string;
  careerAdvice: string;

  // 5 Nhóm kỹ năng
  matchedSkillsList: string[];
  missingSkillsList: string[];
  otherHardSkillsList: string[];
  otherSoftSkillsList: string[];
  recommendedSkillsList: string[];

  // Đếm số lượng
  matchedSkillsCount: number;
  missingSkillsCount: number;
  otherHardSkillsCount: number;
  otherSoftSkillsCount: number;
  recommendedSkillsCount: number;
}

interface CVAnalysisResultProps {
  result: AnalysisData | null;
  isRecruiterView?: boolean;
}

export default function CVAnalysisResult({
  result,
  isRecruiterView = false,
}: CVAnalysisResultProps) {
  console.log("🔍 Dữ liệu CVAnalysisResult nhận được từ Backend:", result);
  if (!result) {
    return (
      <div className="flex flex-col items-center justify-center p-12 bg-gray-50 rounded-3xl border-2 border-dashed border-gray-200">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 mb-4"></div>
        <p className="text-gray-500 font-medium">
          Hệ thống AI đang phân tích dữ liệu...
        </p>
      </div>
    );
  }

  // Xác định màu sắc chủ đạo dựa trên điểm số
  const scoreColorClass =
    result.matchPercentage >= 80
      ? "text-green-600 border-green-500 bg-green-50"
      : result.matchPercentage >= 50
        ? "text-yellow-600 border-yellow-500 bg-yellow-50"
        : "text-red-600 border-red-500 bg-red-50";
  // Component phụ render thẻ kỹ năng để tái sử dụng
  const SkillCard = ({
    title,
    count,
    skills,
    icon: Icon,
    colorClass,
    borderClass,
    bgClass,
    description,
  }: any) => (
    <div
      className={`flex flex-col h-full p-5 rounded-2xl border ${borderClass} ${bgClass} shadow-sm transition-all hover:shadow-md`}
    >
      <div className="flex items-start justify-between mb-4 pb-3 border-b border-black/5">
        <div className="flex items-center gap-3">
          <div className={`p-2 rounded-xl ${colorClass} text-white`}>
            <Icon size={20} />
          </div>
          <div>
            <h4 className="font-bold text-gray-800 text-sm uppercase tracking-tight">
              {title}
            </h4>
            <p className="text-[10px] text-gray-500 font-medium mt-0.5">
              {description}
            </p>
          </div>
        </div>
        <span
          className={`text-xl font-black ${colorClass.replace("bg-", "text-")}`}
        >
          {count}
        </span>
      </div>
      <div className="flex flex-wrap gap-2">
        {skills && skills.length > 0 ? (
          skills.map((skill: string, i: number) => (
            <span
              key={i}
              className={`px-3 py-1.5 bg-white border ${borderClass} ${colorClass.replace(
                "bg-",
                "text-",
              )} rounded-lg text-xs font-bold shadow-sm`}
            >
              {skill}
            </span>
          ))
        ) : (
          <span className="text-gray-400 text-xs italic">
            Không tìm thấy dữ liệu
          </span>
        )}
      </div>
    </div>
  );

  return (
    <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700 pb-12">
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

      {/* 2. GRID KỸ NĂNG: PHÂN LOẠI 5 NHÓM */}
      <div className="space-y-6">
        {/* HÀNG 1: TRỌNG TÂM (ĐÁP ỨNG & THIẾU) */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <SkillCard
            title="Kỹ năng đáp ứng"
            description="Đúng yêu cầu JD (Cả chuyên môn & mềm)"
            count={result.matchedSkillsCount}
            skills={result.matchedSkillsList}
            icon={CheckCircle}
            colorClass="bg-green-600"
            borderClass="border-green-100"
            bgClass="bg-green-50/30"
          />
          <SkillCard
            title="Kỹ năng còn thiếu"
            description="Yêu cầu trong JD nhưng CV chưa có"
            count={result.missingSkillsCount}
            skills={result.missingSkillsList}
            icon={XCircle}
            colorClass="bg-red-600"
            borderClass="border-red-100"
            bgClass="bg-red-50/30"
          />
        </div>

        {/* HÀNG 2: BỔ TRỢ (CHUYÊN MÔN & MỀM KHÁC) */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <SkillCard
            title="Chuyên môn khác"
            description="Tech-stack bạn có nhưng Job không yêu cầu"
            count={result.otherHardSkillsCount}
            skills={result.otherHardSkillsList}
            icon={Award}
            colorClass="bg-blue-600"
            borderClass="border-blue-100"
            bgClass="bg-blue-50/30"
          />
          <SkillCard
            title="Kỹ năng mềm khác"
            description="Lợi thế mềm bổ trợ ngoài yêu cầu"
            count={result.otherSoftSkillsCount}
            skills={result.otherSoftSkillsList}
            icon={Sparkles}
            colorClass="bg-purple-600"
            borderClass="border-purple-100"
            bgClass="bg-purple-50/30"
          />
        </div>

        {/* HÀNG 3: GỢI Ý NÂNG CAO (AI RECOMMENDED) */}
        {isRecruiterView && (
          <div className="bg-gradient-to-br from-amber-600 to-orange-700 p-8 rounded-[2rem] text-white shadow-lg relative overflow-hidden group">
            <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:scale-110 transition-transform duration-500">
              <Lightbulb size={120} />
            </div>
            <div className="relative z-10 space-y-5">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-white/20 rounded-xl backdrop-blur-md">
                  <Zap size={24} className="text-amber-300 fill-amber-300" />
                </div>
                <div>
                  <h4 className="text-xl font-black uppercase tracking-tight">
                    Kỹ năng gợi ý từ chuyên gia AI
                  </h4>
                  <p className="text-amber-100 text-xs font-medium">
                    Cần thiết cho công việc thực tế (ngoài JD & CV)
                  </p>
                </div>
              </div>

              <div className="flex flex-wrap gap-3">
                {result.recommendedSkillsList?.length > 0 ? (
                  result.recommendedSkillsList.map((skill, i) => (
                    <span
                      key={i}
                      className="px-4 py-2 bg-white/10 backdrop-blur-md border border-white/20 rounded-full text-sm font-bold flex items-center gap-2 hover:bg-white/20 transition-colors"
                    >
                      <TrendingUp size={14} /> {skill}
                    </span>
                  ))
                ) : (
                  <p className="text-sm opacity-80 italic">
                    Không có gợi ý bổ sung tại thời điểm này.
                  </p>
                )}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 3. LỘ TRÌNH HÀNH ĐỘNG (ACTION PLAN) */}
      {isRecruiterView && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Lộ trình học tập */}
          <div className="bg-white p-7 rounded-[2rem] border border-gray-100 shadow-sm space-y-5">
            <div className="flex items-center gap-3 text-blue-700">
              <BookOpen size={24} />
              <h4 className="text-lg font-bold">Lộ trình học tập chi tiết</h4>
            </div>
            <div className="prose prose-sm max-w-none text-gray-600 leading-relaxed whitespace-pre-line bg-gray-50/50 p-6 rounded-2xl border border-gray-100">
              {result.learningPath}
            </div>
          </div>

          {/* Lời khuyên sự nghiệp */}
          <div className="bg-white p-7 rounded-[2rem] border border-gray-100 shadow-sm space-y-5">
            <div className="flex items-center gap-3 text-green-700">
              <TrendingUp size={24} />
              <h4 className="text-lg font-bold">Lời khuyên phát triển</h4>
            </div>
            <div className="prose prose-sm max-w-none text-gray-600 leading-relaxed whitespace-pre-line bg-gray-50/50 p-6 rounded-2xl border border-gray-100">
              {result.careerAdvice}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
