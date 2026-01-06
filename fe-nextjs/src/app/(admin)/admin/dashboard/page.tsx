'use client';

import React, { useEffect, useState } from 'react';
import api from '@/services/api';
import { 
  Users, 
  Briefcase, 
  FileText, 
  TrendingUp, 
  Activity,
  Calendar
} from 'lucide-react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  AreaChart,
  Area
} from 'recharts';
import { DashboardSummary, ApplicationsByDay } from '@/types/admin';

export default function AdminDashboard() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [chartData, setChartData] = useState<ApplicationsByDay[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Gọi API Dashboard Summary
        // Endpoint này cần khớp với AdminDashboardController.java
        const summaryRes = await api.get('/admin/dashboard/summary');
        setSummary(summaryRes.data.data); // data.data vì có MessageResponse bọc ngoài

        // Gọi API Chart Data (nếu tách riêng)
        const chartRes = await api.get('/admin/dashboard/applications-chart');
        setChartData(chartRes.data.data);
      } catch (error) {
        console.error("Lỗi tải dữ liệu Dashboard:", error);
        // Dữ liệu mẫu (Mock data) phòng khi API chưa chạy
        setSummary({
          totalCandidates: 120,
          totalRecruiters: 15,
          totalActiveJobs: 45,
          totalApplications: 300
        });
        setChartData([
          { date: 'T2', count: 12 },
          { date: 'T3', count: 19 },
          { date: 'T4', count: 3 },
          { date: 'T5', count: 25 },
          { date: 'T6', count: 14 },
          { date: 'T7', count: 30 },
          { date: 'CN', count: 10 },
        ]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center bg-gray-50">
        <div className="text-blue-600 text-xl font-semibold animate-pulse">
          Đang tải dữ liệu hệ thống...
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Tổng quan hệ thống</h1>
          <p className="text-gray-500">Chào mừng trở lại, Administrator.</p>
        </div>
        <button className="bg-white border border-gray-300 px-4 py-2 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 shadow-sm flex items-center gap-2">
          <Calendar className="w-4 h-4" />
          Hôm nay: {new Date().toLocaleDateString('vi-VN')}
        </button>
      </div>

      {/* 1. Stats Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard 
          title="Ứng viên" 
          value={summary?.totalCandidates || 0} 
          icon={<Users className="w-6 h-6 text-blue-600" />} 
          trend="+12% so với tháng trước"
          color="bg-blue-50"
        />
        <StatCard 
          title="Nhà tuyển dụng" 
          value={summary?.totalRecruiters || 0} 
          icon={<Briefcase className="w-6 h-6 text-purple-600" />} 
          trend="+5% so với tháng trước"
          color="bg-purple-50"
        />
        <StatCard 
          title="Tin tuyển dụng" 
          value={summary?.totalActiveJobs || 0} 
          icon={<FileText className="w-6 h-6 text-green-600" />} 
          trend="85 tin đang mở"
          color="bg-green-50"
        />
        <StatCard 
          title="Lượt ứng tuyển" 
          value={summary?.totalApplications || 0} 
          icon={<TrendingUp className="w-6 h-6 text-orange-600" />} 
          trend="+24% tuần này"
          color="bg-orange-50"
        />
      </div>

      {/* 2. Charts & Activity Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Main Chart (Chiếm 2/3) */}
        <div className="lg:col-span-2 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h3 className="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">
            <Activity className="w-5 h-5 text-blue-500" />
            Biểu đồ Ứng tuyển (7 ngày qua)
          </h3>
          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="colorCount" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#3B82F6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{fill: '#6B7280'}} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#6B7280'}} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#fff', borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                />
                <Area type="monotone" dataKey="count" stroke="#3B82F6" fillOpacity={1} fill="url(#colorCount)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Recent Activity (Chiếm 1/3) */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h3 className="text-lg font-bold text-gray-800 mb-4">Hoạt động gần đây</h3>
          <div className="space-y-4">
            {/* Mock Data cho hoạt động - Bạn có thể map từ API thật */}
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="flex items-start gap-3 pb-3 border-b border-gray-50 last:border-0 last:pb-0">
                <div className="w-2 h-2 mt-2 rounded-full bg-blue-500"></div>
                <div>
                  <p className="text-sm text-gray-800 font-medium">Nguyễn Văn A vừa ứng tuyển vào Viettel</p>
                  <p className="text-xs text-gray-500">2 phút trước</p>
                </div>
              </div>
            ))}
            <button className="w-full text-center text-sm text-blue-600 font-medium mt-4 hover:underline">
              Xem tất cả hoạt động
            </button>
          </div>
        </div>
      </div>

      {/* 3. Quick Actions Table (Optional) */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100">
          <h3 className="font-bold text-gray-800">Quản lý nhanh</h3>
        </div>
        <div className="p-6 grid grid-cols-1 md:grid-cols-3 gap-4">
            <ActionButton label="Phê duyệt bài đăng" count={5} color="text-yellow-600 bg-yellow-50" />
            <ActionButton label="Báo cáo vi phạm" count={2} color="text-red-600 bg-red-50" />
            <ActionButton label="Yêu cầu hỗ trợ" count={0} color="text-gray-600 bg-gray-50" />
        </div>
      </div>
    </div>
  );
}

// Component phụ: Stat Card
function StatCard({ title, value, icon, trend, color }: any) {
  return (
    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
      <div className="flex justify-between items-start mb-4">
        <div>
          <p className="text-sm font-medium text-gray-500">{title}</p>
          <h3 className="text-3xl font-bold text-gray-800 mt-1">{value}</h3>
        </div>
        <div className={`p-3 rounded-lg ${color}`}>
          {icon}
        </div>
      </div>
      <p className="text-xs text-gray-500 flex items-center gap-1">
        {trend.includes('+') ? (
          <span className="text-green-600 font-medium bg-green-50 px-1 rounded">{trend}</span>
        ) : (
          <span className="text-gray-600">{trend}</span>
        )}
      </p>
    </div>
  );
}

// Component phụ: Action Button
function ActionButton({ label, count, color }: any) {
    return (
        <button className={`flex items-center justify-between p-4 rounded-lg border border-transparent hover:border-gray-200 transition-all ${color}`}>
            <span className="font-medium">{label}</span>
            {count > 0 && <span className="bg-white px-2 py-1 rounded-full text-xs font-bold shadow-sm">{count}</span>}
        </button>
    )
}