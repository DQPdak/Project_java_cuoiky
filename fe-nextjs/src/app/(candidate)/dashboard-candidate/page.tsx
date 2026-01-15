"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  getRecommendedJobs,
  applyJob,
  getBatchScores,
} from "@/services/candidateService";
import toast from "react-hot-toast";
import {
  Search,
  MapPin,
  DollarSign,
  TrendingUp,
  Star,
  ArrowRight,
  Building,
  Sparkles,
} from "lucide-react";

export default function CandidateDashboard() {
  const [jobs, setJobs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [applyingId, setApplyingId] = useState<number | null>(null);

  const router = useRouter();
  const [searchTerm, setSearchTerm] = useState("");

  const handleSearch = () => {
    if (searchTerm.trim()) {
      router.push(`/jobs?keyword=${encodeURIComponent(searchTerm)}`);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") handleSearch();
  };

  const handleApply = async (jobId: number) => {
    setApplyingId(jobId);
    try {
      await applyJob(jobId);
      toast.success("Ứng tuyển thành công! 🎉");
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Lỗi khi ứng tuyển");
    } finally {
      setApplyingId(null);
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);

        // BƯỚC 1: Lấy danh sách Job
        const jobsData = await getRecommendedJobs();

        if (jobsData && jobsData.length > 0) {
          const jobIds = jobsData.map((job: any) => job.id);

          try {
            // BƯỚC 2: Gọi API tính điểm
            // API trả về Map<Long, FastMatchResult>
            const scoresMap = await getBatchScores(jobIds);

            // BƯỚC 3: Ghép dữ liệu (SỬA LỖI TẠI ĐÂY)
            const mergedJobs = jobsData.map((job: any) => {
              // Lấy object kết quả, nếu null thì tạo mặc định
              const result = scoresMap[job.id] || {
                matchScore: 0,
                matchedSkills: [],
                missingSkills: [],
              };

              return {
                ...job,
                // Bóc tách từng trường dữ liệu ra
                matchScore: result.matchScore, // Lấy số (Integer) -> Để hiển thị %
                skillsFound: result.matchedSkills, // Lấy list (Array) -> Để hiển thị màu xanh
                skillsMissing: result.missingSkills, // Lấy list (Array) -> Để hiển thị màu đỏ (nếu cần)
              };
            });

            // Sắp xếp
            mergedJobs.sort((a: any, b: any) => b.matchScore - a.matchScore);

            setJobs(mergedJobs);
          } catch (err) {
            console.error("Lỗi tính điểm batch:", err);
            // Fallback nếu lỗi API tính điểm
            setJobs(
              jobsData.map((j: any) => ({
                ...j,
                matchScore: 0,
                skillsFound: [],
              }))
            );
          }
        } else {
          setJobs([]);
        }
      } catch (error) {
        console.error("Lỗi tải job:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  return (
    <div className="space-y-8 pb-10">
      {/* 1. SECTION: WELCOME & SEARCH */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-700 rounded-2xl p-8 text-white shadow-xl">
        <h1 className="text-3xl font-bold mb-2">Xin chào, Ứng viên! 👋</h1>
        <p className="opacity-90 mb-6 text-blue-100">
          Hệ thống AI đã tìm thấy những cơ hội phù hợp nhất với hồ sơ của bạn
          hôm nay.
        </p>

        <div className="bg-white/10 backdrop-blur-md p-1.5 rounded-xl flex gap-2 max-w-2xl shadow-inner">
          <div className="flex-1 flex items-center bg-white rounded-lg px-4 py-2.5">
            <Search className="text-gray-400 mr-3" size={20} />
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Tìm kiếm công việc, kỹ năng, công ty..."
              className="w-full bg-transparent outline-none text-gray-800 placeholder-gray-500"
            />
          </div>
          <button
            onClick={handleSearch}
            className="bg-orange-500 hover:bg-orange-600 text-white px-8 py-2.5 rounded-lg font-semibold shadow-lg transition-transform active:scale-95"
          >
            Tìm kiếm
          </button>
        </div>
      </div>

      {/* 2. SECTION: WIDGETS */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Widget 1: Hồ sơ năng lực */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col justify-between hover:shadow-md transition">
          <div className="flex items-center justify-between mb-4">
            <div className="relative w-16 h-16">
              <svg className="w-full h-full transform -rotate-90">
                <circle
                  cx="32"
                  cy="32"
                  r="28"
                  stroke="currentColor"
                  strokeWidth="4"
                  fill="transparent"
                  className="text-gray-100"
                />
                <circle
                  cx="32"
                  cy="32"
                  r="28"
                  stroke="currentColor"
                  strokeWidth="4"
                  fill="transparent"
                  className="text-blue-600"
                  strokeDasharray="175.9"
                  strokeDashoffset="35"
                  strokeLinecap="round"
                />
              </svg>
              <span className="absolute inset-0 flex items-center justify-center font-bold text-lg text-blue-700">
                80%
              </span>
            </div>
            <div className="text-right">
              <p className="font-bold text-gray-800 text-lg">Hồ sơ tốt</p>
              <p className="text-xs text-gray-500">Cập nhật thêm kỹ năng mềm</p>
            </div>
          </div>
          <Link href="/cv-analysis">
            <button className="w-full bg-gray-50 text-blue-600 py-2.5 rounded-lg text-sm font-semibold hover:bg-blue-50 border border-blue-100 transition">
              Cập nhật CV ngay
            </button>
          </Link>
        </div>

        {/* Widget 2: AI Career Coach */}
        <div className="md:col-span-2 bg-gradient-to-br from-purple-600 to-indigo-600 p-6 rounded-xl shadow-md text-white flex flex-col justify-between relative overflow-hidden">
          <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-2xl -mr-10 -mt-10"></div>
          <div className="relative z-10">
            <div className="flex items-center gap-2 mb-2">
              <TrendingUp className="text-yellow-300" />
              <h3 className="font-bold text-xl">AI Career Coach</h3>
            </div>
            <p className="text-purple-100 mb-4 max-w-lg">
              Bạn muốn biết lộ trình thăng tiến cho vị trí Tech Lead? AI có thể
              phân tích xu hướng thị trường và đưa ra lời khuyên.
            </p>
          </div>
          <button className="w-fit bg-white text-purple-700 py-2 px-6 rounded-lg text-sm font-bold shadow-sm hover:bg-gray-50 transition relative z-10">
            Chat với AI Coach
          </button>
        </div>
      </div>

      {/* 3. SECTION: JOB LIST */}
      <div>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <Star className="text-yellow-500 fill-yellow-500" size={24} />
            Việc làm dành riêng cho bạn
          </h2>
          <Link
            href="#"
            className="text-sm text-blue-600 hover:underline font-medium flex items-center"
          >
            Xem tất cả <ArrowRight size={16} className="ml-1" />
          </Link>
        </div>

        {loading && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((i) => (
              <div
                key={i}
                className="h-48 bg-gray-100 rounded-xl animate-pulse"
              ></div>
            ))}
          </div>
        )}

        {!loading && jobs.length === 0 && (
          <div className="text-center py-12 bg-white rounded-xl border border-gray-100">
            <div className="text-gray-300 text-5xl mb-3">📭</div>
            <p className="text-gray-500">Chưa tìm thấy công việc phù hợp.</p>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
          {jobs.map((job: any) => (
            <div
              key={job.id}
              className="group bg-white p-5 rounded-xl border border-gray-100 shadow-sm hover:shadow-lg hover:border-blue-200 transition-all duration-300 relative overflow-hidden"
            >
              {/* Badge Match Score */}
              <div className="absolute top-0 right-0 bg-blue-50 px-3 py-1.5 rounded-bl-xl border-b border-l border-blue-100">
                <span className="text-sm font-bold text-blue-700">
                  {job.matchScore}% Phù hợp
                </span>
              </div>

              <div className="flex items-start gap-4">
                <div className="w-12 h-12 bg-gray-50 rounded-lg flex items-center justify-center border border-gray-100 group-hover:bg-white transition">
                  <Building className="text-gray-400" size={24} />
                </div>

                <div className="flex-1">
                  <h3 className="font-bold text-lg text-gray-800 group-hover:text-blue-600 transition-colors line-clamp-1">
                    {job.title}
                  </h3>
                  <p className="text-sm text-gray-500 font-medium mb-3">
                    {job.company}
                  </p>

                  <div className="flex flex-wrap gap-3 text-sm text-gray-600 mb-4">
                    <span className="flex items-center bg-gray-50 px-2 py-1 rounded">
                      <MapPin size={14} className="mr-1 text-gray-400" />{" "}
                      {job.location}
                    </span>
                    <span className="flex items-center bg-green-50 text-green-700 px-2 py-1 rounded border border-green-100 font-medium">
                      <DollarSign size={14} className="mr-1" /> {job.salary}
                    </span>
                  </div>

                  {/* Kỹ năng phù hợp (XANH) - Lấy từ skillsFound (Backend matchedSkills) */}
                  {job.skillsFound && job.skillsFound.length > 0 && (
                    <div className="mb-3">
                      <p className="text-[10px] text-gray-400 mb-1.5 uppercase font-bold tracking-wider">
                        Kỹ năng phù hợp
                      </p>
                      <div className="flex flex-wrap gap-1.5">
                        {job.skillsFound
                          .slice(0, 4)
                          .map((skill: string, idx: number) => (
                            <span
                              key={idx}
                              className="text-xs px-2 py-0.5 bg-blue-50 text-blue-700 rounded border border-blue-100"
                            >
                              {skill}
                            </span>
                          ))}
                        {job.skillsFound.length > 4 && (
                          <span className="text-xs px-2 py-0.5 bg-gray-50 text-gray-500 rounded border border-gray-200">
                            +{job.skillsFound.length - 4}
                          </span>
                        )}
                      </div>
                    </div>
                  )}

                  {/* Kỹ năng thiếu (ĐỎ) - Lấy từ skillsMissing (Backend missingSkills) */}
                  {job.skillsMissing && job.skillsMissing.length > 0 && (
                    <div className="mb-4">
                      <p className="text-[10px] text-gray-400 mb-1.5 uppercase font-bold tracking-wider">
                        Cần bổ sung
                      </p>
                      <div className="flex flex-wrap gap-1.5">
                        {job.skillsMissing
                          .slice(0, 3)
                          .map((skill: string, idx: number) => (
                            <span
                              key={idx}
                              className="text-xs px-2 py-0.5 bg-red-50 text-red-600 rounded border border-red-100"
                            >
                              {skill}
                            </span>
                          ))}
                      </div>
                    </div>
                  )}

                  <div className="mt-4 flex gap-3">
                    {/* Nút 1: AI Phân tích (Màu Tím) */}
                    {/* Bạn nhớ sửa href thành đường dẫn đúng tới trang phân tích chi tiết của bạn */}
                    <Link href={`/cv-analysis/${job.id}`} className="flex-1">
                      <button className="w-full py-2 rounded-lg border border-purple-200 bg-purple-50 text-purple-700 font-medium text-sm hover:bg-purple-100 transition-colors flex items-center justify-center gap-2">
                        <Sparkles size={16} className="text-purple-600" />
                        Phân tích
                      </button>
                    </Link>

                    {/* Nút 2: Ứng tuyển (Màu Xanh - Code cũ nhưng chỉnh lại class) */}
                    <button
                      onClick={() => handleApply(job.id)}
                      disabled={applyingId === job.id}
                      className={`flex-1 py-2 rounded-lg border font-medium text-sm transition-colors flex items-center justify-center
                        ${
                          applyingId === job.id
                            ? "bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed"
                            : "border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white"
                        }`}
                    >
                      {applyingId === job.id
                        ? "Đang xử lý..."
                        : "Ứng tuyển ngay"}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
