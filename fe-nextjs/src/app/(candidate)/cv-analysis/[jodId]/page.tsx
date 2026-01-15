"use client";

import React, { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft, Loader2 } from "lucide-react";
import CVAnalysisResult from "@/components/features/cv/CVAnalysisResult"; // Component bạn vừa sửa
import { getJobAnalysisResult } from "@/services/candidateService"; // Giả sử bạn có hàm này gọi AI hoặc lấy từ Cache DB

export default function JobAnalysisPage() {
  const params = useParams();
  const router = useRouter();
  const jobId = params.jodId;

  const [analysisData, setAnalysisData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  console.log("🔄 State hiện tại (Render):", analysisData);

  useEffect(() => {
    const fetchAnalysis = async () => {
      try {
        setLoading(true);
        // Gọi API lấy kết quả phân tích chi tiết của Job này với User hiện tại
        // (Nếu chưa có trong DB thì Backend tự gọi AI phân tích rồi trả về)
        const data = await getJobAnalysisResult(Number(jobId));
        console.log("data:", data);
        setAnalysisData(data);
      } catch (error) {
        console.error("Lỗi tải phân tích:", error);
      } finally {
        setLoading(false);
      }
    };

    if (jobId) {
      fetchAnalysis();
    }
  }, [jobId]);

  return (
    <div className="min-h-screen bg-gray-50 pb-10">
      {/* Header điều hướng */}
      <div className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-5xl mx-auto px-4 h-16 flex items-center">
          <button
            onClick={() => router.back()}
            className="flex items-center gap-2 text-gray-600 hover:text-blue-600 transition-colors"
          >
            <ArrowLeft size={20} />
            <span>Quay lại tìm việc</span>
          </button>
        </div>
      </div>

      <div className="max-w-5xl mx-auto px-4 mt-8">
        {loading ? (
          <div className="flex flex-col items-center justify-center py-20">
            <Loader2 className="animate-spin text-blue-600 mb-4" size={48} />
            <p className="text-gray-500 text-lg">
              AI đang đọc kỹ JD và CV của bạn...
            </p>
            <p className="text-gray-400 text-sm">
              Quá trình này có thể mất 5-10 giây.
            </p>
          </div>
        ) : (
          <div className="space-y-6">
            {/* 1. Phần tiêu đề Job */}
            <div className="bg-gradient-to-r from-purple-600 to-indigo-600 text-white p-8 rounded-2xl shadow-lg">
              <h1 className="text-3xl font-bold mb-2">
                Báo cáo mức độ phù hợp
              </h1>
              <p className="opacity-90">
                Phân tích chuyên sâu cho vị trí:{" "}
                <span className="font-bold text-yellow-300">
                  {analysisData?.jobTitle}
                </span>
              </p>
            </div>

            {/* 2. Component Kết quả (Tái sử dụng cái bạn vừa sửa) */}
            {/* Lưu ý: Bạn cần map dữ liệu từ API về đúng format của props 'profile' hoặc sửa component để nhận props khác */}
            <CVAnalysisResult result={analysisData} />
          </div>
        )}
      </div>
    </div>
  );
}
