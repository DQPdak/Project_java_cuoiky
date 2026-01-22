"use client";
import { useState, useEffect } from "react";
import Link from "next/link";
import { Plus, Users, MapPin, Calendar, DollarSign } from "lucide-react";
import { recruitmentService } from "@/services/recruitmentService";
import { JobPosting, JobStatus } from "@/types/recruitment";
import toast from "react-hot-toast";

export default function ManageJobsPage() {
  const [jobs, setJobs] = useState<JobPosting[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // State cho form tạo mới
  const [newJob, setNewJob] = useState({
    title: "", location: "", salaryRange: "", deadline: "", description: "", requirements: ""
  });

  useEffect(() => {
    loadJobs();
  }, []);

  const loadJobs = async () => {
    try {
      const data = await recruitmentService.getMyJobs();
      setJobs(data);
    } catch (error) {
      toast.error("Lỗi tải danh sách việc làm");
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateJob = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await recruitmentService.createJob(newJob);
      toast.success("Đăng tin thành công!");
      setShowCreateModal(false);
      loadJobs(); // Reload lại list
      setNewJob({ title: "", location: "", salaryRange: "", deadline: "", description: "", requirements: "" });
    } catch (error) {
      toast.error("Lỗi khi tạo tin tuyển dụng");
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Quản lý tuyển dụng</h1>
        <button 
          onClick={() => setShowCreateModal(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-blue-700 transition"
        >
          <Plus size={20} /> Đăng tin mới
        </button>
      </div>

      {/* Danh sách Jobs */}
      {isLoading ? <div className="text-center py-10">Đang tải...</div> : (
        <div className="grid gap-4">
          {jobs.length === 0 ? <p className="text-gray-500">Chưa có tin tuyển dụng nào.</p> : jobs.map((job) => (
            <div key={job.id} className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="text-xl font-bold text-gray-900 mb-1">
                    <Link href={`/recruiter/manage-jobs/${job.id}`} className="hover:text-blue-600">
                      {job.title}
                    </Link>
                  </h3>
                  <div className="flex gap-4 text-sm text-gray-500 mb-3">
                    <span className="flex items-center gap-1"><MapPin size={16}/> {job.location}</span>
                    <span className="flex items-center gap-1"><DollarSign size={16}/> {job.salaryRange}</span>
                    <span className="flex items-center gap-1"><Calendar size={16}/> {new Date(job.deadline).toLocaleDateString('vi-VN')}</span>
                  </div>
                </div>
                <div className={`px-3 py-1 rounded-full text-xs font-semibold
                  ${job.status === JobStatus.OPEN ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'}`}>
                  {job.status === JobStatus.OPEN ? 'Đang tuyển' : 'Đã đóng'}
                </div>
              </div>
              
              <div className="border-t pt-4 flex justify-between items-center">
                <Link 
                  href={`/recruiter/manage-jobs/${job.id}`}
                  className="flex items-center gap-2 text-blue-600 font-medium hover:underline"
                >
                  <Users size={18} />
                  Xem {job.applicationCount || 0} hồ sơ ứng tuyển
                </Link>
                <button className="text-gray-400 hover:text-red-500 text-sm">Xóa tin</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal Tạo Job */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <h2 className="text-xl font-bold mb-4">Đăng tin tuyển dụng mới</h2>
            <form onSubmit={handleCreateJob} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <input placeholder="Tiêu đề công việc" className="border p-2 rounded" required
                  value={newJob.title} onChange={e => setNewJob({...newJob, title: e.target.value})} />
                <input placeholder="Địa điểm" className="border p-2 rounded" required
                  value={newJob.location} onChange={e => setNewJob({...newJob, location: e.target.value})} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <input placeholder="Mức lương (VD: 10-15 triệu)" className="border p-2 rounded"
                  value={newJob.salaryRange} onChange={e => setNewJob({...newJob, salaryRange: e.target.value})} />
                <input type="date" className="border p-2 rounded" required
                  value={newJob.deadline} onChange={e => setNewJob({...newJob, deadline: e.target.value})} />
              </div>
              <textarea placeholder="Mô tả công việc..." className="border p-2 rounded w-full h-24" required
                value={newJob.description} onChange={e => setNewJob({...newJob, description: e.target.value})} />
              <textarea placeholder="Yêu cầu ứng viên..." className="border p-2 rounded w-full h-24" required
                value={newJob.requirements} onChange={e => setNewJob({...newJob, requirements: e.target.value})} />
              
              <div className="flex justify-end gap-3 pt-4">
                <button type="button" onClick={() => setShowCreateModal(false)} className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded">Hủy</button>
                <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Đăng tin</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}