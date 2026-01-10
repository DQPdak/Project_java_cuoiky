'use client';

import React, { useState, useEffect } from 'react';
import api from '@/services/api';
import { Search, Lock, Unlock, MoreVertical, Shield } from 'lucide-react';

interface UserData {
  id: number;
  fullName: string;
  email: string;
  role: string;
  status: 'ACTIVE' | 'BANNED' | 'PENDING';
  createdAt: string;
}

export default function UserManagementPage() {
  const [users, setUsers] = useState<UserData[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  // Mock data tạm thời nếu chưa có API thật
  const fetchUsers = async () => {
    try {
      setLoading(true);
      // const res = await api.get('/admin/users'); 
      // setUsers(res.data.data);
      
      // Giả lập data để bạn thấy UI
      setTimeout(() => {
        setUsers([
          { id: 1, fullName: 'Nguyễn Văn A', email: 'a@gmail.com', role: 'CANDIDATE', status: 'ACTIVE', createdAt: '2024-01-01' },
          { id: 2, fullName: 'Công ty Tech', email: 'hr@tech.com', role: 'RECRUITER', status: 'ACTIVE', createdAt: '2024-01-02' },
          { id: 3, fullName: 'Spammer', email: 'spam@mail.com', role: 'CANDIDATE', status: 'BANNED', createdAt: '2024-01-05' },
        ]);
        setLoading(false);
      }, 500);
    } catch (error) {
      console.error(error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleToggleStatus = async (id: number, currentStatus: string) => {
    if(!confirm('Bạn có chắc muốn thay đổi trạng thái user này?')) return;
    // Gọi API update status
    // await api.put(`/admin/users/${id}/status`, { status: currentStatus === 'ACTIVE' ? 'BANNED' : 'ACTIVE' });
    alert(`Đã đổi trạng thái user ${id}`);
    fetchUsers();
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Quản lý người dùng</h1>
        <div className="relative">
          <input
            type="text"
            placeholder="Tìm kiếm..."
            className="pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <Search className="w-5 h-5 text-gray-400 absolute left-3 top-2.5" />
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
        <table className="w-full text-left">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-6 py-4 text-sm font-medium text-gray-500">Người dùng</th>
              <th className="px-6 py-4 text-sm font-medium text-gray-500">Vai trò</th>
              <th className="px-6 py-4 text-sm font-medium text-gray-500">Trạng thái</th>
              <th className="px-6 py-4 text-sm font-medium text-gray-500">Ngày tham gia</th>
              <th className="px-6 py-4 text-sm font-medium text-gray-500 text-right">Hành động</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div>
                    <p className="font-medium text-gray-900">{user.fullName}</p>
                    <p className="text-sm text-gray-500">{user.email}</p>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                    user.role === 'RECRUITER' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'
                  }`}>
                    {user.role}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                    user.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                  }`}>
                    {user.status === 'ACTIVE' ? 'Hoạt động' : 'Đã khóa'}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-500">{user.createdAt}</td>
                <td className="px-6 py-4 text-right">
                  <button 
                    onClick={() => handleToggleStatus(user.id, user.status)}
                    className="p-2 hover:bg-gray-200 rounded-full transition text-gray-500"
                    title={user.status === 'ACTIVE' ? "Khóa tài khoản" : "Mở khóa"}
                  >
                    {user.status === 'ACTIVE' ? <Lock className="w-4 h-4" /> : <Unlock className="w-4 h-4 text-green-600" />}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {loading && <div className="p-4 text-center text-gray-500">Đang tải...</div>}
      </div>
    </div>
  );
}