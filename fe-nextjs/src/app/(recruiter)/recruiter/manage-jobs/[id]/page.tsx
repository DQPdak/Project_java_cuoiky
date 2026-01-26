"use client";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation"; // Dùng useParams thay vì props params trong Client Component Next 15+
import { recruitmentService } from "@/services/recruitmentService";
import { CandidateApplication, ApplicationStatus } from "@/types/recruitment";
import { ArrowLeft, Mail, FileText, CheckCircle } from "lucide-react";
import toast from "react-hot-toast";

export default function JobDetailPage() {
  const params = useParams();
  const router = useRouter();
  const jobId = Number(params.id); // Lấy ID từ URL
  
  const [candidates, setCandidates] = useState<CandidateApplication[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (jobId) loadPipeline();
  }, [jobId]);

  const loadPipeline = async () => {
    try {
      const data = await recruitmentService.getJobPipeline(jobId);
      // Sắp xếp theo điểm AI giảm dần
      setCandidates(data.sort((a, b) => b.matchScore - a.matchScore));
    } catch (error) {
      toast.error("Không thể tải danh sách ứng viên");
    } finally {
      setIsLoading(false);
    }
  };

  const updateStatus = async (id: number, status: ApplicationStatus) => {
    try {
      await recruitmentService.updateStatus(id, status);
      toast.success("Đã cập nhật trạng thái");
      setCandidates(prev => prev.map(c => c.id === id ? { ...c, status } : c));
    } catch (error) {
      toast.error("Lỗi cập nhật");
    }
  };

  return (
    <div className="space-y-6">
      <button onClick={() => router.back()} className="flex items-center text-gray-500 hover:text-gray-800">
        <ArrowLeft size={18} className="mr-1" /> Quay lại
      </button>

      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">Danh sách ứng viên (Pipeline)</h1>
        <div className="text-sm text-gray-500">Job ID: #{jobId}</div>
      </div>

      {isLoading ? <div>Đang tải dữ liệu...</div> : (
        <div className="bg-white rounded-xl shadow border border-gray-200 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="py-3 px-4 text-left font-medium text-gray-500">Ứng viên</th>
                <th className="py-3 px-4 text-left font-medium text-gray-500">Độ phù hợp (AI)</th>
                <th className="py-3 px-4 text-left font-medium text-gray-500">CV & Email</th>
                <th className="py-3 px-4 text-left font-medium text-gray-500">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {candidates.length === 0 ? (
                <tr><td colSpan={4} className="text-center py-8 text-gray-500">Chưa có ứng viên nào nộp đơn.</td></tr>
              ) : candidates.map((c) => (
                <tr key={c.id} className="hover:bg-gray-50">
                  <td className="py-3 px-4 font-medium">{c.candidateName}</td>
                  <td className="py-3 px-4">
                    <div className="flex items-center gap-2">
                      <div className="w-full bg-gray-200 rounded-full h-2.5 w-24">
                        <div 
                          className={`h-2.5 rounded-full ${c.matchScore > 75 ? 'bg-green-500' : c.matchScore > 50 ? 'bg-yellow-500' : 'bg-red-500'}`} 
                          style={{ width: `${c.matchScore}%` }}
                        ></div>
                      </div>
                      <span className="text-sm font-bold">{c.matchScore}%</span>
                    </div>
                  </td>
                  <td className="py-3 px-4 space-y-1">
                    <div className="flex items-center gap-1 text-sm text-gray-600">
                        <Mail size={14}/> {c.email}
                    </div>
                    <a href={c.cvUrl} target="_blank" className="flex items-center gap-1 text-sm text-blue-600 hover:underline">
                        <FileText size={14}/> Xem CV
                    </a>
                  </td>
                  <td className="py-3 px-4">
                    <select 
                      value={c.status}
                      onChange={(e) => updateStatus(c.id, e.target.value as ApplicationStatus)}
                      className="border rounded px-2 py-1 text-sm bg-white focus:ring-2 ring-blue-500 outline-none"
                    >
                      <option value="APPLIED">Mới ứng tuyển</option>
                      <option value="SCREENING">Đang xem xét</option>
                      <option value="INTERVIEW">Phỏng vấn</option>
                      <option value="OFFERED">Đề nghị (Offer)</option>
                      <option value="REJECTED">Từ chối</option>
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}