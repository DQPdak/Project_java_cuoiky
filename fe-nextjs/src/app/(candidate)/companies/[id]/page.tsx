"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { MapPin, Globe, Users, ArrowLeft, Building } from "lucide-react";
import CompanyReviews from "@/components/features/company/CompanyReviews";
import { companyService, Company } from "@/services/companyService";
import toast from "react-hot-toast";

export default function CompanyDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = Number(params.id);
  
  const [company, setCompany] = useState<Company | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (id) {
      fetchCompanyDetail();
    }
  }, [id]);

  const fetchCompanyDetail = async () => {
    try {
      setIsLoading(true);
      const data = await companyService.getById(id);
      setCompany(data);
    } catch (error) {
      console.error("Lỗi khi tải thông tin công ty:", error);
      toast.error("Không thể tải thông tin công ty");
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) return <div className="p-10 text-center">Đang tải thông tin công ty...</div>;
  if (!company) return <div className="p-10 text-center text-red-500">Không tìm thấy thông tin công ty.</div>;

  return (
    <div className="max-w-5xl mx-auto py-8 px-4">
      <button 
        onClick={() => router.back()} 
        className="flex items-center text-gray-500 mb-4 hover:text-indigo-600 transition-colors"
      >
        <ArrowLeft size={18} className="mr-1"/> Quay lại
      </button>

      {/* Banner & Thông tin chính */}
      <div className="bg-white rounded-xl shadow-sm border overflow-hidden mb-6">
        <div className="h-32 bg-indigo-600 relative">
          {company.coverImageUrl && (
            <img src={company.coverImageUrl} className="w-full h-full object-cover opacity-50" alt="banner" />
          )}
        </div>
        <div className="px-6 pb-6">
          <div className="flex flex-col md:flex-row items-end -mt-12 mb-4 gap-4">
            <div className="w-24 h-24 bg-white rounded-xl shadow-md border p-1 overflow-hidden">
              {company.logoUrl ? (
                <img src={company.logoUrl} alt={company.name} className="w-full h-full object-contain"/>
              ) : (
                <div className="w-full h-full flex items-center justify-center bg-gray-100">
                   <Building className="text-gray-400" size={40} />
                </div>
              )}
            </div>
            <div className="flex-1 mb-2 text-center md:text-left">
              <h1 className="text-2xl font-bold text-gray-900">{company.name}</h1>
              <div className="flex flex-wrap justify-center md:justify-start gap-4 text-sm text-gray-600 mt-2">
                <span className="flex items-center gap-1"><MapPin size={16}/> {company.address}</span>
                <span className="flex items-center gap-1"><Users size={16}/> {company.size || "Đang cập nhật"} nhân viên</span>
                {company.website && (
                  <a href={company.website} target="_blank" className="flex items-center gap-1 text-indigo-600 hover:underline">
                    <Globe size={16}/> Website
                  </a>
                )}
              </div>
            </div>
          </div>
          
          <div className="mt-6 border-t pt-6">
            <h3 className="font-bold text-lg mb-2">Giới thiệu công ty</h3>
            <div className="text-gray-700 leading-relaxed whitespace-pre-line">
              {company.description || "Chưa có mô tả chi tiết."}
            </div>
          </div>
        </div>
      </div>

      {/* Phần Đánh giá từ ứng viên */}
      <CompanyReviews companyId={id} />
    </div>
  );
}