'use client';

import React, { useEffect, useMemo, useState } from 'react';
import api from '@/services/api';
import { useRouter, useSearchParams } from 'next/navigation';

type ViolationReportResponse = {
  id: number;
  reporterId: number;
  reporterName: string;

  targetType: 'JOB_POSTING' | 'COMPANY' | 'USER';
  targetId: number;

  reason:
    | 'SPAM'
    | 'SCAM'
    | 'FAKE_INFORMATION'
    | 'HARASSMENT'
    | 'INAPPROPRIATE_CONTENT'
    | 'OTHER';
  description?: string;
  evidenceUrl?: string;

  status: 'PENDING' | 'VALID' | 'INVALID' | 'RESOLVED';

  handledById?: number | null;
  handledByName?: string | null;
  adminNote?: string | null;

  createdAt?: string;
  updatedAt?: string;
  handledAt?: string | null;
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
  // nếu BE trả { success, data: { content... } }
  if (data?.data?.content && Array.isArray(data.data.content)) return data.data as PageLike<T>;
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

export default function ViolationReportsPage() {
  const router = useRouter();
  const sp = useSearchParams();

  // Lấy status từ query: /admin/violation-reports?status=PENDING
  const urlStatus = (sp.get('status') || 'PENDING').toUpperCase();
  const initialStatus: ViolationReportResponse['status'] =
    urlStatus === 'VALID' || urlStatus === 'INVALID' || urlStatus === 'RESOLVED'
      ? (urlStatus as any)
      : 'PENDING';

  const [status, setStatus] = useState<ViolationReportResponse['status']>(initialStatus);

  const [loading, setLoading] = useState(false);
  const [rows, setRows] = useState<ViolationReportResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState<number | undefined>(undefined);

  const [selected, setSelected] = useState<ViolationReportResponse | null>(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);


  const PAGE_SIZE = 10;
  const [size] = useState(PAGE_SIZE);

  const fetchReports = async (p = page, st = status) => {
  setLoading(true);
  try {
    const res = await api.get('/admin/violation-reports', {
      params: { status: st, page: p, size: PAGE_SIZE, sort: 'createdAt,desc' },
    });
    const pageData = normalizePage<ViolationReportResponse>(res.data);
    setRows(pageData.content ?? []);
    setTotalPages(pageData.totalPages ?? 1);
    setTotalElements(pageData.totalElements);
    setPage(pageData.number ?? p);
  } catch (e) {
    console.error('Lỗi tải violation reports:', e);
    setRows([]);
    setTotalPages(1);
    setTotalElements(undefined);
  } finally {
    setLoading(false);
  }
};

  useEffect(() => {
    // khi mới vào page hoặc đổi query status
    setStatus(initialStatus);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [urlStatus]);

  useEffect(() => {
    // đổi status => reset selection + fetch trang 0
    setSelected(null);
    setDrawerOpen(false);
    fetchReports(0, status);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status]);

  const openDetail = (r: ViolationReportResponse) => {
    setSelected(r);
    setDrawerOpen(true);
  };

  const closeDrawer = () => {
    setDrawerOpen(false);
    setSelected(null);
  };

  // ⚠️ Tạm thời vẫn dùng adminId=5 giống code cũ của bạn
  // Sau này backend lấy từ token thì xoá params adminId.
  const updateStatus = async (reportId: number, next: 'VALID' | 'INVALID') => {
    setActionLoading(true);
    try {
      await api.patch(
        `/admin/violation-reports/${reportId}/status`,
        { status: next, adminNote: next === 'VALID' ? 'Xác nhận vi phạm' : 'Từ chối báo cáo' },
        { params: { adminId: 5 } }
      );
      closeDrawer();
      await fetchReports(page, status);
    } catch (e: any) {
      alert(e?.response?.data?.message || 'Cập nhật trạng thái thất bại');
    } finally {
      setActionLoading(false);
    }
  };

const paginationText = useMemo(() => {
  const total = totalElements ?? 0;

  if (total === 0) return 'Hiển thị 0-0 / 0';

  const from = page * PAGE_SIZE + 1;
  const to = Math.min((page + 1) * PAGE_SIZE, total);

  return `Hiển thị ${from}-${to} / ${total}`;
}, [page, totalElements]);

  const statusLabel = (s: ViolationReportResponse['status']) => {
    if (s === 'PENDING') return 'Chờ xử lý';
    if (s === 'VALID') return 'Hợp lệ';
    if (s === 'INVALID') return 'Không hợp lệ';
    return 'Đã xử lý';
  };

  const setStatusAndUrl = (s: ViolationReportResponse['status']) => {
    router.push(`/admin/violation-reports?status=${s}`);
    // setStatus sẽ được sync qua useEffect (urlStatus change)
  };

  return (
    <div className="min-h-screen bg-gray-50 p-6 space-y-6">
      {/* Header page */}
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Báo cáo vi phạm</h1>
          <p className="text-gray-500">
            Danh sách báo cáo. Click 1 dòng để xem chi tiết.
          </p>
        </div>

        <div className="flex items-center gap-3 flex-wrap justify-end">
          {/* Tabs status */}
          <div className="flex space-x-2 bg-white p-1 rounded-lg border">
            {(['PENDING', 'VALID', 'INVALID', 'RESOLVED'] as const).map((s) => (
              <button
                key={s}
                type="button"
                onClick={() => setStatusAndUrl(s)}
                className={`px-4 py-2 text-sm font-medium rounded-md transition ${
                  status === s ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:bg-gray-50'
                }`}
              >
                {statusLabel(s)}
              </button>
            ))}
          </div>

          <button
            type="button"
            onClick={() => fetchReports(page, status)}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 shadow-sm flex items-center gap-2"
          >
            ↻ Tải lại
          </button>

          <button
            type="button"
            onClick={() => router.back()}
            className="bg-white border border-gray-300 px-4 py-2 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 shadow-sm"
          >
            ← Quay lại
          </button>
        </div>
      </div>

      {/* Body: table + drawer */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Table */}
        <div className="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="px-6 py-4 border-b flex items-center justify-between">
            <div className="text-sm text-gray-600">{paginationText}</div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                disabled={page <= 0 || loading}
                onClick={() => fetchReports(page - 1, status)}
                className="px-3 py-1.5 rounded-lg border bg-white text-sm disabled:opacity-40"
              >
                ← Trước
              </button>
              <button
                type="button"
                disabled={page >= totalPages - 1 || loading}
                onClick={() => fetchReports(page + 1, status)}
                className="px-3 py-1.5 rounded-lg border bg-white text-sm disabled:opacity-40"
              >
                Sau →
              </button>
                <div className="px-3 py-2 rounded-lg bg-white border text-sm text-gray-700">
                    {PAGE_SIZE}/trang
                </div>
            </div>
          </div>

          {loading ? (
            <div className="p-6 text-sm text-gray-500">Đang tải danh sách...</div>
          ) : rows.length === 0 ? (
            <div className="p-10 text-center text-sm text-gray-500">
              Không có báo cáo nào.
            </div>
          ) : (
            <div className="overflow-x-auto">
                <div className="max-h-[520px] overflow-y-auto">
                    <table className="min-w-full text-sm">
                        <thead className="bg-gray-50 text-gray-600 sticky top-0 z-10">
                        <tr>
                            <th className="text-left px-4 py-3 font-semibold">ID</th>
                            <th className="text-left px-4 py-3 font-semibold">Lý do</th>
                            <th className="text-left px-4 py-3 font-semibold">Target</th>
                            <th className="text-left px-4 py-3 font-semibold">Reporter</th>
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
                                <div className="font-medium text-gray-900">#{r.id}</div>
                            </td>
                            <td className="px-4 py-3 text-gray-700">{r.reason}</td>
                            <td className="px-4 py-3 text-gray-700">
                                {r.targetType} (ID: {r.targetId})
                            </td>
                            <td className="px-4 py-3 text-gray-700">
                                {r.reporterName} (ID: {r.reporterId})
                            </td>
                            <td className="px-4 py-3 text-gray-600">{fmtDate(r.createdAt)}</td>
                            <td className="px-4 py-3 text-right">
                                <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-100">
                                {r.status}
                                </span>
                            </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
          )}

          <div className="px-6 py-3 border-t text-xs text-gray-500 b-1">
            Tip: Dùng bộ lọc trạng thái ở header để xem VALID/INVALID/RESOLVED.
          </div>
        </div>

        {/* Drawer (cột phải) */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          {!drawerOpen || !selected ? (
            <div className="p-6 text-sm text-gray-500">
              Chọn một báo cáo bên trái để xem chi tiết.
            </div>
          ) : (
            <div className="flex flex-col h-full">
              <div className="p-5 border-b flex items-start justify-between">
                <div>
                  <div className="text-xs text-gray-500">Chi tiết báo cáo</div>
                  <div className="text-lg font-bold text-gray-900">Report #{selected.id}</div>
                  <div className="text-xs text-gray-500 mt-1">
                    Tạo lúc: {fmtDate(selected.createdAt)}
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
                <InfoRow label="Lý do" value={selected.reason || '-'} />
                <InfoRow label="Target" value={`${selected.targetType} (ID: ${selected.targetId})`} />
                <InfoRow label="Reporter" value={`${selected.reporterName} (ID: ${selected.reporterId})`} />
                <InfoRow label="Trạng thái" value={selected.status || 'PENDING'} />

                <Section title="Mô tả">
                  <div className="text-sm text-gray-700 whitespace-pre-wrap">
                    {selected.description || '(Không có mô tả)'}
                  </div>
                </Section>

                <Section title="Bằng chứng">
                  {selected.evidenceUrl ? (
                    <a
                      className="text-sm text-blue-600 hover:underline break-all"
                      href={selected.evidenceUrl}
                      target="_blank"
                      rel="noreferrer"
                    >
                      {selected.evidenceUrl}
                    </a>
                  ) : (
                    <div className="text-sm text-gray-700">(Không có)</div>
                  )}
                </Section>
              </div>

              {/* Actions chỉ khi đang PENDING */}
              {status === 'PENDING' && (
                <div className="p-5 border-t flex items-center justify-between gap-3">
                  <button
                    type="button"
                    onClick={() => updateStatus(selected.id, 'INVALID')}
                    disabled={actionLoading}
                    className="px-4 py-2 rounded-lg border text-red-600 hover:bg-red-50 disabled:opacity-60"
                  >
                    {actionLoading ? 'Đang xử lý...' : 'Từ chối'}
                  </button>

                  <button
                    type="button"
                    onClick={() => updateStatus(selected.id, 'VALID')}
                    disabled={actionLoading}
                    className="px-4 py-2 rounded-lg bg-green-600 text-white hover:bg-green-700 disabled:opacity-60"
                  >
                    {actionLoading ? 'Đang xử lý...' : 'Xác nhận vi phạm'}
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
