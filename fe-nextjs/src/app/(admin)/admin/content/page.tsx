"use client";

import React, { useState, useEffect } from "react";
import api from "@/services/api";
import { Check, X, FileText, Clock } from "lucide-react";
import { useConfirm } from "@/context/ConfirmDialogContext";
import toast from "react-hot-toast";

interface Article {
  id: number;
  title: string;
  authorName?: string; // sẽ map từ companyName (tạm)
  summary: string;
  status: "PENDING" | "PUBLISHED" | "REJECTED" | "DRAFT" | "HIDDEN" | "CLOSED";
  createdAt: string;
}

export default function ContentManagementPage() {
  const confirm = useConfirm();
  const [activeTab, setActiveTab] = useState<"PENDING" | "ALL">("PENDING");
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchArticles = async () => {
    setLoading(true);
    try {
      const endpoint =
        activeTab === "PENDING"
          ? "/admin/content/pending"
          : "/admin/content/posts?status=ALL";

      const res = await api.get(endpoint);

      // BE trả Page => lấy res.data.content
      const content = res.data?.content ?? [];

      // Map dữ liệu JobPosting -> Article UI
      const mapped: Article[] = content.map((j: any) => ({
        id: j.id,
        title: j.title,
        // tạm dùng companyName làm "authorName" cho UI hiện tại
        authorName: j.companyName ?? "N/A",
        summary: j.description ?? j.summary ?? "", // tuỳ DTO BE của bạn trả gì
        status: j.status,
        createdAt: j.createdAt,
      }));

      setArticles(mapped);
    } catch (error) {
      console.error("Lỗi tải bài viết:", error);
      setArticles([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchArticles();
  }, [activeTab]);

  const handleApprove = async (id: number) => {
    const ok = await confirm({
      title: "Duyệt bài đăng",
      message:
        "Bạn có chắc chắn muốn duyệt bài tuyển dụng này để hiển thị công khai?",
      confirmLabel: "Duyệt bài",
    });

    if (!ok) return;
    try {
      await api.put(`/admin/content/posts/${id}/approve`);
      toast.success("Đã duyệt bài viết!");
      setArticles((prev) => prev.filter((a) => a.id !== id));
    } catch (err) {
      toast.error("Lỗi khi duyệt bài");
    }
  };

  const handleReject = async (id: number) => {
    const ok = await confirm({
      title: "Từ chối bài đăng",
      message:
        "Bài viết này sẽ bị từ chối và không được hiển thị. Bạn có chắc không?",
      isDanger: true,
      confirmLabel: "Từ chối",
    });

    if (!ok) return;
    try {
      // Nếu BE của bạn yêu cầu body reason thì đổi thành { reason: '...' }
      await api.put(`/admin/content/posts/${id}/reject`, {});
      toast.success("Đã từ chối bài viết!");
      setArticles((prev) => prev.filter((a) => a.id !== id));
    } catch (err) {
      toast.error("Lỗi khi từ chối bài viết!");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Quản lý Nội dung</h1>
        <div className="flex space-x-2 bg-white p-1 rounded-lg border">
          <button
            onClick={() => setActiveTab("PENDING")}
            className={`px-4 py-2 text-sm font-medium rounded-md transition ${
              activeTab === "PENDING"
                ? "bg-blue-100 text-blue-700"
                : "text-gray-600 hover:bg-gray-50"
            }`}
          >
            Chờ duyệt
          </button>
          <button
            onClick={() => setActiveTab("ALL")}
            className={`px-4 py-2 text-sm font-medium rounded-md transition ${
              activeTab === "ALL"
                ? "bg-blue-100 text-blue-700"
                : "text-gray-600 hover:bg-gray-50"
            }`}
          >
            Tất cả bài viết
          </button>
        </div>
      </div>

      <div className="grid gap-4">
        {loading ? (
          <p className="text-center text-gray-500">Đang tải dữ liệu...</p>
        ) : articles.length === 0 ? (
          <div className="text-center p-8 bg-white rounded-lg border border-dashed">
            <FileText className="w-12 h-12 text-gray-300 mx-auto mb-2" />
            <p className="text-gray-500">Không có bài viết nào.</p>
          </div>
        ) : (
          articles.map((item) => (
            <div
              key={item.id}
              className="bg-white p-5 rounded-xl shadow-sm border hover:shadow-md transition flex justify-between items-start group"
            >
              <div className="flex-1 pr-4">
                <div className="flex items-center gap-2 mb-1">
                  {item.status === "PENDING" && (
                    <span className="bg-yellow-100 text-yellow-700 text-xs px-2 py-0.5 rounded font-medium flex items-center">
                      <Clock className="w-3 h-3 mr-1" /> Chờ duyệt
                    </span>
                  )}
                  <span className="text-xs text-gray-500">
                    {item.createdAt}
                  </span>
                </div>
                <h3 className="text-lg font-bold text-gray-900 group-hover:text-blue-600 transition">
                  {item.title}
                </h3>
                <p className="text-sm text-gray-600 mt-1 line-clamp-2">
                  {item.summary}
                </p>
                <p className="text-xs text-gray-400 mt-2">
                  Công ty: {item.authorName}
                </p>
              </div>

              {activeTab === "PENDING" && (
                <div className="flex gap-2">
                  <button
                    onClick={() => handleApprove(item.id)}
                    className="p-2 bg-green-50 text-green-600 rounded-lg hover:bg-green-100 transition"
                    title="Duyệt"
                  >
                    <Check className="w-5 h-5" />
                  </button>
                  <button
                    onClick={() => handleReject(item.id)}
                    className="p-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition"
                    title="Từ chối"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
