'use client';

import React, { useState } from 'react';
import { Search, Filter, UserCheck, Calendar, Send, MessageSquare, Star, Download, Eye, MoreVertical, CheckCircle, Clock, XCircle } from 'lucide-react';

export default function CandidatesPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('all');
  const [selectedCandidates, setSelectedCandidates] = useState<number[]>([]);

  const candidates = [
    {
      id: 1,
      name: 'Nguyen Van A',
      email: 'nguyenvana@email.com',
      phone: '0123 456 789',
      job: 'Senior Java Backend Developer',
      status: 'applied',
      score: 85,
      appliedDate: '2024-01-15',
      skills: ['Java', 'Spring Boot', 'MySQL', 'Docker'],
      experience: '5 năm'
    },
    {
      id: 2,
      name: 'Tran Thi B',
      email: 'tranthib@email.com',
      phone: '0987 654 321',
      job: 'ReactJS Frontend Developer',
      status: 'screening',
      score: 92,
      appliedDate: '2024-01-14',
      skills: ['React', 'TypeScript', 'CSS', 'Node.js'],
      experience: '3 năm'
    },
    {
      id: 3,
      name: 'Le Van C',
      email: 'levanc@email.com',
      phone: '0567 890 123',
      job: 'Business Analyst',
      status: 'interview',
      score: 78,
      appliedDate: '2024-01-13',
      skills: ['SQL', 'Excel', 'Data Analysis', 'Agile'],
      experience: '4 năm'
    },
    {
      id: 4,
      name: 'Pham Thi D',
      email: 'phamthid@email.com',
      phone: '0345 678 901',
      job: 'Senior Java Backend Developer',
      status: 'offer',
      score: 88,
      appliedDate: '2024-01-12',
      skills: ['Java', 'Spring', 'PostgreSQL', 'AWS'],
      experience: '6 năm'
    },
  ];

  const filteredCandidates = candidates.filter(candidate => {
    const matchesSearch = candidate.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         candidate.job.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         candidate.skills.some(skill => skill.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesStatus = filterStatus === 'all' || candidate.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'applied': return 'bg-blue-100 text-blue-700';
      case 'screening': return 'bg-yellow-100 text-yellow-700';
      case 'interview': return 'bg-purple-100 text-purple-700';
      case 'offer': return 'bg-green-100 text-green-700';
      case 'hired': return 'bg-emerald-100 text-emerald-700';
      case 'rejected': return 'bg-red-100 text-red-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'applied': return 'Đã ứng tuyển';
      case 'screening': return 'Sơ tuyển';
      case 'interview': return 'Phỏng vấn';
      case 'offer': return 'Đề nghị';
      case 'hired': return 'Đã tuyển';
      case 'rejected': return 'Từ chối';
      default: return status;
    }
  };

  const handleBulkAction = (action: string) => {
    // Handle bulk actions like move to next stage, reject, etc.
    console.log(`Bulk ${action} for candidates:`, selectedCandidates);
  };

  const handleCandidateAction = (candidateId: number, action: string) => {
    // Handle individual candidate actions
    console.log(`Action ${action} for candidate ${candidateId}`);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý Ứng viên</h1>
          <p className="text-gray-500">Xem hồ sơ, đánh giá và quản lý quy trình tuyển dụng.</p>
        </div>
        <div className="flex gap-2">
          {selectedCandidates.length > 0 && (
            <>
              <button
                onClick={() => handleBulkAction('screening')}
                className="flex items-center bg-yellow-600 text-white px-4 py-2 rounded-lg hover:bg-yellow-700 transition shadow-sm font-medium text-sm"
              >
                <UserCheck className="w-4 h-4 mr-2" /> Sơ tuyển ({selectedCandidates.length})
              </button>
              <button
                onClick={() => handleBulkAction('interview')}
                className="flex items-center bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition shadow-sm font-medium text-sm"
              >
                <Calendar className="w-4 h-4 mr-2" /> Phỏng vấn
              </button>
              <button
                onClick={() => handleBulkAction('offer')}
                className="flex items-center bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition shadow-sm font-medium text-sm"
              >
                <Send className="w-4 h-4 mr-2" /> Gửi đề nghị
              </button>
            </>
          )}
        </div>
      </div>

      {/* Search and Filter */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="w-5 h-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
            <input
              type="text"
              placeholder="Tìm kiếm theo tên, vị trí hoặc kỹ năng..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <div className="flex items-center gap-2">
            <Filter className="w-5 h-5 text-gray-400" />
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="all">Tất cả trạng thái</option>
              <option value="applied">Đã ứng tuyển</option>
              <option value="screening">Sơ tuyển</option>
              <option value="interview">Phỏng vấn</option>
              <option value="offer">Đề nghị</option>
              <option value="hired">Đã tuyển</option>
              <option value="rejected">Từ chối</option>
            </select>
          </div>
        </div>
      </div>

      {/* Candidates List */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 className="font-bold text-gray-800">Danh sách ứng viên ({filteredCandidates.length})</h3>
          <div className="text-sm text-gray-500">
            Đã chọn: {selectedCandidates.length} ứng viên
          </div>
        </div>
        <div className="divide-y divide-gray-100">
          {filteredCandidates.map((candidate) => (
            <div key={candidate.id} className="p-6 hover:bg-gray-50 transition">
              <div className="flex items-start gap-4">
                <input
                  type="checkbox"
                  checked={selectedCandidates.includes(candidate.id)}
                  onChange={(e) => {
                    if (e.target.checked) {
                      setSelectedCandidates([...selectedCandidates, candidate.id]);
                    } else {
                      setSelectedCandidates(selectedCandidates.filter(id => id !== candidate.id));
                    }
                  }}
                  className="mt-1 w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                />
                <div className="w-12 h-12 rounded-full bg-gray-200 flex items-center justify-center font-bold text-gray-500 flex-shrink-0">
                  {candidate.name.split(' ').map(n => n[0]).join('')}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3 mb-2">
                    <h4 className="text-lg font-semibold text-gray-900">{candidate.name}</h4>
                    <span className={`text-xs px-2 py-1 rounded-full font-medium ${getStatusColor(candidate.status)}`}>
                      {getStatusText(candidate.status)}
                    </span>
                    <div className="flex items-center gap-1 text-sm text-gray-600">
                      <Star className="w-4 h-4 text-yellow-500 fill-current" />
                      {candidate.score}% phù hợp
                    </div>
                  </div>
                  <div className="text-sm text-gray-600 mb-2">
                    <p><strong>Email:</strong> {candidate.email} | <strong>Phone:</strong> {candidate.phone}</p>
                    <p><strong>Vị trí:</strong> {candidate.job} | <strong>Kinh nghiệm:</strong> {candidate.experience}</p>
                  </div>
                  <div className="flex flex-wrap gap-1 mb-3">
                    {candidate.skills.map((skill, index) => (
                      <span key={index} className="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded-full">
                        {skill}
                      </span>
                    ))}
                  </div>
                  <div className="flex items-center justify-between">
                    <p className="text-sm text-gray-500">
                      Ứng tuyển: {new Date(candidate.appliedDate).toLocaleDateString('vi-VN')}
                    </p>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => handleCandidateAction(candidate.id, 'view')}
                        className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition"
                        title="Xem hồ sơ"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleCandidateAction(candidate.id, 'download')}
                        className="p-2 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-lg transition"
                        title="Tải CV"
                      >
                        <Download className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleCandidateAction(candidate.id, 'message')}
                        className="p-2 text-gray-400 hover:text-purple-600 hover:bg-purple-50 rounded-lg transition"
                        title="Nhắn tin"
                      >
                        <MessageSquare className="w-4 h-4" />
                      </button>
                      <div className="relative">
                        <button className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-50 rounded-lg transition">
                          <MoreVertical className="w-4 h-4" />
                        </button>
                        {/* Dropdown menu would go here */}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
        {filteredCandidates.length === 0 && (
          <div className="p-12 text-center">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Search className="w-8 h-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">Không tìm thấy ứng viên</h3>
            <p className="text-gray-500">Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc trạng thái.</p>
          </div>
        )}
      </div>
    </div>
  );
}