"use client";

import React, { useEffect, useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import api from '@/services/api';
import { MapPin, DollarSign, Building, Search, ArrowLeft, Calendar } from 'lucide-react';
import Link from 'next/link';

// Component con để dùng useSearchParams an toàn trong Suspense
function SearchContent() {
  const searchParams = useSearchParams();
  const keyword = searchParams.get('keyword') || '';
  const [jobs, setJobs] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [inputVal, setInputVal] = useState(keyword);
  const router = useRouter();

  useEffect(() => {
    fetchJobs(keyword);
    setInputVal(keyword);
  }, [keyword]);

  const fetchJobs = async (query: string) => {
    setLoading(true);
    try {
      const res = await api.get(`/recruitment/jobs/search?keyword=${query}`);
      setJobs(res.data.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    router.push(`/jobs?keyword=${inputVal}`);
  };

  return (
    <div className="max-w-6xl mx-auto py-8 px-4">
      {/* Thanh tìm kiếm phía trên */}
      <div className="flex items-center gap-4 mb-8">
        <Link href="/dashboard-candidate" className="p-2 hover:bg-gray-100 rounded-full">
            <ArrowLeft size={24} className="text-gray-600"/>
        </Link>
        <form onSubmit={handleSearch} className="flex-1 flex gap-2 relative">
            <Search className="absolute left-3 top-3 text-gray-400" size={20}/>
            <input 
                type="text" 
                value={inputVal}
                onChange={(e) => setInputVal(e.target.value)}
                placeholder="Tìm kiếm việc làm, công ty..."
                className="w-full pl-10 pr-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none shadow-sm"
            />
            <button type="submit" className="bg-blue-600 text-white px-6 py-2.5 rounded-lg font-medium hover:bg-blue-700 transition">
                Tìm
            </button>
        </form>
      </div>

      <h2 className="text-xl font-bold mb-6 text-gray-800">
        {keyword ? `Kết quả cho "${keyword}"` : "Việc làm mới nhất"} 
        <span className="text-sm font-normal text-gray-500 ml-2">({jobs.length} kết quả)</span>
      </h2>
      
      {loading ? (
        <div className="space-y-4">
             {[1,2,3].map(i => <div key={i} className="h-32 bg-gray-100 rounded-xl animate-pulse"/>)}
        </div>
      ) : (
        <div className="grid gap-4">
            {jobs.length === 0 ? (
                <div className="text-center py-10 bg-white rounded-xl border border-dashed">
                    <p className="text-gray-500">Không tìm thấy công việc nào phù hợp.</p>
                </div>
            ) : jobs.map((job) => (
               <div key={job.id} className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition group relative overflow-hidden">
                   <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                       <div>
                           <h3 className="font-bold text-lg text-gray-900 group-hover:text-blue-600 transition">{job.title}</h3>
                           <div className="flex items-center text-gray-600 text-sm mt-2 gap-4 flex-wrap">
                               <span className="flex items-center font-medium"><Building size={16} className="mr-1.5 text-gray-400"/> {job.companyName || "Công ty ẩn danh"}</span>
                               <span className="flex items-center"><MapPin size={16} className="mr-1.5 text-gray-400"/> {job.location}</span>
                               <span className="flex items-center text-green-600 font-semibold bg-green-50 px-2 py-0.5 rounded border border-green-100">
                                   <DollarSign size={14} className="mr-1"/> {job.salaryRange || "Thỏa thuận"}
                               </span>
                               <span className="flex items-center text-gray-400 text-xs">
                                   <Calendar size={14} className="mr-1"/> Hết hạn: {new Date(job.deadline).toLocaleDateString('vi-VN')}
                               </span>
                           </div>
                       </div>
                       <button 
                         onClick={() => {/* Xử lý ứng tuyển sau */}}
                         className="px-5 py-2.5 bg-blue-50 text-blue-600 font-semibold rounded-lg hover:bg-blue-600 hover:text-white transition whitespace-nowrap">
                         Chi tiết
                       </button>
                   </div>
               </div>
            ))}
        </div>
      )}
    </div>
  );
}

// Wrap trong Suspense để tránh lỗi build Next.js khi dùng useSearchParams
export default function JobSearchPage() {
    return (
        <Suspense fallback={<div>Loading search...</div>}>
            <SearchContent />
        </Suspense>
    )
}