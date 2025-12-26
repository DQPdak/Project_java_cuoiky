'use client';

import React from 'react';
import { Search, MapPin, DollarSign, Clock, CheckCircle, TrendingUp } from 'lucide-react';

export default function CandidateDashboard() {
  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-8 text-white shadow-lg">
        <h1 className="text-3xl font-bold mb-2">Xin chào, Ứng viên! 👋</h1>
        <p className="opacity-90 mb-6">Bạn đã sẵn sàng tìm kiếm công việc mơ ước hôm nay chưa?</p>
        
        <div className="bg-white/10 backdrop-blur-md p-1 rounded-lg flex gap-2 max-w-2xl">
            <div className="flex-1 flex items-center bg-white rounded-md px-4 py-2">
                <Search className="text-gray-400 mr-2" size={20} />
                <input 
                    type="text" 
                    placeholder="Tìm kiếm công việc, kỹ năng, công ty..." 
                    className="w-full bg-transparent outline-none text-gray-800 placeholder-gray-500"
                />
            </div>
            <button className="bg-orange-500 hover:bg-orange-600 text-white px-6 py-2 rounded-md font-medium transition">
                Tìm kiếm
            </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Stats & Recommendations */}
        <div className="lg:col-span-2 space-y-6">
            {/* Stats */}
            <div className="grid grid-cols-3 gap-4">
                <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-500 mb-1">Việc đã ứng tuyển</p>
                    <p className="text-2xl font-bold text-blue-600">12</p>
                </div>
                <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-500 mb-1">Phỏng vấn</p>
                    <p className="text-2xl font-bold text-green-600">3</p>
                </div>
                <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                    <p className="text-sm text-gray-500 mb-1">CV Score</p>
                    <p className="text-2xl font-bold text-purple-600">85/100</p>
                </div>
            </div>

            {/* Recommended Jobs */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
                    <h3 className="font-bold text-gray-800">Việc làm phù hợp với bạn</h3>
                    <a href="#" className="text-sm text-blue-600 hover:underline">Xem tất cả</a>
                </div>
                <div className="divide-y divide-gray-100">
                    {[1, 2, 3].map((item) => (
                        <div key={item} className="p-6 hover:bg-gray-50 transition cursor-pointer flex justify-between items-start">
                            <div className="flex gap-4">
                                <div className="w-12 h-12 bg-gray-200 rounded-lg flex items-center justify-center font-bold text-gray-500">
                                    FPT
                                </div>
                                <div>
                                    <h4 className="font-bold text-gray-800">Senior Java Developer</h4>
                                    <p className="text-sm text-gray-500 mb-2">FPT Software</p>
                                    <div className="flex gap-3 text-xs text-gray-500">
                                        <span className="flex items-center"><MapPin size={12} className="mr-1"/> Hà Nội</span>
                                        <span className="flex items-center"><DollarSign size={12} className="mr-1"/> $2000 - $3000</span>
                                        <span className="flex items-center bg-blue-50 text-blue-600 px-2 py-0.5 rounded-full">Remote</span>
                                    </div>
                                </div>
                            </div>
                            <button className="text-sm border border-blue-600 text-blue-600 px-4 py-1.5 rounded-full hover:bg-blue-50 transition">
                                Ứng tuyển
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </div>

        {/* Right Column: Profile Status */}
        <div className="space-y-6">
            <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                <h3 className="font-bold text-gray-800 mb-4">Trạng thái hồ sơ</h3>
                <div className="flex items-center gap-4 mb-4">
                    <div className="relative w-16 h-16">
                        <svg className="w-full h-full transform -rotate-90">
                            <circle cx="32" cy="32" r="28" stroke="currentColor" strokeWidth="4" fill="transparent" className="text-gray-200" />
                            <circle cx="32" cy="32" r="28" stroke="currentColor" strokeWidth="4" fill="transparent" className="text-blue-600" strokeDasharray="175.9" strokeDashoffset="35" />
                        </svg>
                        <span className="absolute inset-0 flex items-center justify-center font-bold text-sm">80%</span>
                    </div>
                    <div>
                        <p className="font-medium">Hồ sơ khá tốt</p>
                        <p className="text-xs text-gray-500">Hãy cập nhật thêm kỹ năng mềm để đạt 100%</p>
                    </div>
                </div>
                <button className="w-full bg-gray-100 text-gray-700 py-2 rounded-lg text-sm font-medium hover:bg-gray-200 transition">
                    Cập nhật hồ sơ
                </button>
            </div>

            <div className="bg-gradient-to-br from-purple-500 to-indigo-600 p-6 rounded-xl shadow-md text-white">
                <div className="flex items-center gap-2 mb-2">
                    <TrendingUp className="text-yellow-300" />
                    <h3 className="font-bold">AI Career Coach</h3>
                </div>
                <p className="text-sm opacity-90 mb-4">Bạn muốn biết lộ trình thăng tiến cho vị trí Tech Lead?</p>
                <button className="w-full bg-white text-purple-600 py-2 rounded-lg text-sm font-bold shadow-sm hover:bg-gray-50 transition">
                    Chat ngay với AI
                </button>
            </div>
        </div>
      </div>
    </div>
  );
}