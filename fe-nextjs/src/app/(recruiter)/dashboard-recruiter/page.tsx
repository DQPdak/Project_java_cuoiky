"use client";

import React, { useState, useEffect } from 'react';
import { Users, FileText, Eye, TrendingUp, Plus, Clock, CheckCircle, XCircle, AlertCircle, MoreHorizontal } from 'lucide-react';
import { recruitmentService, DashboardStats } from "@/services/recruitmentService";
import { CandidateApplication, ApplicationStatus } from "@/types/recruitment"; // Import type
import Link from 'next/link';

export default function RecruiterDashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentCandidates, setRecentCandidates] = useState<CandidateApplication[]>([]); // State cho bảng
  const [loading, setLoading] = useState(true);
  const [selectedPipeline, setSelectedPipeline] = useState('all');

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Gọi song song cả 2 API để tối ưu tốc độ
        const [statsData, candidatesData] = await Promise.all([
          recruitmentService.getDashboardStats(),
          recruitmentService.getRecentApplications()
        ]);
        
        setStats(statsData);
        setRecentCandidates(candidatesData);
      } catch (error) {
        console.error("Lỗi tải dữ liệu dashboard", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const getCount = (statusKey: string) => {
    if (!stats?.pipelineStats) return 0;
    return stats.pipelineStats[statusKey] || 0;
  };

  const pipelines = [
    { id: 'PENDING', name: 'Chờ duyệt', count: getCount('PENDING') },
    { id: 'SCREENING', name: 'Sơ tuyển', count: getCount('SCREENING') },
    { id: 'INTERVIEW', name: 'Phỏng vấn', count: getCount('INTERVIEW') },
    { id: 'OFFERED', name: 'Đề nghị', count: getCount('OFFERED') },
    { id: 'HIRED', name: 'Đã tuyển', count: getCount('HIRED') },
    { id: 'REJECTED', name: 'Từ chối', count: getCount('REJECTED') },
  ];
  
  const totalApplications = stats?.totalCandidates || 0;

  // Helper function để hiển thị badge trạng thái đẹp hơn
  const getStatusBadge = (status: ApplicationStatus) => {
    switch (status) {
      case ApplicationStatus.PENDING:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800">Chờ duyệt</span>;
      case ApplicationStatus.SCREENING:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">Sơ tuyển</span>;
      case ApplicationStatus.INTERVIEW:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">Phỏng vấn</span>;
      case ApplicationStatus.OFFERED:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800">Đề nghị</span>;
      case ApplicationStatus.HIRED:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">Đã tuyển</span>;
      case ApplicationStatus.REJECTED:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">Từ chối</span>;
      default:
        return <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">{status}</span>;
    }
  };

  // Helper format ngày tháng
  const formatDate = (dateString?: string) => {
    if (!dateString) return '---';
    return new Date(dateString).toLocaleDateString('vi-VN', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute:'2-digit'
    });
  };

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

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        {[
          { title: 'Tin đang tuyển', value: stats?.totalActiveJobs || 0, icon: FileText, color: 'text-blue-600', bg: 'bg-blue-50' },
          { title: 'Tổng ứng viên', value: totalApplications, icon: Users, color: 'text-purple-600', bg: 'bg-purple-50' },
          { title: 'Đơn mới hôm nay', value: stats?.newCandidatesToday || 0, icon: TrendingUp, color: 'text-green-600', bg: 'bg-green-50' },
          { title: 'Cần duyệt', value: getCount('PENDING'), icon: AlertCircle, color: 'text-orange-600', bg: 'bg-orange-50' },
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
        {/* Pipeline Overview */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 h-fit">
          <div className="px-6 py-4 border-b border-gray-100">
            <h3 className="font-bold text-gray-800">Luồng Ứng viên</h3>
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
                <div className="flex items-center">
                    {/* Icon nhỏ minh họa trạng thái nếu thích */}
                    <span className="font-medium text-gray-700">{pipeline.name}</span>
                </div>
                <span className={`text-sm px-2 py-1 rounded-full font-medium ${
                    selectedPipeline === pipeline.id ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-600'
                }`}>
                    {pipeline.count}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Phần Recent Candidates - Đã hoàn thiện */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 lg:col-span-2 overflow-hidden flex flex-col">
            <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
                <h3 className="font-bold text-gray-800">Ứng viên gần đây</h3>
                <Link href="/dashboard-recruiter" className="text-sm text-blue-600 hover:text-blue-700 font-medium hover:underline">
                    Xem tất cả
                </Link>
            </div>
            
            <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                    <thead className="text-xs text-gray-500 uppercase bg-gray-50 border-b">
                        <tr>
                            <th className="px-6 py-3">Ứng viên</th>
                            <th className="px-6 py-3">Vị trí ứng tuyển</th>
                            <th className="px-6 py-3">Ngày nộp</th>
                            <th className="px-6 py-3">Trạng thái</th>
                            <th className="px-6 py-3 text-right">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        {recentCandidates.length > 0 ? (
                            recentCandidates.map((candidate) => (
                                <tr key={candidate.id} className="bg-white border-b hover:bg-gray-50 transition">
                                    <td className="px-6 py-4 font-medium text-gray-900 flex items-center gap-3">
                                        <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-xs">
                                            {(candidate.candidateName || candidate.studentName || 'U').charAt(0).toUpperCase()}
                                        </div>
                                        {candidate.candidateName || candidate.studentName || 'Unknown Candidate'}
                                    </td>
                                    <td className="px-6 py-4 text-gray-600">
                                        {candidate.jobTitle || '---'}
                                    </td>
                                    <td className="px-6 py-4 text-gray-500">
                                        <div className="flex items-center gap-1">
                                            <Clock size={14} />
                                            {formatDate(candidate.appliedAt)}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        {getStatusBadge(candidate.status)}
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <button className="text-gray-400 hover:text-blue-600">
                                            <MoreHorizontal size={18} />
                                        </button>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                                    Chưa có ứng viên nào nộp đơn gần đây.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
      </div>
    </div>
  );
}