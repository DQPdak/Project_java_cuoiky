'use client';

import React, { useEffect, useMemo, useState } from 'react';
import api from '@/services/api';
import { Search, Lock, Unlock } from 'lucide-react';

type UserStatus = 'ACTIVE' | 'BANNED' | 'PENDING_VERIFICATION';
type UserRole = 'ADMIN' | 'CANDIDATE' | 'RECRUITER' | string;

interface UserData {
  id: number;
  fullName: string;
  email: string;
  userRole: UserRole; // khớp AdminUserResponse: role
  status: UserStatus;
  createdAt: string; // LocalDateTime -> string ISO
}

interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // page index
  size: number;
  first: boolean;
  last: boolean;
}

function formatDate(input: string) {
  // input dạng "2024-01-01T10:00:00"
  if (!input) return '';
  return input.replace('T', ' ').slice(0, 16);
}

function statusLabel(status: UserStatus) {
  switch (status) {
    case 'ACTIVE':
      return 'Hoạt động';
    case 'BANNED':
      return 'Đã khóa';
    case 'PENDING_VERIFICATION':
      return 'Chờ xác thực';
    default:
      return status;
  }
}

export default function UserManagementPage() {
  const [users, setUsers] = useState<UserData[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [searchTerm, setSearchTerm] = useState<string>('');

  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10);

  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalElements, setTotalElements] = useState<number>(0);

  const canPrev = page > 0;
  const canNext = page + 1 < totalPages;

  const fetchUsers = async (opts?: { page?: number; size?: number; keyword?: string }) => {
    try {
      setLoading(true);

      const p = opts?.page ?? page;
      const s = opts?.size ?? size;
      const keyword = opts?.keyword ?? searchTerm;

      const res = await api.get<PageResponse<UserData>>('/admin/users', {
        params: {
          keyword: keyword?.trim() || undefined,
          page: p,
          size: s,
          sort: 'createdAt,desc',
        },
      });

      const data = res.data;
      setUsers(data.content ?? []);
      setTotalPages(data.totalPages ?? 0);
      setTotalElements(data.totalElements ?? 0);
      setPage(data.number ?? p);
      setSize(data.size ?? s);
    } catch (err) {
      console.error('fetchUsers error:', err);
      setUsers([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  // load lần đầu
  useEffect(() => {
    fetchUsers({ page: 0 });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // debounce search
  useEffect(() => {
    const t = setTimeout(() => {
      fetchUsers({ page: 0, keyword: searchTerm });
    }, 300);

    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchTerm]);

  const handleToggleStatus = async (u: UserData) => {
    if (!confirm('Bạn có chắc muốn thay đổi trạng thái user này?')) return;

    try {
      // ACTIVE -> lock (BANNED), BANNED -> unlock (ACTIVE)
      if (u.status === 'ACTIVE') {
        await api.put(`/admin/users/${u.id}/lock`);
      } else if (u.status === 'BANNED') {
        await api.put(`/admin/users/${u.id}/unlock`);
      } else {
        alert('User đang ở trạng thái chờ xác thực, không hỗ trợ toggle tại đây.');
        return;
      }

      await fetchUsers(); // refresh list
    } catch (err) {
      console.error('toggle status error:', err);
      alert('Thay đổi trạng thái thất bại (kiểm tra quyền ADMIN / token).');
    }
  };

  const roleBadgeClass = useMemo(
    () => (role: string) =>
      role === 'RECRUITER'
        ? 'bg-purple-100 text-purple-700'
        : role === 'ADMIN'
        ? 'bg-amber-100 text-amber-800'
        : 'bg-blue-100 text-blue-700',
    []
  );

  const statusBadgeClass = useMemo(
    () => (status: UserStatus) =>
      status === 'ACTIVE'
        ? 'bg-green-100 text-green-700'
        : status === 'PENDING_VERIFICATION'
        ? 'bg-yellow-100 text-yellow-800'
        : 'bg-red-100 text-red-700',
    []
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 md:flex-row md:justify-between md:items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Quản lý người dùng</h1>
          <p className="text-sm text-gray-500">
            Tổng: <span className="font-medium">{totalElements}</span>
          </p>
        </div>

        <div className="relative">
          <input
            type="text"
            placeholder="Tìm kiếm tên hoặc email..."
            className="pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none w-72"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <Search className="w-5 h-5 text-gray-400 absolute left-3 top-2.5" />
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
        <div className="overflow-x-auto">
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
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${roleBadgeClass(user.userRole)}`}>
                      {user.userRole}
                    </span>
                  </td>

                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${statusBadgeClass(user.status)}`}>
                      {statusLabel(user.status)}
                    </span>
                  </td>

                  <td className="px-6 py-4 text-sm text-gray-500">{formatDate(user.createdAt)}</td>

                  <td className="px-6 py-4 text-right">
                    <button
                      onClick={() => handleToggleStatus(user)}
                      className="p-2 hover:bg-gray-200 rounded-full transition text-gray-500 disabled:opacity-50"
                      title={user.status === 'ACTIVE' ? 'Khóa tài khoản' : user.status === 'BANNED' ? 'Mở khóa' : 'Không hỗ trợ'}
                      disabled={user.userRole === 'ADMIN'} // tránh khóa admin từ UI
                    >
                      {user.status === 'ACTIVE' ? (
                        <Lock className="w-4 h-4" />
                      ) : user.status === 'BANNED' ? (
                        <Unlock className="w-4 h-4 text-green-600" />
                      ) : (
                        <Lock className="w-4 h-4 opacity-40" />
                      )}
                    </button>
                  </td>
                </tr>
              ))}

              {!loading && users.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                    Không có người dùng nào.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="flex items-center justify-between px-6 py-4 border-t bg-white">
          <div className="text-sm text-gray-500">
            Trang <span className="font-medium">{totalPages === 0 ? 0 : page + 1}</span> /{' '}
            <span className="font-medium">{totalPages}</span>
          </div>

          <div className="flex items-center gap-2">
            <button
              className="px-3 py-2 border rounded-lg text-sm disabled:opacity-50"
              disabled={!canPrev || loading}
              onClick={() => fetchUsers({ page: page - 1 })}
            >
              Trước
            </button>
            <button
              className="px-3 py-2 border rounded-lg text-sm disabled:opacity-50"
              disabled={!canNext || loading}
              onClick={() => fetchUsers({ page: page + 1 })}
            >
              Sau
            </button>
          </div>
        </div>

        {loading && <div className="px-6 py-3 text-center text-gray-500">Đang tải...</div>}
      </div>
    </div>
  );
}
