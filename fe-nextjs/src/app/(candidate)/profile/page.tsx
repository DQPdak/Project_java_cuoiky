"use client";
import React, { useEffect, useState } from 'react';
import { getMyProfile, updateProfile } from '@/services/candidateService';
import { User, Phone, Globe, Linkedin, Book, Code, Save, Loader2, MapPin, Briefcase } from 'lucide-react';
import toast from 'react-hot-toast';

export default function ProfilePage() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  
  // Load dữ liệu ban đầu
  useEffect(() => {
      fetchProfile();
    }, []);
    
  // State quản lý dữ liệu form
  const [formData, setFormData] = useState({
      aboutMe: '',
      phoneNumber: '',
      address: '',        // Thêm trường address
      websiteUrl: '',     // Đổi tên cho khớp backend
      linkedInUrl: '',    // Thêm trường LinkedIn
      skills: [] as string[],
  });

  const fetchProfile = async () => {
    try {
      const data = await getMyProfile();
      if (data) {
        setFormData({
          aboutMe: data.aboutMe || '',
          phoneNumber: data.phoneNumber || '',
          address: data.address || '',
          websiteUrl: data.websiteUrl || '',     // Load link website
          linkedInUrl: data.linkedInUrl || '',   // Load link LinkedIn
          skills: data.skills || [],
        });
      }
    } catch (error) {
      console.error("Lỗi load profile", error);
    } finally {
      setLoading(false);
    }
  };

  // Xử lý thay đổi input
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  // Xử lý thay đổi Skills (nhập chuỗi phân cách bởi dấu phẩy)
  const handleSkillChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const skillsArr = e.target.value.split(',').map(s => s.trim());
    setFormData(prev => ({ ...prev, skills: skillsArr }));
  };

  // Submit form
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateProfile(formData);
      toast.success("Đã lưu hồ sơ thành công! 🎉");
    } catch (error) {
      toast.error("Lỗi khi lưu hồ sơ.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="p-8 text-center">Đang tải hồ sơ...</div>;

  return (
    <div className="max-w-4xl mx-auto py-8 px-4">
      <div className="flex items-center justify-between mb-8">
          <h1 className="text-2xl font-bold flex items-center gap-2 text-gray-800">
            <User className="text-blue-600"/> Hồ sơ cá nhân
          </h1>
          <button 
            onClick={handleSubmit}
            disabled={saving}
            className="flex items-center gap-2 bg-blue-600 text-white px-6 py-2.5 rounded-lg hover:bg-blue-700 transition disabled:bg-blue-300">
            {saving ? <Loader2 className="animate-spin" size={20}/> : <Save size={20}/>}
            {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 space-y-6">
        
        {/* 1. Thông tin liên hệ */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                    <Phone size={16}/> Số điện thoại
                </label>
                <input 
                    type="text" name="phoneNumber"
                    value={formData.phoneNumber} onChange={handleChange}
                    className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-200 outline-none"
                    placeholder="0912..."
                />
            </div>
             <div>
                <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                    <MapPin size={16}/> Địa chỉ
                </label>
                <input 
                    type="text" name="address"
                    value={formData.address} onChange={handleChange}
                    className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-200 outline-none"
                    placeholder="Hà Nội, Việt Nam"
                />
            </div>
        </div>

        {/* 2. Liên kết mạng xã hội (2 Links) */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                    <Linkedin size={16} className="text-blue-700"/> LinkedIn Profile
                </label>
                <input 
                    type="text" name="linkedInUrl"
                    value={formData.linkedInUrl} onChange={handleChange}
                    className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-200 outline-none"
                    placeholder="https://linkedin.com/in/username"
                />
            </div>
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                    <Globe size={16} className="text-green-600"/> Website / Portfolio
                </label>
                <input 
                    type="text" name="websiteUrl"
                    value={formData.websiteUrl} onChange={handleChange}
                    className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-200 outline-none"
                    placeholder="https://myportfolio.com"
                />
            </div>
        </div>

        {/* 2. Giới thiệu */}
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                <Book size={16}/> Giới thiệu bản thân
            </label>
            <textarea 
                name="aboutMe"
                rows={5}
                value={formData.aboutMe}
                onChange={handleChange}
                className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-200 outline-none"
                placeholder="Mô tả ngắn gọn về mục tiêu nghề nghiệp và điểm mạnh của bạn..."
            />
        </div>

        {/* 3. Kỹ năng */}
        <div>
            <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                <Code size={16}/> Kỹ năng chuyên môn
            </label>
            <input 
                type="text" 
                value={formData.skills.join(', ')}
                onChange={handleSkillChange}
                className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-200 outline-none"
                placeholder="Java, Spring Boot, ReactJS (Phân cách bằng dấu phẩy)"
            />
            <div className="mt-3 flex flex-wrap gap-2">
                {formData.skills.map((skill, idx) => (
                    skill && <span key={idx} className="bg-blue-50 text-blue-700 px-3 py-1 rounded-full text-sm font-medium border border-blue-100">
                        {skill}
                    </span>
                ))}
            </div>
        </div>

        {/* (Optional) Phần Experience có thể làm phức tạp hơn sau */}
        <div className="p-4 bg-yellow-50 rounded-lg border border-yellow-100 text-yellow-800 text-sm">
            <Briefcase className="inline-block mr-2" size={16}/>
            Hiện tại bạn có thể cập nhật Kinh nghiệm làm việc bằng cách tải lên CV mới để AI phân tích lại.
        </div>

      </div>
    </div>
  );
}