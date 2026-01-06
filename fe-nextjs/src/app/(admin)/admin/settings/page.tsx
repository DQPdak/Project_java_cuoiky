'use client';

import React, { useState } from 'react';
import { Save, Lock, Bell, Globe, Mail } from 'lucide-react';

export default function SettingsPage() {
  const [activeTab, setActiveTab] = useState('GENERAL');
  const [loading, setLoading] = useState(false);

  // Mock Form State
  const [config, setConfig] = useState({
    siteName: 'CareerMate',
    supportEmail: 'support@careermate.com',
    maintenanceMode: false,
    emailNotifications: true,
    newJobApproval: true, // Tin tuyển dụng mới cần duyệt?
  });

  const handleSave = () => {
    setLoading(true);
    // Giả lập gọi API
    setTimeout(() => {
      setLoading(false);
      alert('Đã lưu cài đặt thành công!');
    }, 1000);
  };

  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Cài đặt hệ thống</h1>

      <div className="flex flex-col md:flex-row gap-6">
        {/* Sidebar Menu Cài đặt */}
        <div className="w-full md:w-64 flex-shrink-0">
          <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
            <button
              onClick={() => setActiveTab('GENERAL')}
              className={`w-full flex items-center px-4 py-3 text-sm font-medium transition ${
                activeTab === 'GENERAL' ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' : 'text-gray-600 hover:bg-gray-50'
              }`}
            >
              <Globe className="w-5 h-5 mr-3" /> Chung
            </button>
            <button
              onClick={() => setActiveTab('SECURITY')}
              className={`w-full flex items-center px-4 py-3 text-sm font-medium transition ${
                activeTab === 'SECURITY' ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' : 'text-gray-600 hover:bg-gray-50'
              }`}
            >
              <Lock className="w-5 h-5 mr-3" /> Bảo mật & Quyền
            </button>
            <button
              onClick={() => setActiveTab('NOTIFICATIONS')}
              className={`w-full flex items-center px-4 py-3 text-sm font-medium transition ${
                activeTab === 'NOTIFICATIONS' ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' : 'text-gray-600 hover:bg-gray-50'
              }`}
            >
              <Bell className="w-5 h-5 mr-3" /> Thông báo
            </button>
          </div>
        </div>

        {/* Content Area */}
        <div className="flex-1">
          <div className="bg-white rounded-xl shadow-sm border p-6">
            
            {/* TAB: GENERAL */}
            {activeTab === 'GENERAL' && (
              <div className="space-y-6 animate-fadeIn">
                <h3 className="text-lg font-bold text-gray-800 border-b pb-2">Thông tin chung</h3>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Tên ứng dụng</label>
                  <input
                    type="text"
                    value={config.siteName}
                    onChange={(e) => setConfig({...config, siteName: e.target.value})}
                    className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email hỗ trợ</label>
                  <div className="flex items-center border rounded-lg px-3 py-2 bg-gray-50">
                    <Mail className="w-4 h-4 text-gray-500 mr-2" />
                    <input
                      type="email"
                      value={config.supportEmail}
                      onChange={(e) => setConfig({...config, supportEmail: e.target.value})}
                      className="w-full bg-transparent outline-none text-sm"
                    />
                  </div>
                </div>

                <div className="flex items-center justify-between p-4 bg-yellow-50 rounded-lg border border-yellow-100">
                  <div>
                    <span className="font-medium text-yellow-800 block">Chế độ bảo trì</span>
                    <span className="text-xs text-yellow-600">Chỉ Admin mới có thể truy cập hệ thống khi bật.</span>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" className="sr-only peer" checked={config.maintenanceMode} onChange={(e) => setConfig({...config, maintenanceMode: e.target.checked})} />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-yellow-500"></div>
                  </label>
                </div>
              </div>
            )}

            {/* TAB: SECURITY */}
            {activeTab === 'SECURITY' && (
              <div className="space-y-6 animate-fadeIn">
                <h3 className="text-lg font-bold text-gray-800 border-b pb-2">Cấu hình Bảo mật</h3>
                
                <div className="flex items-center justify-between">
                  <div>
                    <span className="font-medium text-gray-800 block">Kiểm duyệt tin tuyển dụng</span>
                    <span className="text-sm text-gray-500">Yêu cầu Admin duyệt trước khi tin được hiển thị công khai.</span>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" className="sr-only peer" checked={config.newJobApproval} onChange={(e) => setConfig({...config, newJobApproval: e.target.checked})} />
                    <div className="w-11 h-6 bg-gray-200 rounded-full peer peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>

                <div className="pt-4 border-t">
                  <button className="text-blue-600 hover:text-blue-700 text-sm font-medium">
                    Đổi mật khẩu Admin
                  </button>
                </div>
              </div>
            )}

            {/* TAB: NOTIFICATIONS */}
            {activeTab === 'NOTIFICATIONS' && (
              <div className="space-y-6 animate-fadeIn">
                <h3 className="text-lg font-bold text-gray-800 border-b pb-2">Cài đặt Thông báo</h3>
                 <div className="flex items-center justify-between">
                  <div>
                    <span className="font-medium text-gray-800 block">Email thông báo hệ thống</span>
                    <span className="text-sm text-gray-500">Nhận email khi có người dùng mới hoặc báo cáo vi phạm.</span>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" className="sr-only peer" checked={config.emailNotifications} onChange={(e) => setConfig({...config, emailNotifications: e.target.checked})} />
                    <div className="w-11 h-6 bg-gray-200 rounded-full peer peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                  </label>
                </div>
              </div>
            )}

            {/* Save Button */}
            <div className="mt-8 pt-6 border-t flex justify-end">
              <button
                onClick={handleSave}
                disabled={loading}
                className="flex items-center bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-medium transition disabled:opacity-50"
              >
                {loading ? 'Đang lưu...' : (
                  <>
                    <Save className="w-4 h-4 mr-2" /> Lưu thay đổi
                  </>
                )}
              </button>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}