'use client';

import React from 'react';
import { Users, FileText, Eye, TrendingUp, Plus } from 'lucide-react';

export default function RecruiterDashboard() {
  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <div>
            <h1 className="text-2xl font-bold text-gray-900">Dashboard Tuyển dụng</h1>
            <p className="text-gray-500">Tổng quan hiệu suất tuyển dụng của công ty bạn.</p>
        </div>
        <button className="flex items-center bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition shadow-sm font-medium">
            <Plus className="w-4 h-4 mr-2" /> Đăng tin mới
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        {[
            { title: 'Tin đang tuyển', value: '5', icon: FileText, color: 'text-blue-600', bg: 'bg-blue-50' },
            { title: 'Tổng ứng viên', value: '128', icon: Users, color: 'text-purple-600', bg: 'bg-purple-50' },
            { title: 'Lượt xem tin', value: '3,450', icon: Eye, color: 'text-green-600', bg: 'bg-green-50' },
            { title: 'Tỷ lệ chuyển đổi', value: '12%', icon: TrendingUp, color: 'text-orange-600', bg: 'bg-orange-50' },
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

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Applications */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100">
            <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
                <h3 className="font-bold text-gray-800">Ứng viên mới nhất</h3>
                <a href="#" className="text-sm text-blue-600 hover:text-blue-700 font-medium">Xem tất cả</a>
            </div>
            <div className="divide-y divide-gray-100">
                {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="p-4 hover:bg-gray-50 transition flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500">
                                NV
                            </div>
                            <div>
                                <p className="font-medium text-gray-900">Nguyen Van A</p>
                                <p className="text-xs text-gray-500">Ứng tuyển: Java Backend</p>
                            </div>
                        </div>
                        <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full font-medium">Mới</span>
                    </div>
                ))}
            </div>
        </div>

        {/* Active Jobs Performance */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100">
            <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
                <h3 className="font-bold text-gray-800">Hiệu quả tin đăng</h3>
            </div>
            <div className="p-6 space-y-4">
                {[
                    { name: 'Senior Java Backend', views: 80, apps: 45 },
                    { name: 'ReactJS Frontend', views: 60, apps: 20 },
                    { name: 'Business Analyst', views: 40, apps: 15 },
                ].map((job, i) => (
                    <div key={i}>
                        <div className="flex justify-between text-sm mb-1">
                            <span className="font-medium text-gray-700">{job.name}</span>
                            <span className="text-gray-500">{job.apps} ứng tuyển</span>
                        </div>
                        <div className="w-full bg-gray-100 rounded-full h-2">
                            <div className="bg-blue-600 h-2 rounded-full" style={{ width: `${job.views}%` }}></div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
      </div>
    </div>
  );
}