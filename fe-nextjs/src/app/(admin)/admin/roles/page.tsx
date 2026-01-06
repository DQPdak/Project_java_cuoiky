'use client';

import React, { useState } from 'react';
import { Shield, Plus, Edit, Trash2, CheckCircle, XCircle } from 'lucide-react';

// Mock Data: Danh sách quyền
const ROLES = [
  {
    id: 1,
    name: 'Administrator',
    description: 'Quyền cao nhất, quản lý toàn bộ hệ thống.',
    usersCount: 3,
    permissions: ['all_access', 'manage_users', 'manage_content', 'system_settings'],
    isSystem: true // Không thể xóa
  },
  {
    id: 2,
    name: 'Recruiter',
    description: 'Nhà tuyển dụng, có thể đăng tin và xem hồ sơ ứng viên.',
    usersCount: 156,
    permissions: ['post_job', 'view_candidate', 'manage_applications'],
    isSystem: true
  },
  {
    id: 3,
    name: 'Candidate',
    description: 'Người tìm việc, có thể ứng tuyển và tạo hồ sơ.',
    usersCount: 1240,
    permissions: ['create_profile', 'apply_job', 'view_job'],
    isSystem: true
  },
  {
    id: 4,
    name: 'Content Moderator',
    description: 'Nhân viên kiểm duyệt nội dung tin đăng.',
    usersCount: 5,
    permissions: ['view_job', 'approve_job', 'reject_job'],
    isSystem: false
  }
];

export default function RolesPage() {
  const [roles, setRoles] = useState(ROLES);

  const handleDelete = (id: number) => {
    if (confirm('Bạn có chắc muốn xóa vai trò này?')) {
      setRoles(roles.filter(role => role.id !== id));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Phân quyền hệ thống</h1>
          <p className="text-gray-500 text-sm">Quản lý vai trò và quyền hạn truy cập của người dùng.</p>
        </div>
        <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium flex items-center shadow-sm transition">
          <Plus className="w-4 h-4 mr-2" />
          Thêm vai trò mới
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
        {roles.map((role) => (
          <div key={role.id} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition">
            <div className="flex justify-between items-start mb-4">
              <div className="p-3 bg-blue-50 text-blue-600 rounded-lg">
                <Shield className="w-6 h-6" />
              </div>
              <div className="flex gap-2">
                <button className="p-2 text-gray-400 hover:text-blue-600 transition" title="Chỉnh sửa">
                  <Edit className="w-4 h-4" />
                </button>
                {!role.isSystem && (
                  <button 
                    onClick={() => handleDelete(role.id)}
                    className="p-2 text-gray-400 hover:text-red-600 transition" 
                    title="Xóa"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>

            <h3 className="text-lg font-bold text-gray-800">{role.name}</h3>
            <p className="text-sm text-gray-500 mt-1 h-10 line-clamp-2">{role.description}</p>

            <div className="mt-4 flex items-center text-sm text-gray-600">
              <span className="font-semibold mr-1">{role.usersCount}</span> người dùng đang sở hữu
            </div>

            <div className="mt-6 pt-4 border-t border-gray-100">
              <p className="text-xs font-semibold text-gray-400 uppercase mb-3">Quyền hạn chính</p>
              <div className="flex flex-wrap gap-2">
                {role.permissions.map((perm, index) => (
                  <span key={index} className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded border border-gray-200">
                    {perm.replace('_', ' ')}
                  </span>
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}