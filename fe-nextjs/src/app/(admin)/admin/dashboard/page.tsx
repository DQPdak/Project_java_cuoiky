'use client';

import React, { useEffect, useMemo, useState } from 'react';
import api from '@/services/api';
import { useRouter } from 'next/navigation';
import {
  Users,
  Briefcase,
  FileText,
  TrendingUp,
  Activity,
  Calendar,
} from 'lucide-react';
import {
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area,
} from 'recharts';
import { DashboardSummary, ApplicationsByDay } from '@/types/admin';

type RecentActivity = {
  refId: number;
  message: string;
  createdAt: string; // ISO
  timeAgo: string;
};

type JobPostingResponse = {
  id: number;
  title: string;
  description?: string;
  requirements?: string;
  salaryRange?: string;
  location?: string;
  expiryDate?: string;
  status?: string;
  recruiterId?: number;
  recruiterName?: string;
  createdAt?: string;
  updatedAt?: string;
};

type PageLike<T> = {
  content: T[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
};

function normalizePage<T>(raw: any): PageLike<T> {
  const data = raw?.data ?? raw;
  if (Array.isArray(data)) return { content: data };
  if (data?.content && Array.isArray(data.content)) return data as PageLike<T>;
  return { content: [] };
}

function fmtDate(dt?: string) {
  if (!dt) return '-';
  try {
    return new Date(dt).toLocaleString('vi-VN');
  } catch {
    return dt;
  }
}

export default function AdminDashboard() {
  const router = useRouter();

  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [chartData, setChartData] = useState<ApplicationsByDay[]>([]);
  const [recentActivities, setRecentActivities] = useState<RecentActivity[]>([]);
  const [loading, setLoading] = useState(true);

  // Quick action count
  const [pendingPostsCount, setPendingPostsCount] = useState(0);

  // Modal for approval
  const [approveModalOpen, setApproveModalOpen] = useState(false);

  const fetchDashboard = async () => {
    setLoading(true);
    try {
      const [summaryRes, chartRes, recentRes, pendingRes] = await Promise.all([
        api.get('/admin/dashboard/summary'),
        api.get('/admin/dashboard/applications-chart', { params: { days: 7 } }),
        api.get('/admin/dashboard/recent-activities', { params: { limit: 5 } }),
        api.get('/admin/content/pending', { params: { page: 0, size: 1 } }),
      ]);

      setSummary(summaryRes.data?.data ?? null);
      setChartData(chartRes.data?.data ?? []);
      setRecentActivities(recentRes.data?.data ?? []);

      const pendingPage = normalizePage<JobPostingResponse>(pendingRes.data);
      setPendingPostsCount(pendingPage.totalElements ?? 0);
    } catch (err) {
      console.error('Lỗi tải dữ liệu Dashboard:', err);
      setSummary(null);
      setChartData([]);
      setRecentActivities([]);
      setPendingPostsCount(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
        <button
          type="button"
          className="bg-white border border-gray-300 px-4 py-2 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 shadow-sm flex items-center gap-2"
        >
          <Calendar className="w-4 h-4" />
          Hôm nay: {new Date().toLocaleDateString('vi-VN')}
        </button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Ứng viên"
          value={summary?.totalCandidates ?? 0}
          icon={<Users className="w-6 h-6 text-blue-600" />}
          trend="+12% so với tháng trước"
          color="bg-blue-50"
        />
        <StatCard
          title="Nhà tuyển dụng"
          value={summary?.totalRecruiters ?? 0}
          icon={<Briefcase className="w-6 h-6 text-purple-600" />}
          trend="+5% so với tháng trước"
          color="bg-purple-50"
        />
        <StatCard
          title="Tin tuyển dụng"
          value={summary?.totalActiveJobs ?? 0}
          icon={<FileText className="w-6 h-6 text-green-600" />}
          trend="Tin đang mở"
          color="bg-green-50"
        />
        <StatCard
          title="Lượt ứng tuyển"
          value={summary?.totalApplications ?? 0}
          icon={<TrendingUp className="w-6 h-6 text-orange-600" />}
          trend="7 ngày gần đây"
          color="bg-orange-50"
        />
      </div>

      {/* Charts + Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Chart */}
        <div className="lg:col-span-2 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2">
              <Activity className="w-5 h-5 text-blue-500" />
              Biểu đồ Ứng tuyển (7 ngày qua)
            </h3>
            <button
              type="button"
              onClick={fetchDashboard}
              className="text-sm text-blue-600 hover:underline"
            >
              Tải lại
            </button>
          </div>

          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="colorCount" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.8} />
                    <stop offset="95%" stopColor="#3B82F6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#fff',
                    borderRadius: '8px',
                    border: 'none',
                    boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)',
                  }}
                />
                <Area type="monotone" dataKey="count" stroke="#3B82F6" fillOpacity={1} fill="url(#colorCount)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* ✅ Recent activities (fixed footer button) */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-gray-800">Hoạt động gần đây</h3>
            <button
              type="button"
              onClick={fetchDashboard}
              className="text-sm text-blue-600 hover:underline"
            >
              Tải lại
            </button>
          </div>

          {/* Body co giãn để nút luôn dính đáy */}
          <div className="flex-1 min-h-[220px]">
            {recentActivities.length === 0 ? (
              <div className="text-sm text-gray-500">
                Chưa có hoạt động ứng tuyển gần đây.
              </div>
            ) : (
              <div className="space-y-4">
                {recentActivities.map((a) => (
                  <div
                    key={a.refId}
                    className="flex items-start gap-3 pb-3 border-b border-gray-50 last:border-0 last:pb-0"
                  >
                    <div className="w-2 h-2 mt-2 rounded-full bg-blue-500" />
                    <div>
                      <p className="text-sm text-gray-800 font-medium">{a.message}</p>
                      <p className="text-xs text-gray-500">{a.timeAgo}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* ✅ Footer dính đáy + chuyển trang */}
          <button
            type="button"
            className="w-full text-center text-sm text-blue-600 font-medium mt-4 hover:underline pt-4 border-t border-gray-100"
            onClick={() => router.push('/admin/activities')}
          >
            Xem tất cả hoạt động
          </button>
        </div>
      </div>

      {/* Quick actions */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100">
          <h3 className="font-bold text-gray-800">Quản lý nhanh</h3>
        </div>
        <div className="p-6 grid grid-cols-1 md:grid-cols-3 gap-4">
          <ActionButton
            label="Phê duyệt bài đăng"
            count={pendingPostsCount}
            color="text-yellow-600 bg-yellow-50"
            onClick={() => setApproveModalOpen(true)}
          />
          <ActionButton
            label="Báo cáo vi phạm"
            count={2}
            color="text-red-600 bg-red-50"
            onClick={() => alert('TODO: mở modal báo cáo vi phạm')}
          />
          <ActionButton
            label="Yêu cầu hỗ trợ"
            count={0}
            color="text-gray-600 bg-gray-50"
            onClick={() => alert('TODO: mở modal yêu cầu hỗ trợ')}
          />
        </div>
      </div>

      {/* Modal phê duyệt tin (giữ như bản bạn đang dùng) */}
      <ApproveJobPostingModal
        open={approveModalOpen}
        onClose={() => setApproveModalOpen(false)}
        onAfterAction={async () => {
          await fetchDashboard();
        }}
      />
    </div>
  );
}

/** ====== MODAL (giữ nguyên phần bạn đang có) ====== */
function ApproveJobPostingModal({
  open,
  onClose,
  onAfterAction,
}: {
  open: boolean;
  onClose: () => void;
  onAfterAction: () => Promise<void> | void;
}) {
  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<JobPostingResponse[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState<number | undefined>(undefined);

  const [selected, setSelected] = useState<JobPostingResponse | null>(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  const fetchPending = async (p = page, s = size) => {
    setLoading(true);
    try {
      const res = await api.get('/admin/content/pending', { params: { page: p, size: s } });
      const pageData = normalizePage<JobPostingResponse>(res.data);
      setRows(pageData.content ?? []);
      setTotalPages(pageData.totalPages ?? 1);
      setTotalElements(pageData.totalElements);
      setPage(pageData.number ?? p);
      setSize(pageData.size ?? s);
    } catch (e) {
      console.error('Lỗi tải pending:', e);
      setRows([]);
      setTotalPages(1);
      setTotalElements(undefined);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open) {
      fetchPending(0, size);
    } else {
      // reset state khi đóng modal
      setRows([]);
      setSelected(null);
      setDrawerOpen(false);
      setPage(0);
      setTotalPages(1);
      setTotalElements(undefined);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  const closeDrawer = () => {
    setDrawerOpen(false);
    setSelected(null);
  };

  const openDetail = (jp: JobPostingResponse) => {
    setSelected(jp);
    setDrawerOpen(true);
  };

  const approve = async () => {
    if (!selected?.id) return;
    setActionLoading(true);
    try {
      await api.put(`/admin/content/posts/${selected.id}/approve`);
      closeDrawer();
      await fetchPending(page, size);
      await onAfterAction();
    } catch (e: any) {
      alert(e?.response?.data?.message || 'Duyệt thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  const reject = async () => {
    if (!selected?.id) return;

    const ok = confirm('Bạn chắc chắn muốn TỪ CHỐI tin này?');
    if (!ok) return;

    setActionLoading(true);
    try {
      // Reject 1 click - không lý do
      await api.put(`/admin/content/posts/${selected.id}/reject`);
      closeDrawer();
      await fetchPending(page, size);
      await onAfterAction();
    } catch (e: any) {
      alert(e?.response?.data?.message || 'Từ chối thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  const paginationText = useMemo(() => {
    if (typeof totalElements === 'number') {
      const from = page * size + 1;
      const to = Math.min((page + 1) * size, totalElements);
      return `Hiển thị ${from}-${to} / ${totalElements}`;
    }
    return `Trang ${page + 1} / ${totalPages}`;
  }, [page, size, totalPages, totalElements]);

  if (!open) return null;

  return (
    <>
      {/* Overlay */}
      <div className="fixed inset-0 bg-black/40 z-40" onClick={onClose} />

      {/* Modal */}
      <div className="fixed inset-0 z-50 flex items-start justify-center p-4 sm:p-8">
        <div
          className="relative w-full max-w-5xl bg-white rounded-2xl shadow-xl border overflow-hidden"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="px-6 py-4 border-b flex items-center justify-between">
            <div>
              <div className="text-lg font-bold text-gray-900">Phê duyệt bài đăng</div>
              <div className="text-sm text-gray-500">
                Click 1 tin để mở Drawer chi tiết bên phải.
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => fetchPending(page, size)}
                className="px-3 py-2 rounded-lg border bg-white hover:bg-gray-50 text-sm"
              >
                Tải lại
              </button>
              <button
                type="button"
                onClick={onClose}
                className="px-3 py-2 rounded-lg border bg-white hover:bg-gray-50 text-sm"
              >
                Đóng
              </button>
            </div>
          </div>

          {/* Body */}
          <div className="relative">
            <div className="p-6">
              <div className="flex items-center justify-between mb-3">
                <div className="text-sm text-gray-600">{paginationText}</div>

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    disabled={page <= 0 || loading}
                    onClick={() => fetchPending(page - 1, size)}
                    className="px-3 py-1.5 rounded-lg border bg-white text-sm disabled:opacity-40"
                  >
                    ← Trước
                  </button>
                  <button
                    type="button"
                    disabled={page >= totalPages - 1 || loading}
                    onClick={() => fetchPending(page + 1, size)}
                    className="px-3 py-1.5 rounded-lg border bg-white text-sm disabled:opacity-40"
                  >
                    Sau →
                  </button>

                  <select
                    value={size}
                    onChange={(e) => {
                      const newSize = Number(e.target.value);
                      setSize(newSize);
                      fetchPending(0, newSize);
                    }}
                    className="px-3 py-2 rounded-lg bg-white border text-sm"
                  >
                    {[5, 10, 20, 50].map((n) => (
                      <option key={n} value={n}>
                        {n}/trang
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {loading ? (
                <div className="p-6 text-sm text-gray-500">Đang tải danh sách...</div>
              ) : rows.length === 0 ? (
                <div className="p-6 text-sm text-gray-500">Không có tin nào đang chờ duyệt.</div>
              ) : (
                <div className="overflow-x-auto border rounded-xl">
                  <table className="min-w-full text-sm">
                    <thead className="bg-gray-50 text-gray-600">
                      <tr>
                        <th className="text-left px-4 py-3 font-semibold">Tiêu đề</th>
                        <th className="text-left px-4 py-3 font-semibold">Địa điểm</th>
                        <th className="text-left px-4 py-3 font-semibold">Lương</th>
                        <th className="text-left px-4 py-3 font-semibold">Recruiter</th>
                        <th className="text-left px-4 py-3 font-semibold">Ngày tạo</th>
                        <th className="text-right px-4 py-3 font-semibold">Trạng thái</th>
                      </tr>
                    </thead>
                    <tbody>
                      {rows.map((r) => (
                        <tr
                          key={r.id}
                          onClick={() => openDetail(r)}
                          className="border-t hover:bg-gray-50 cursor-pointer"
                        >
                          <td className="px-4 py-3">
                            <div className="font-medium text-gray-900 line-clamp-1">{r.title}</div>
                            <div className="text-xs text-gray-500">ID: {r.id}</div>
                          </td>
                          <td className="px-4 py-3 text-gray-700">{r.location || '-'}</td>
                          <td className="px-4 py-3 text-gray-700">{r.salaryRange || '-'}</td>
                          <td className="px-4 py-3 text-gray-700">{r.recruiterName || '-'}</td>
                          <td className="px-4 py-3 text-gray-600">{fmtDate(r.createdAt)}</td>
                          <td className="px-4 py-3 text-right">
                            <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-yellow-50 text-yellow-700 border border-yellow-100">
                              {r.status || 'PENDING'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            {/* Drawer */}
            {drawerOpen && (
              <>
                <div className="fixed inset-0 z-50" onClick={closeDrawer} />

                <div className="absolute top-0 right-0 h-full w-full sm:w-[520px] bg-white z-[60] shadow-2xl border-l flex flex-col">
                  <div className="p-5 border-b flex items-start justify-between">
                    <div>
                      <div className="text-xs text-gray-500">Chi tiết tin</div>
                      <div className="text-lg font-bold text-gray-900">{selected?.title}</div>
                      <div className="text-xs text-gray-500 mt-1">
                        ID: {selected?.id} • Tạo lúc: {fmtDate(selected?.createdAt)}
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={closeDrawer}
                      className="px-3 py-1.5 rounded-lg border hover:bg-gray-50 text-sm"
                    >
                      Đóng
                    </button>
                  </div>

                  <div className="p-5 overflow-y-auto space-y-4 flex-1">
                    <InfoRow label="Địa điểm" value={selected?.location || '-'} />
                    <InfoRow label="Mức lương" value={selected?.salaryRange || '-'} />
                    <InfoRow label="Hạn tin" value={selected?.expiryDate ? fmtDate(selected.expiryDate) : '-'} />
                    <InfoRow label="Recruiter" value={selected?.recruiterName || '-'} />
                    <InfoRow label="Trạng thái" value={selected?.status || 'PENDING'} />

                    <Section title="Mô tả">
                      <div className="text-sm text-gray-700 whitespace-pre-wrap">
                        {selected?.description || '(Không có mô tả)'}
                      </div>
                    </Section>

                    <Section title="Yêu cầu">
                      <div className="text-sm text-gray-700 whitespace-pre-wrap">
                        {selected?.requirements || '(Không có yêu cầu)'}
                      </div>
                    </Section>
                  </div>

                  <div className="p-5 border-t flex items-center justify-between gap-3">
                    <button
                      type="button"
                      onClick={reject}
                      disabled={actionLoading}
                      className="px-4 py-2 rounded-lg border text-red-600 hover:bg-red-50 disabled:opacity-60"
                    >
                      {actionLoading ? 'Đang xử lý...' : 'Từ chối'}
                    </button>

                    <button
                      type="button"
                      onClick={approve}
                      disabled={actionLoading}
                      className="px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-60"
                    >
                      {actionLoading ? 'Đang xử lý...' : 'Duyệt tin'}
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>

          <div className="px-6 py-3 border-t text-xs text-gray-500">
            Tip: Duyệt/Từ chối ngay trong Drawer mà không rời Dashboard.
          </div>
        </div>
      </div>
    </>
  );
}

function InfoRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-4">
      <div className="text-sm text-gray-500">{label}</div>
      <div className="text-sm text-gray-900 font-medium text-right max-w-[70%]">
        {value}
      </div>
    </div>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="border rounded-xl p-4">
      <div className="text-sm font-semibold text-gray-900 mb-2">{title}</div>
      {children}
    </div>
  );
}


function StatCard({ title, value, icon, trend, color }: any) {
  return (
    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
      <div className="flex justify-between items-start mb-4">
        <div>
          <p className="text-sm font-medium text-gray-500">{title}</p>
          <h3 className="text-3xl font-bold text-gray-800 mt-1">{value}</h3>
        </div>
        <div className={`p-3 rounded-lg ${color}`}>{icon}</div>
      </div>
      <p className="text-xs text-gray-500 flex items-center gap-1">
        {String(trend).includes('+') ? (
          <span className="text-green-600 font-medium bg-green-50 px-1 rounded">{trend}</span>
        ) : (
          <span className="text-gray-600">{trend}</span>
        )}
      </p>
    </div>
  );
}

function ActionButton({ label, count, color, onClick }: any) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex items-center justify-between p-4 rounded-lg border border-transparent hover:border-gray-200 transition-all ${color}`}
    >
      <span className="font-medium">{label}</span>
      {count > 0 && (
        <span className="bg-white px-2 py-1 rounded-full text-xs font-bold shadow-sm">
          {count}
        </span>
      )}
    </button>
  );
}
