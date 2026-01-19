'use client';

import React, { useState } from 'react';
import { Users, FileText, Eye, TrendingUp, Plus, Search, Filter, Calendar, CheckCircle, Clock, XCircle } from 'lucide-react';

export default function RecruiterDashboard() {
  const [selectedPipeline, setSelectedPipeline] = useState('all');

  const pipelines = [
    { id: 'all', name: 'Tất cả', count: 128 },
    { id: 'applied', name: 'Đã ứng tuyển', count: 45 },
    { id: 'screening', name: 'Sơ tuyển', count: 20 },
    { id: 'interview', name: 'Phỏng vấn', count: 15 },
    { id: 'offer', name: 'Đề nghị', count: 8 },
    { id: 'hired', name: 'Đã tuyển', count: 5 },
  ];

  const recentCandidates = [
    { id: 1, name: 'Nguyen Van A', job: 'Java Backend', status: 'applied', score: 85, appliedDate: '2024-01-15' },
    { id: 2, name: 'Tran Thi B', job: 'ReactJS Frontend', status: 'screening', score: 92, appliedDate: '2024-01-14' },
    { id: 3, name: 'Le Van C', job: 'Business Analyst', status: 'interview', score: 78, appliedDate: '2024-01-13' },
    { id: 4, name: 'Pham Thi D', job: 'Java Backend', status: 'offer', score: 88, appliedDate: '2024-01-12' },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'applied': return 'bg-blue-100 text-blue-700';
      case 'screening': return 'bg-yellow-100 text-yellow-700';
      case 'interview': return 'bg-purple-100 text-purple-700';
      case 'offer': return 'bg-green-100 text-green-700';
      case 'hired': return 'bg-emerald-100 text-emerald-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'applied': return <Clock className="w-4 h-4" />;
      case 'screening': return <Search className="w-4 h-4" />;
      case 'interview': return <Calendar className="w-4 h-4" />;
      case 'offer': return <CheckCircle className="w-4 h-4" />;
      case 'hired': return <CheckCircle className="w-4 h-4" />;
      default: return <XCircle className="w-4 h-4" />;
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard Tuyển dụng</h1>
          <p className="text-gray-500">Tổng quan hiệu suất tuyển dụng và quản lý pipelines ứng viên.</p>
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

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* Pipeline Overview */}
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

        {/* Recent Candidates with Match Scores */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 lg:col-span-2">
          <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
            <h3 className="font-bold text-gray-800">Ứng viên gần đây & Điểm phù hợp</h3>
            <div className="flex items-center space-x-2">
              <Filter className="w-4 h-4 text-gray-400" />
              <select className="text-sm border border-gray-300 rounded px-2 py-1">
                <option>Tất cả</option>
                <option>Điểm cao (&gt;80)</option>
                <option>Đang sơ tuyển</option>
              </select>
            </div>
          </div>
          <div className="divide-y divide-gray-100">
            {recentCandidates.map((candidate) => (
              <div key={candidate.id} className="p-4 hover:bg-gray-50 transition">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500">
                      {candidate.name.split(' ').map(n => n[0]).join('')}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{candidate.name}</p>
                      <p className="text-xs text-gray-500">Ứng tuyển: {candidate.job}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`flex items-center gap-1 text-xs px-2 py-1 rounded-full font-medium ${getStatusColor(candidate.status)}`}>
                      {getStatusIcon(candidate.status)}
                      {candidate.status === 'applied' ? 'Đã ứng tuyển' :
                       candidate.status === 'screening' ? 'Sơ tuyển' :
                       candidate.status === 'interview' ? 'Phỏng vấn' :
                       candidate.status === 'offer' ? 'Đề nghị' : 'Đã tuyển'}
                    </span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div>
                      <p className="text-xs text-gray-500">Điểm phù hợp</p>
                      <div className="flex items-center gap-2">
                        <div className="w-16 bg-gray-200 rounded-full h-2">
                          <div className="bg-green-500 h-2 rounded-full" style={{ width: `${candidate.score}%` }}></div>
                        </div>
                        <span className="text-sm font-medium text-gray-700">{candidate.score}%</span>
                      </div>
                    </div>
                  </div>
                  <div className="text-xs text-gray-500">
                    Ứng tuyển: {new Date(candidate.appliedDate).toLocaleDateString('vi-VN')}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Active Jobs Performance */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100">
        <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
          <h3 className="font-bold text-gray-800">Hiệu quả tin đăng</h3>
        </div>
        <div className="p-6 space-y-4">
          {[
            { name: 'Senior Java Backend', views: 80, apps: 45, conversions: 15 },
            { name: 'ReactJS Frontend', views: 60, apps: 20, conversions: 8 },
            { name: 'Business Analyst', views: 40, apps: 15, conversions: 5 },
          ].map((job, i) => (
            <div key={i}>
              <div className="flex justify-between text-sm mb-1">
                <span className="font-medium text-gray-700">{job.name}</span>
                <span className="text-gray-500">{job.apps} ứng tuyển • {job.conversions} chuyển đổi</span>
              </div>
              <div className="w-full bg-gray-100 rounded-full h-2">
                <div className="bg-blue-600 h-2 rounded-full" style={{ width: `${job.views}%` }}></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}