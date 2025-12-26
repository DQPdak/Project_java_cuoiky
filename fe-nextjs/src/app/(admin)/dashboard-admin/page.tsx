'use client';

import React from 'react';
import { Users, Briefcase, FileText, Server } from 'lucide-react';

export default function AdminDashboard() {
  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-8">Thống kê hệ thống</h1>

      {/* System Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        {[
            { title: 'Tổng User', value: '15,204', icon: Users, color: 'bg-blue-500' },
            { title: 'Việc làm Active', value: '843', icon: Briefcase, color: 'bg-green-500' },
            { title: 'CV đã phân tích', value: '5,129', icon: FileText, color: 'bg-purple-500' },
            { title: 'Server Load', value: '24%', icon: Server, color: 'bg-orange-500' },
        ].map((stat, index) => (
            <div key={index} className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between">
                <div>
                    <p className="text-sm font-medium text-gray-500 mb-1">{stat.title}</p>
                    <h3 className="text-2xl font-bold text-gray-900">{stat.value}</h3>
                </div>
                <div className={`p-3 rounded-lg ${stat.color} text-white shadow-md`}>
                    <stat.icon size={24} />
                </div>
            </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Logs / Activity */}
        <div className="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-100">
            <div className="px-6 py-4 border-b border-gray-100">
                <h3 className="font-bold text-gray-800">Hoạt động gần đây</h3>
            </div>
            <div className="p-0">
                <table className="w-full text-sm text-left">
                    <thead className="text-xs text-gray-500 uppercase bg-gray-50">
                        <tr>
                            <th className="px-6 py-3">User</th>
                            <th className="px-6 py-3">Hành động</th>
                            <th className="px-6 py-3">Thời gian</th>
                            <th className="px-6 py-3">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        {[
                            { user: 'user1@example.com', action: 'Login', time: '2 phút trước', status: 'Success' },
                            { user: 'recruiter2@tech.com', action: 'Post Job', time: '15 phút trước', status: 'Pending' },
                            { user: 'admin', action: 'Delete User', time: '1 giờ trước', status: 'Success' },
                            { user: 'candidate99', action: 'Upload CV', time: '3 giờ trước', status: 'Failed' },
                        ].map((log, i) => (
                            <tr key={i} className="border-b hover:bg-gray-50">
                                <td className="px-6 py-4 font-medium">{log.user}</td>
                                <td className="px-6 py-4">{log.action}</td>
                                <td className="px-6 py-4 text-gray-500">{log.time}</td>
                                <td className="px-6 py-4">
                                    <span className={`px-2 py-1 rounded-full text-xs font-bold ${
                                        log.status === 'Success' ? 'bg-green-100 text-green-700' : 
                                        log.status === 'Pending' ? 'bg-yellow-100 text-yellow-700' : 
                                        'bg-red-100 text-red-700'
                                    }`}>
                                        {log.status}
                                    </span>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>

        {/* User Growth */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h3 className="font-bold text-gray-800 mb-4">Tăng trưởng người dùng</h3>
            <div className="space-y-4">
                <div className="flex items-end gap-2 h-40 justify-between px-2">
                    {[30, 45, 35, 60, 55, 75, 90].map((h, i) => (
                        <div key={i} className="w-8 bg-blue-500 rounded-t-sm hover:bg-blue-600 transition" style={{ height: `${h}%` }}></div>
                    ))}
                </div>
                <div className="flex justify-between text-xs text-gray-500">
                    <span>T2</span>
                    <span>T3</span>
                    <span>T4</span>
                    <span>T5</span>
                    <span>T6</span>
                    <span>T7</span>
                    <span>CN</span>
                </div>
            </div>
            <div className="mt-6 text-center">
                <p className="text-3xl font-bold text-gray-900">+145</p>
                <p className="text-sm text-green-600 font-medium">Người dùng mới tuần này</p>
            </div>
        </div>
      </div>
    </div>
  );
}