'use client';

import React, { useState, useEffect } from 'react';
import api from '@/services/api';
import { Check, X, FileText, Clock } from 'lucide-react';

interface Article {
  id: number;
  title: string;
  authorName: string;
  summary: string;
  status: 'PENDING' | 'APPROVED';
  createdAt: string;
}

export default function ContentManagementPage() {
  const [activeTab, setActiveTab] = useState<'PENDING' | 'ALL'>('PENDING');
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchArticles = async () => {
    setLoading(true);
    try {
      // Gọi API tương ứng dựa trên Tab
      const endpoint = activeTab === 'PENDING' ? '/admin/articles/pending' : '/admin/articles';
      const res = await api.get(endpoint);
      setArticles(res.data.data); // data.data do MessageResponse bọc
    } catch (error) {
      console.error("Lỗi tải bài viết:", error);
      // Mock data
      setArticles([
        { id: 101, title: 'Cách viết CV chuẩn chỉnh', authorName: 'Nguyễn Văn A', summary: 'Hướng dẫn chi tiết...', status: 'PENDING', createdAt: '2024-01-20' },
        { id: 102, title: 'Kinh nghiệm phỏng vấn Java', authorName: 'Lê Thị B', summary: 'Các câu hỏi thường gặp...', status: 'PENDING', createdAt: '2024-01-21' },
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchArticles();
  }, [activeTab]);

  const handleApprove = async (id: number) => {
    if(!confirm('Duyệt bài viết này?')) return;
    try {
      await api.put(`/admin/articles/${id}/approve`);
      alert('Đã duyệt!');
      setArticles(prev => prev.filter(a => a.id !== id));
    } catch (err) {
      alert('Lỗi khi duyệt');
    }
  };

  const handleReject = async (id: number) => {
    if(!confirm('Xóa/Từ chối bài viết này?')) return;
    try {
      await api.delete(`/admin/articles/${id}`);
      alert('Đã từ chối!');
      setArticles(prev => prev.filter(a => a.id !== id));
    } catch (err) {
      alert('Lỗi khi từ chối');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-800">Quản lý Nội dung</h1>
        <div className="flex space-x-2 bg-white p-1 rounded-lg border">
          <button 
            onClick={() => setActiveTab('PENDING')}
            className={`px-4 py-2 text-sm font-medium rounded-md transition ${activeTab === 'PENDING' ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:bg-gray-50'}`}
          >
            Chờ duyệt
          </button>
          <button 
            onClick={() => setActiveTab('ALL')}
            className={`px-4 py-2 text-sm font-medium rounded-md transition ${activeTab === 'ALL' ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:bg-gray-50'}`}
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
            <div key={item.id} className="bg-white p-5 rounded-xl shadow-sm border hover:shadow-md transition flex justify-between items-start group">
              <div className="flex-1 pr-4">
                <div className="flex items-center gap-2 mb-1">
                  <span className="bg-yellow-100 text-yellow-700 text-xs px-2 py-0.5 rounded font-medium flex items-center">
                     <Clock className="w-3 h-3 mr-1" /> Chờ duyệt
                  </span>
                  <span className="text-xs text-gray-500">{item.createdAt}</span>
                </div>
                <h3 className="text-lg font-bold text-gray-900 group-hover:text-blue-600 transition">{item.title}</h3>
                <p className="text-sm text-gray-600 mt-1 line-clamp-2">{item.summary}</p>
                <p className="text-xs text-gray-400 mt-2">Tác giả: {item.authorName}</p>
              </div>

              {activeTab === 'PENDING' && (
                <div className="flex gap-2">
                  <button 
                    onClick={() => handleApprove(item.id)}
                    className="p-2 bg-green-50 text-green-600 rounded-lg hover:bg-green-100 transition" 
                    title="Duyệt bài"
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