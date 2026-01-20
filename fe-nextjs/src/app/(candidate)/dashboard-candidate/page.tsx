"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  getRecentJobs,
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
  Sparkles,
  Briefcase,
  FileText, // Icon cho mô tả
  ListChecks,
  User, // Icon cho yêu cầu
} from "lucide-react";

const formatTimeAgo = (dateString: string) => {
  if (!dateString) return "Mới đăng";
  const date = new Date(dateString);
  const now = new Date();
  const diffTime = Math.abs(now.getTime() - date.getTime());
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays <= 1 ? "Vừa xong" : `${diffDays} ngày trước`;
};

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
        const jobsData = await getRecentJobs();

        if (jobsData && jobsData.length > 0) {
          const jobIds = jobsData.map((job: any) => job.id);

          try {
            const scoresMap = await getBatchScores(jobIds);
            const mergedJobs = jobsData.map((job: any) => {
              const result = scoresMap[job.id] || {
                matchScore: 0,
                matchedSkills: [],
                missingSkills: [],
              };

              return {
                ...job,
                matchScore: result.matchScore,
                skillsFound: result.matchedSkills,
                skillsMissing: result.missingSkills,
              };
            });
            mergedJobs.sort((a: any, b: any) => b.matchScore - a.matchScore);
            setJobs(mergedJobs);
          } catch (err) {
            console.error("Lỗi tính điểm batch:", err);
            setJobs(
              jobsData.map((j: any) => ({
                ...j,
                matchScore: 0,
                skillsFound: [],
              })),
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
                  className="text-blue-600"
                />
              </svg>
              <span className="absolute inset-0 flex items-center justify-center text-blue-700">
                {/* Nếu có CV thì hiện hình File, chưa có thì hiện hình Người */}
                <FileText size={24} strokeWidth={2.5} />
              </span>
            </div>
            <div className="text-right">
              <p className="font-bold text-gray-800 text-lg">CV Của Bạn</p>
              <p className="text-gray-500 text-sm">
                Xem và cập nhật CV của bạn
              </p>
            </div>
          </div>
          <Link href="/upload-cv">
            <button className="w-full bg-gray-50 text-blue-600 py-2.5 rounded-lg text-sm font-semibold hover:bg-blue-50 border border-blue-100 transition">
              Cập nhật CV ngay
            </button>
          </Link>
        </div>

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
          <Link href={`/interview/`}>
            <button className="w-fit bg-white text-purple-700 py-2 px-6 rounded-lg text-sm font-bold shadow-sm hover:bg-gray-50 transition relative z-10">
              Chat với AI Coach
            </button>
          </Link>
        </div>
      </div>

      {/* 3. SECTION: JOB LIST */}
      <div>
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
            <Star className="text-yellow-500 fill-yellow-500" size={24} />
            Danh Sách công việc mới nhất
          </h2>
          <Link
            href="/jobs?mode=all" // [QUAN TRỌNG] Thêm query param
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

        {/* LƯỚI CARD */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 auto-rows-fr">
          {/* auto-rows-fr giúp các card trong cùng hàng có chiều cao bằng nhau */}

          {jobs.map((job: any) => (
            <div
              key={job.id}
              // [FIX] Thêm flex flex-col h-full để card kéo dãn hết chiều cao ô lưới
              className="group bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-lg hover:border-blue-200 transition-all duration-300 relative overflow-hidden flex flex-col h-full"
            >
              {/* HEADER */}
              <div className="flex justify-between items-start p-5 pb-2">
                <div className="flex gap-4">
                  <div className="w-14 h-14 bg-gray-50 rounded-xl flex items-center justify-center border border-gray-100 group-hover:bg-white group-hover:shadow-sm transition font-bold text-xl text-blue-600 flex-shrink-0">
                    {job.company ? job.company.charAt(0).toUpperCase() : "C"}
                  </div>
                  <div>
                    <h3 className="font-bold text-lg text-gray-800 group-hover:text-blue-600 transition-colors line-clamp-1">
                      {job.title}
                    </h3>
                    <p className="text-sm text-gray-500 font-medium line-clamp-1">
                      {job.company}
                    </p>
                  </div>
                </div>

                <div className="flex flex-col items-end gap-1 flex-shrink-0 pl-2">
                  <div className="bg-blue-50 px-3 py-1 rounded-full border border-blue-100">
                    <span className="text-sm font-bold text-blue-700 flex items-center gap-1">
                      <Sparkles size={12} /> {job.matchScore}%
                    </span>
                  </div>
                  <span className="text-[10px] text-gray-400 font-medium whitespace-nowrap">
                    {formatTimeAgo(job.createdAt || job.postedAt)}
                  </span>
                </div>
              </div>

              {/* BODY: [FIX] Thêm flex-1 để phần này chiếm hết khoảng trống, đẩy Footer xuống đáy */}
              <div className="px-5 py-2 flex-1 flex flex-col gap-4">
                {/* TAGS */}
                <div className="flex flex-wrap gap-2 text-xs text-gray-600">
                  <span className="flex items-center bg-gray-50 px-2 py-1 rounded border border-gray-200">
                    <MapPin size={12} className="mr-1 text-gray-400" />{" "}
                    {job.location}
                  </span>
                  <span className="flex items-center bg-green-50 text-green-700 px-2 py-1 rounded border border-green-100 font-medium">
                    <DollarSign size={12} className="mr-1" /> {job.salary}
                  </span>
                  <span className="flex items-center bg-purple-50 text-purple-700 px-2 py-1 rounded border border-purple-100">
                    <Briefcase size={12} className="mr-1" />
                    {job.jobType || "Full-time"}
                  </span>
                </div>

                {/* [MỚI] MÔ TẢ & YÊU CẦU */}
                <div className="space-y-2">
                  {job.description && (
                    <div className="flex gap-2 items-start">
                      <FileText
                        size={14}
                        className="mt-1 text-blue-500 shrink-0"
                      />
                      <div>
                        <span className="text-[10px] font-bold text-gray-400 uppercase block mb-0.5">
                          Mô tả:
                        </span>
                        <p className="text-sm text-gray-600 line-clamp-2 leading-relaxed">
                          {job.description}
                        </p>
                      </div>
                    </div>
                  )}

                  {job.requirements && (
                    <div className="flex gap-2 items-start">
                      <ListChecks
                        size={14}
                        className="mt-1 text-orange-500 shrink-0"
                      />
                      <div>
                        <span className="text-[10px] font-bold text-gray-400 uppercase block mb-0.5">
                          Yêu cầu:
                        </span>
                        <p className="text-sm text-gray-600 line-clamp-2 leading-relaxed">
                          {job.requirements}
                        </p>
                      </div>
                    </div>
                  )}
                </div>

                {/* SKILLS SECTION (Đẩy xuống dưới cùng của body) */}
                <div className="mt-auto space-y-2 pt-2">
                  {/* Kỹ năng phù hợp */}
                  {job.skillsFound && job.skillsFound.length > 0 && (
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-1.5 mb-1">
                        <p className="text-[10px] text-gray-400 uppercase font-bold tracking-wider">
                          Kỹ năng phù hợp
                        </p>
                      </div>
                      <div className="flex flex-wrap gap-1.5">
                        {job.skillsFound
                          .slice(0, 4)
                          .map((skill: string, idx: number) => (
                            <span
                              key={idx}
                              className="text-xs px-2 py-0.5 bg-blue-50 text-blue-700 rounded border border-blue-100 whitespace-nowrap"
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

                  {/* Kỹ năng thiếu */}
                  {job.skillsMissing && job.skillsMissing.length > 0 && (
                    <div className="flex flex-col gap-1 mt-1">
                      <div className="flex items-center gap-1.5 mb-1">
                        <p className="text-[10px] text-gray-400 uppercase font-bold tracking-wider">
                          Cần bổ sung
                        </p>
                      </div>
                      <div className="flex flex-wrap gap-1.5">
                        {job.skillsMissing
                          .slice(0, 3)
                          .map((skill: string, idx: number) => (
                            <span
                              key={idx}
                              className="text-xs px-2 py-0.5 bg-red-50 text-red-600 rounded border border-red-100 whitespace-nowrap"
                            >
                              {skill}
                            </span>
                          ))}
                        {job.skillsMissing.length > 3 && (
                          <span className="text-xs px-2 py-0.5 bg-gray-50 text-gray-500 rounded border border-gray-200">
                            +{job.skillsMissing.length - 3}
                          </span>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* FOOTER: BUTTONS */}
              {/* [FIX] mt-auto đảm bảo footer luôn nằm dưới cùng card */}
              <div className="p-5 pt-2 mt-auto border-t border-gray-50">
                <div className="flex gap-3 mt-3">
                  <Link href={`/cv-analysis/${job.id}`} className="flex-1">
                    <button className="w-full py-2.5 rounded-lg bg-purple-50 text-purple-700 font-semibold text-sm hover:bg-purple-100 transition-colors flex items-center justify-center gap-2 group-hover:shadow-sm">
                      <Sparkles size={16} />
                      AI Phân tích
                    </button>
                  </Link>

                  <button
                    onClick={() => handleApply(job.id)}
                    disabled={applyingId === job.id}
                    className={`flex-1 py-2.5 rounded-lg font-semibold text-sm transition-all shadow-sm flex items-center justify-center
                      ${
                        applyingId === job.id
                          ? "bg-gray-100 text-gray-400 cursor-not-allowed"
                          : "bg-blue-600 text-white hover:bg-blue-700 hover:shadow-md"
                      }`}
                  >
                    {applyingId === job.id ? "Đang gửi..." : "Ứng tuyển ngay"}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
