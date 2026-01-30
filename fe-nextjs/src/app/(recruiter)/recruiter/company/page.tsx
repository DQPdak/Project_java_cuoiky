"use client";

import React, { useState, useEffect, useRef } from "react";
import {
  Edit,
  Save,
  X,
  Camera,
  Loader2,
} from "lucide-react";
import { recruitmentService } from "@/services/recruitmentService";
import { CompanyProfile } from "@/types/recruitment";
import { toast } from "react-hot-toast";

export default function CompanyPage() {
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false); // State loading khi upload
  const [companyData, setCompanyData] = useState<CompanyProfile | null>(null);
  const [editData, setEditData] = useState<CompanyProfile | null>(null);

  // Ref để kích hoạt input file ẩn
  const logoInputRef = useRef<HTMLInputElement>(null);
  const coverInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchCompanyData();
  }, []);

  const fetchCompanyData = async () => {
    try {
      setLoading(true);
      const data = await recruitmentService.getMyCompany();
      // Khởi tạo giá trị mặc định để tránh lỗi null
      const formattedData: CompanyProfile = {
        ...data,
        name: data?.name || "",
        description: data?.description || "",
        logoUrl: data?.logoUrl || "/api/placeholder/150/150",
        coverImageUrl: data?.coverImageUrl || "/api/placeholder/800/200",
      };
      setCompanyData(formattedData);
      setEditData(formattedData);
    } catch (error) {
      toast.error("Không thể tải thông tin công ty");
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!editData) return;
    try {
      const updated = await recruitmentService.updateCompany(editData);
      setCompanyData(updated);
      setIsEditing(false);
      toast.success("Cập nhật thành công!");
    } catch (error) {
      toast.error("Cập nhật thất bại. Vui lòng kiểm tra lại dữ liệu.");
    }
  };

  // --- HÀM XỬ LÝ UPLOAD ẢNH ---
  const handleFileUpload = async (
    e: React.ChangeEvent<HTMLInputElement>,
    field: "logoUrl" | "coverImageUrl"
  ) => {
    const file = e.target.files?.[0];
    if (!file || !editData) return;

    // Validate kích thước (ví dụ 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error("File ảnh quá lớn (Max 5MB)");
      return;
    }

    try {
      setUploading(true);
      // Gọi API upload
      const url = await recruitmentService.uploadImage(file);
      
      // Cập nhật URL trả về vào state editData để hiển thị ngay
      setEditData({ ...editData, [field]: url });
      toast.success("Upload ảnh thành công!");
    } catch (error) {
      console.error(error);
      toast.error("Lỗi khi upload ảnh");
    } finally {
      setUploading(false);
      // Reset input để người dùng có thể chọn lại cùng 1 file nếu muốn
      e.target.value = "";
    }
  };

  if (loading)
    return <div className="p-8 text-center">Đang tải thông tin...</div>;
  if (!editData)
    return <div className="p-8 text-center">Không tìm thấy dữ liệu.</div>;

  return (
    <div className="max-w-6xl mx-auto p-4">
      {/* Input File Ẩn cho Cover và Logo */}
      <input
        type="file"
        ref={coverInputRef}
        hidden
        accept="image/*"
        onChange={(e) => handleFileUpload(e, "coverImageUrl")}
      />
      <input
        type="file"
        ref={logoInputRef}
        hidden
        accept="image/*"
        onChange={(e) => handleFileUpload(e, "logoUrl")}
      />

      {/* Header & Buttons */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý Công ty</h1>
          <p className="text-gray-500">
            Thông tin này sẽ hiển thị trên các tin tuyển dụng của bạn.
          </p>
        </div>
        {!isEditing ? (
          <button
            onClick={() => setIsEditing(true)}
            className="flex items-center bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition"
          >
            <Edit className="w-4 h-4 mr-2" /> Chỉnh sửa
          </button>
        ) : (
          <div className="flex gap-2">
            <button
              onClick={handleSave}
              disabled={uploading}
              className="flex items-center bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition disabled:opacity-50"
            >
              {uploading ? <Loader2 className="w-4 h-4 mr-2 animate-spin"/> : <Save className="w-4 h-4 mr-2" />}
              Lưu thay đổi
            </button>
            <button
              onClick={() => {
                setEditData(companyData);
                setIsEditing(false);
              }}
              disabled={uploading}
              className="flex items-center bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition disabled:opacity-50"
            >
              <X className="w-4 h-4 mr-2" /> Hủy
            </button>
          </div>
        )}
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {/* Cover Image */}
        <div className="relative h-48 bg-gray-200 group">
          <img
            src={editData.coverImageUrl}
            alt="Cover"
            className="w-full h-full object-cover"
          />
          {isEditing && (
            <div className="absolute inset-0 bg-black/30 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
               <button
                onClick={() => coverInputRef.current?.click()}
                className="bg-white/20 backdrop-blur-md border border-white/50 text-white px-4 py-2 rounded-full flex items-center hover:bg-white/30 transition"
              >
                <Camera className="w-5 h-5 mr-2" />
                Thay đổi ảnh bìa
              </button>
            </div>
          )}
        </div>

        <div className="p-6">
          <div className="flex items-start gap-6 mb-8">
            {/* Logo Image */}
            <div className="relative w-24 h-24 rounded-lg bg-gray-100 border flex-shrink-0 group overflow-hidden">
              <img
                src={editData.logoUrl}
                alt="Logo"
                className="w-full h-full object-contain p-1"
              />
              {isEditing && (
                <div 
                    className="absolute inset-0 bg-black/30 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
                    onClick={() => logoInputRef.current?.click()}
                >
                  <Camera className="w-6 h-6 text-white" />
                </div>
              )}
            </div>

            <div className="flex-1 space-y-4">
              {isEditing ? (
                <>
                  <input
                    type="text"
                    value={editData.name}
                    onChange={(e) =>
                      setEditData({ ...editData, name: e.target.value })
                    }
                    className="text-xl font-bold w-full border-b focus:border-blue-500 outline-none pb-1"
                    placeholder="Tên công ty"
                  />
                  <textarea
                    value={editData.description}
                    onChange={(e) =>
                      setEditData({ ...editData, description: e.target.value })
                    }
                    className="w-full border rounded-lg p-2 text-gray-600 h-24"
                    placeholder="Mô tả ngắn về công ty..."
                  />
                </>
              ) : (
                <>
                  <h2 className="text-2xl font-bold text-gray-900">
                    {companyData?.name}
                  </h2>
                  <p className="text-gray-600">{companyData?.description}</p>
                </>
              )}
            </div>
          </div>

          {/* Details Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 border-t pt-8">
            <div className="space-y-4">
              {/* Ngành nghề */}
              <div>
                <label className="block text-sm font-semibold text-gray-700">
                  Ngành nghề
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editData.industry || ""}
                    onChange={(e) =>
                      setEditData({ ...editData, industry: e.target.value })
                    }
                    className="w-full border rounded-lg px-3 py-2 mt-1"
                  />
                ) : (
                  <p className="mt-1">
                    {companyData?.industry || "Chưa cập nhật"}
                  </p>
                )}
              </div>
              {/* Quy mô */}
              <div>
                <label className="block text-sm font-semibold text-gray-700">
                  Quy mô nhân sự
                </label>
                {isEditing ? (
                  <select
                    value={editData.size || ""}
                    onChange={(e) =>
                      setEditData({ ...editData, size: e.target.value })
                    }
                    className="w-full border rounded-lg px-3 py-2 mt-1"
                  >
                    <option value="">Chọn quy mô</option>
                    <option value="1-50">1-50 nhân viên</option>
                    <option value="51-200">51-200 nhân viên</option>
                    <option value="201-500">201-500 nhân viên</option>
                    <option value="500+">Trên 500 nhân viên</option>
                  </select>
                ) : (
                  <p className="mt-1">{companyData?.size || "Chưa cập nhật"}</p>
                )}
              </div>
            </div>

            <div className="space-y-4">
              {/* Website */}
              <div>
                <label className="block text-sm font-semibold text-gray-700">
                  Website
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editData.website || ""}
                    onChange={(e) =>
                      setEditData({ ...editData, website: e.target.value })
                    }
                    className="w-full border rounded-lg px-3 py-2 mt-1"
                  />
                ) : (
                  <a
                    href={companyData?.website}
                    target="_blank"
                    rel="noreferrer"
                    className="text-blue-600 block mt-1"
                  >
                    {companyData?.website || "Chưa có website"}
                  </a>
                )}
              </div>
              {/* Địa chỉ */}
              <div>
                <label className="block text-sm font-semibold text-gray-700">
                  Địa chỉ trụ sở
                </label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editData.address || ""}
                    onChange={(e) =>
                      setEditData({ ...editData, address: e.target.value })
                    }
                    className="w-full border rounded-lg px-3 py-2 mt-1"
                  />
                ) : (
                  <p className="mt-1">
                    {companyData?.address || "Chưa cập nhật"}
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}