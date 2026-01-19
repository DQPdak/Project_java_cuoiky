'use client';

import React, { useState } from 'react';
import { Building, MapPin, Globe, Users, Edit, Save, X, Upload, Camera } from 'lucide-react';

export default function CompanyPage() {
  const [isEditing, setIsEditing] = useState(false);
  const [companyData, setCompanyData] = useState({
    name: 'TechCorp Vietnam',
    description: 'TechCorp là công ty công nghệ hàng đầu Việt Nam, chuyên cung cấp giải pháp phần mềm cho doanh nghiệp. Chúng tôi cam kết mang đến những sản phẩm chất lượng cao và dịch vụ tốt nhất cho khách hàng.',
    industry: 'Công nghệ thông tin',
    size: '100-500 nhân viên',
    founded: '2015',
    website: 'https://techcorp.vn',
    address: '123 Đường ABC, Quận 1, TP.HCM',
    phone: '+84 28 1234 5678',
    email: 'contact@techcorp.vn',
    logo: '/api/placeholder/150/150',
    coverImage: '/api/placeholder/800/200'
  });

  const [editData, setEditData] = useState(companyData);

  const handleSave = () => {
    setCompanyData(editData);
    setIsEditing(false);
  };

  const handleCancel = () => {
    setEditData(companyData);
    setIsEditing(false);
  };

  const stats = [
    { label: 'Tin tuyển dụng đang hoạt động', value: '5', icon: Building },
    { label: 'Tổng ứng viên đã nhận', value: '127', icon: Users },
    { label: 'Tỷ lệ điền form', value: '68%', icon: Globe },
    { label: 'Thời gian trung bình tuyển dụng', value: '24 ngày', icon: MapPin },
  ];

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý Công ty</h1>
          <p className="text-gray-500">Cập nhật thông tin và thương hiệu công ty của bạn.</p>
        </div>
        {!isEditing ? (
          <button
            onClick={() => setIsEditing(true)}
            className="flex items-center bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition shadow-sm font-medium"
          >
            <Edit className="w-4 h-4 mr-2" /> Chỉnh sửa
          </button>
        ) : (
          <div className="flex gap-2">
            <button
              onClick={handleSave}
              className="flex items-center bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition shadow-sm font-medium"
            >
              <Save className="w-4 h-4 mr-2" /> Lưu
            </button>
            <button
              onClick={handleCancel}
              className="flex items-center bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition shadow-sm font-medium"
            >
              <X className="w-4 h-4 mr-2" /> Hủy
            </button>
          </div>
        )}
      </div>

      {/* Company Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        {stats.map((stat, index) => (
          <div key={index} className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center">
            <div className="p-4 rounded-lg bg-blue-50 text-blue-600 mr-4">
              <stat.icon size={24} />
            </div>
            <div>
              <p className="text-sm font-medium text-gray-500">{stat.label}</p>
              <h3 className="text-2xl font-bold text-gray-900">{stat.value}</h3>
            </div>
          </div>
        ))}
      </div>

      {/* Company Profile */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {/* Cover Image */}
        <div className="relative h-48 bg-gradient-to-r from-blue-500 to-purple-600">
          <img
            src={companyData.coverImage}
            alt="Company cover"
            className="w-full h-full object-cover"
          />
          {isEditing && (
            <button className="absolute top-4 right-4 bg-white/20 backdrop-blur-sm text-white p-2 rounded-lg hover:bg-white/30 transition">
              <Camera className="w-5 h-5" />
            </button>
          )}
        </div>

        <div className="p-6">
          {/* Logo and Basic Info */}
          <div className="flex items-start gap-6 mb-6">
            <div className="relative">
              <div className="w-24 h-24 rounded-lg bg-gray-200 flex items-center justify-center overflow-hidden">
                <img
                  src={companyData.logo}
                  alt="Company logo"
                  className="w-full h-full object-cover"
                />
              </div>
              {isEditing && (
                <button className="absolute -bottom-2 -right-2 bg-blue-600 text-white p-2 rounded-full hover:bg-blue-700 transition">
                  <Upload className="w-4 h-4" />
                </button>
              )}
            </div>
            <div className="flex-1">
              {isEditing ? (
                <div className="space-y-4">
                  <input
                    type="text"
                    value={editData.name}
                    onChange={(e) => setEditData({...editData, name: e.target.value})}
                    className="text-2xl font-bold text-gray-900 border border-gray-300 rounded-lg px-3 py-2 w-full"
                    placeholder="Tên công ty"
                  />
                  <textarea
                    value={editData.description}
                    onChange={(e) => setEditData({...editData, description: e.target.value})}
                    className="text-gray-600 border border-gray-300 rounded-lg px-3 py-2 w-full h-24 resize-none"
                    placeholder="Mô tả công ty"
                  />
                </div>
              ) : (
                <div>
                  <h2 className="text-2xl font-bold text-gray-900 mb-2">{companyData.name}</h2>
                  <p className="text-gray-600">{companyData.description}</p>
                </div>
              )}
            </div>
          </div>

          {/* Company Details */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Ngành nghề</label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editData.industry}
                    onChange={(e) => setEditData({...editData, industry: e.target.value})}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                ) : (
                  <p className="text-gray-900">{companyData.industry}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Quy mô</label>
                {isEditing ? (
                  <select
                    value={editData.size}
                    onChange={(e) => setEditData({...editData, size: e.target.value})}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  >
                    <option>1-10 nhân viên</option>
                    <option>11-50 nhân viên</option>
                    <option>51-100 nhân viên</option>
                    <option>100-500 nhân viên</option>
                    <option>500+ nhân viên</option>
                  </select>
                ) : (
                  <p className="text-gray-900">{companyData.size}</p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Năm thành lập</label>
                {isEditing ? (
                  <input
                    type="number"
                    value={editData.founded}
                    onChange={(e) => setEditData({...editData, founded: e.target.value})}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                ) : (
                  <p className="text-gray-900">{companyData.founded}</p>
                )}
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Website</label>
                {isEditing ? (
                  <input
                    type="url"
                    value={editData.website}
                    onChange={(e) => setEditData({...editData, website: e.target.value})}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2"
                  />
                ) : (
                  <a href={companyData.website} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:text-blue-700">
                    {companyData.website}
                  </a>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Địa chỉ</label>
                {isEditing ? (
                  <textarea
                    value={editData.address}
                    onChange={(e) => setEditData({...editData, address: e.target.value})}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 h-20 resize-none"
                  />
                ) : (
                  <p className="text-gray-900">{companyData.address}</p>
                )}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Số điện thoại</label>
                  {isEditing ? (
                    <input
                      type="tel"
                      value={editData.phone}
                      onChange={(e) => setEditData({...editData, phone: e.target.value})}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  ) : (
                    <p className="text-gray-900">{companyData.phone}</p>
                  )}
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                  {isEditing ? (
                    <input
                      type="email"
                      value={editData.email}
                      onChange={(e) => setEditData({...editData, email: e.target.value})}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2"
                    />
                  ) : (
                    <p className="text-gray-900">{companyData.email}</p>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
