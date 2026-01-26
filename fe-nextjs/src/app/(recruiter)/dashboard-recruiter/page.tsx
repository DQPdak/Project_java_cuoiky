"use client";

import React, { useState, useEffect } from 'react';
import { Users, FileText, Eye, TrendingUp, Plus, Filter, Clock, Search, Calendar, CheckCircle, XCircle } from 'lucide-react';
import { recruitmentService, DashboardStats } from "@/services/recruitmentService"; // Import service và type
import Link from 'next/link';

export default function RecruiterDashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedPipeline, setSelectedPipeline] = useState('all');

  // Load dữ liệu khi component mount
  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await recruitmentService.getDashboardStats();
        setStats(data);
      } catch (error) {
        console.error("Lỗi tải thống kê dashboard", error);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  // Helper để lấy số lượng từ pipelineStats an toàn
  const getCount = (statusKey: string) => {
    if (!stats?.pipelineStats) return 0;
    return stats.pipelineStats[statusKey] || 0;
  };

  // Định nghĩa Pipeline hiển thị (Mapping với Enum Backend: PENDING, SCREENING, INTERVIEW, OFFERED, HIRED...)
  // Lưu ý: Key phải khớp với Enum name trong Java (thường là chữ in hoa)
  const pipelines = [
    { id: 'PENDING', name: 'Chờ duyệt', count: getCount('PENDING') },
    { id: 'SCREENING', name: 'Sơ tuyển', count: getCount('SCREENING') },
    { id: 'INTERVIEW', name: 'Phỏng vấn', count: getCount('INTERVIEW') },
    { id: 'OFFERED', name: 'Đề nghị', count: getCount('OFFERED') },
    { id: 'HIRED', name: 'Đã tuyển', count: getCount('HIRED') },
    { id: 'REJECTED', name: 'Từ chối', count: getCount('REJECTED') },
  ];
  
  // Tính tổng số đơn thực tế từ pipeline
  const totalApplications = stats?.totalCandidates || 0;

  if (loading) return <div className="p-8 text-center">Đang tải dữ liệu dashboard...</div>;

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard Tuyển dụng</h1>
          <p className="text-gray-500">Tổng quan hiệu suất tuyển dụng và quản lý pipelines ứng viên.</p>
        </div>
        <Link href="/recruiter/manage-jobs" className="flex items-center bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition shadow-sm font-medium">
          <Plus className="w-4 h-4 mr-2" /> Đăng tin mới
        </Link>
      </div>

      {/* Stats Cards - Dữ liệu thực tế */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        {[
          { title: 'Tin đang tuyển', value: stats?.totalActiveJobs || 0, icon: FileText, color: 'text-blue-600', bg: 'bg-blue-50' },
          { title: 'Tổng ứng viên', value: totalApplications, icon: Users, color: 'text-purple-600', bg: 'bg-purple-50' },
          { title: 'Đơn mới hôm nay', value: stats?.newCandidatesToday || 0, icon: TrendingUp, color: 'text-green-600', bg: 'bg-green-50' },
          // Stat cuối có thể giữ cứng hoặc tính toán thêm
          { title: 'Tỷ lệ chuyển đổi', value: '---', icon: Eye, color: 'text-orange-600', bg: 'bg-orange-50' },
        ].map((stat, index) => (
          <div key={index} className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center">
            <div className={`p-4 rounded-lg ${stat.bg} ${stat.color} mr-4`}>
              <stat.icon size={24} />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-500">{stat.title}</p>
              <h3 className="text-2xl font-bold text-gray-900">{stat.value}</h3>
            </div>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* Pipeline Overview - Dữ liệu thực tế */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100">
          <div className="px-6 py-4 border-b border-gray-100">
            <h3 className="font-bold text-gray-800">Luồng Ứng viên (Pipelines)</h3>
          </div>
          <div className="p-4 space-y-3">
            {pipelines.map((pipeline) => (
              <div
                key={pipeline.id}
                onClick={() => setSelectedPipeline(pipeline.id)}
                className={`flex items-center justify-between p-3 rounded-lg cursor-pointer transition ${
                  selectedPipeline === pipeline.id ? 'bg-blue-50 border border-blue-200' : 'hover:bg-gray-50'
                }`}
              >
                <span className="font-medium text-gray-700">{pipeline.name}</span>
                <span className="text-sm bg-gray-100 text-gray-600 px-2 py-1 rounded-full">{pipeline.count}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Phần Recent Candidates (Có thể cần thêm API list candidates để hoàn thiện sau) */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 lg:col-span-2">
            <div className="p-8 text-center text-gray-500">
                (<span className="font-medium">Danh sách ứng viên gần đây</span> sẽ được cập nhật sau khi hoàn thành API)
            </div>
        </div>
      </div>
    </div>
  );
}