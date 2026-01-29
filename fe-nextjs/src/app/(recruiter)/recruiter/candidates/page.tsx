"use client";
import { useState } from "react";
import { Search, Sparkles } from "lucide-react";
import { recruitmentService } from "@/services/recruitmentService";
import { CandidateSearchResult } from "@/types/recruitment";

export default function CandidateSearchPage() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<CandidateSearchResult[]>([]);
  const [loading, setLoading] = useState(false);

  const handleSearch = async () => {
    if (!query.trim()) return;
    setLoading(true);
    try {
      const data = await recruitmentService.searchCandidates(query);
      setResults(data);
    } catch (error) {
      alert("Lỗi tìm kiếm");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <div className="text-center space-y-2">
        <h1 className="text-3xl font-bold text-gray-900 flex items-center justify-center gap-2">
          <Sparkles className="text-purple-600" /> Tìm kiếm nhân tài AI
        </h1>
        <p className="text-gray-500">Nhập mô tả công việc (JD) để tìm ứng viên phù hợp nhất trong hệ thống.</p>
      </div>

      <div className="bg-white p-6 rounded-2xl shadow-sm border border-purple-100">
        <textarea
          className="w-full border border-gray-200 rounded-xl p-4 h-32 focus:ring-2 ring-purple-500 focus:border-transparent outline-none transition"
          placeholder="Ví dụ: Tìm lập trình viên ReactJS có 2 năm kinh nghiệm, biết sử dụng Tailwind CSS và Next.js..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <div className="flex justify-end mt-4">
          <button 
            onClick={handleSearch}
            disabled={loading || !query}
            className="bg-purple-600 text-white px-6 py-3 rounded-xl font-medium flex items-center gap-2 hover:bg-purple-700 disabled:opacity-50 transition shadow-lg shadow-purple-200"
          >
            {loading ? "Đang AI phân tích..." : <><Search size={20} /> Tìm kiếm ngay</>}
          </button>
        </div>
      </div>

      <div className="space-y-4">
        <h2 className="font-semibold text-lg text-gray-700">Kết quả ({results.length})</h2>
        <div className="grid md:grid-cols-2 gap-4">
          {results.map((c, idx) => (
            <div key={idx} className="bg-white p-5 rounded-xl border border-gray-100 hover:border-purple-200 hover:shadow-md transition group">
              <div className="flex justify-between items-start">
                <div className="flex gap-4">
                  <div className="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center font-bold text-gray-500">
                    {c.fullName?.charAt(0) || "U"}
                  </div>
                  <div>
                    <h3 className="font-bold text-gray-900">{c.fullName || "Ứng viên ẩn danh"}</h3>
                    <p className="text-purple-600 text-sm font-medium">{c.title || "Chưa cập nhật chức danh"}</p>
                    <div className="mt-2 flex flex-wrap gap-1">
                      {c.skills?.slice(0, 3).map((skill: string) => (
                        <span key={skill} className="bg-gray-100 px-2 py-0.5 rounded text-xs text-gray-600">{skill}</span>
                      ))}
                    </div>
                  </div>
                </div>
                <button className="text-sm border border-blue-600 text-blue-600 px-3 py-1 rounded-lg hover:bg-blue-50 transition">
                  Xem hồ sơ
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}