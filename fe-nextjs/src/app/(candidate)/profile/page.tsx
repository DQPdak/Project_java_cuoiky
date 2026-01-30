"use client";
import React, { useEffect, useState } from 'react';
import { getMyProfile, updateProfile , uploadAvatar} from '@/services/candidateService';
import { 
  User, Phone, Globe, Linkedin, Book, Code, Save, Loader2, Camera,
  MapPin, Briefcase, Plus, Trash2, Calendar, Mail, AlertCircle 
} from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '@/context/Authcontext'; 

interface ExperienceForm {
  companyName: string;
  role: string;
  startDate: string;
  endDate: string;
  description: string;
}

export default function ProfilePage() {
  const { updateUser, user } = useAuth(); 
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  const [formData, setFormData] = useState({
      fullName: '',
      email: '',
      avatarUrl: '',
      aboutMe: '',
      phoneNumber: '',
      address: '',
      websiteUrl: '',
      linkedInUrl: '',
      skills: [] as string[],
  });

  const [experiences, setExperiences] = useState<ExperienceForm[]>([]);

  useEffect(() => {
      fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const data = await getMyProfile();
      if (data) {
        setFormData({
          fullName: data.fullName || '',
          email: data.email || '',
          avatarUrl: data.avatarUrl || (user as any)?.avatarUrl || '', 
          aboutMe: data.aboutMe || '',
          phoneNumber: data.phoneNumber || '',
          address: data.address || '',
          websiteUrl: data.websiteUrl || '',
          linkedInUrl: data.linkedInUrl || '',
          skills: data.skills || [],
        });

        if (data.experiences && data.experiences.length > 0) {
            const mappedExps = data.experiences.map((exp: any) => ({
                companyName: exp.company || '',
                role: exp.role || '',
                startDate: formatDateForInput(exp.startDate),
                endDate: formatDateForInput(exp.endDate),
                description: exp.description || ''
            }));
            setExperiences(mappedExps);
        }
      }
    } catch (error) {
      console.error("Lỗi load profile", error);
    } finally {
      setLoading(false);
    }
  };

  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) { 
        toast.error("File quá lớn! Vui lòng chọn ảnh dưới 5MB");
        return;
    }

    const toastId = toast.loading("Đang tải ảnh lên...");
    try {
        const newUrl = await uploadAvatar(file); 
        
        setFormData(prev => ({ ...prev, avatarUrl: newUrl }));
        
        updateUser({ avatarUrl: newUrl } as any);

        toast.success("Đã cập nhật ảnh đại diện!", { id: toastId });
    } catch (error) {
        console.error(error);
        toast.error("Lỗi upload ảnh", { id: toastId });
    }
  };

  const formatDateForInput = (dateStr: string) => {
      if (!dateStr) return '';
      if (/^\d{4}-\d{2}$/.test(dateStr)) return dateStr;
      return dateStr; 
  };

  const validateForm = () => {
      const newErrors: { [key: string]: string } = {};
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!formData.email) newErrors.email = "Email không được để trống";
      else if (!emailRegex.test(formData.email)) newErrors.email = "Email không đúng định dạng";

      const phoneRegex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
      if (formData.phoneNumber && !phoneRegex.test(formData.phoneNumber)) {
          newErrors.phoneNumber = "Số điện thoại không hợp lệ";
      }

      if (!formData.fullName.trim()) newErrors.fullName = "Họ tên không được để trống";

      setErrors(newErrors);
      return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) {
        setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleSkillChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const skillsArr = e.target.value.split(',').map(s => s.trim());
    setFormData(prev => ({ ...prev, skills: skillsArr }));
  };

  const handleExpChange = (index: number, field: keyof ExperienceForm, value: string) => {
    const newExps = [...experiences];
    newExps[index][field] = value;
    setExperiences(newExps);
  };

  const addExperience = () => {
    setExperiences([
        ...experiences, 
        { companyName: '', role: '', startDate: '', endDate: '', description: '' }
    ]);
  };

  const removeExperience = (index: number) => {
    setExperiences(experiences.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) {
        toast.error("Vui lòng kiểm tra lại thông tin nhập");
        return;
    }

    setSaving(true);
    try {
      const payload = { ...formData, experiences };
      await updateProfile(payload);
      
      // Đồng bộ tên nếu người dùng sửa tên
      updateUser({ fullName: formData.fullName } as any);

      toast.success("Đã lưu hồ sơ thành công! 🎉");
    } catch (error) {
      toast.error("Lỗi khi lưu hồ sơ.");
    } finally {
      setSaving(false);
    }
  };

  // Helper hiển thị ảnh avatar (có fallback mặc định)
  const getDisplayAvatar = () => {
      if (formData.avatarUrl) return formData.avatarUrl;
      const name = formData.fullName || "User";
      return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=random&color=fff&size=128`;
  };

  if (loading) return <div className="flex justify-center items-center h-screen"><Loader2 className="animate-spin text-blue-600"/></div>;

  return (
    <div className="max-w-5xl mx-auto py-8 px-4">
      <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Hồ sơ cá nhân</h1>
            <p className="text-gray-500 text-sm">Quản lý thông tin hiển thị với nhà tuyển dụng</p>
          </div>
          <button 
            onClick={handleSubmit}
            disabled={saving}
            className="flex items-center gap-2 bg-blue-600 text-white px-6 py-2.5 rounded-lg hover:bg-blue-700 transition shadow-md disabled:bg-blue-300">
            {saving ? <Loader2 className="animate-spin" size={20}/> : <Save size={20}/>}
            {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Cột trái: Thông tin cá nhân */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex flex-col items-center">
                <div className="relative group">
                    <div className="w-32 h-32 rounded-full overflow-hidden border-4 border-white shadow-md bg-gray-100">
                        <img 
                            src={getDisplayAvatar()} 
                            alt="Avatar" 
                            className="w-full h-full object-cover"
                        />
                    </div>
                    
                    {/* Nút Camera đè lên ảnh */}
                    <label className="absolute bottom-0 right-0 bg-blue-600 text-white p-2 rounded-full cursor-pointer hover:bg-blue-700 transition shadow-sm border-2 border-white">
                        <Camera size={18} />
                        <input 
                            type="file" 
                            className="hidden" 
                            accept="image/*"
                            onChange={handleAvatarChange}
                        />
                    </label>
                </div>
                <h2 className="mt-4 font-bold text-gray-800 text-lg">{formData.fullName || "Tên của bạn"}</h2>
                <p className="text-sm text-gray-500">{formData.email}</p>
            </div>
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="font-semibold text-gray-800 mb-4 flex items-center gap-2">
                    <User size={18} className="text-blue-600"/> Thông tin cơ bản
                </h3>
                <div className="space-y-4">
                    <div>
                        <label className="text-xs font-medium text-gray-500 uppercase">Họ và tên <span className="text-red-500">*</span></label>
                        <input 
                            type="text" name="fullName" 
                            value={formData.fullName} onChange={handleChange} 
                            className={`w-full mt-1 p-2 border rounded-md text-sm outline-none ${errors.fullName ? 'border-red-500 bg-red-50' : 'focus:border-blue-500'}`}
                            placeholder="Nguyễn Văn A"
                        />
                        {errors.fullName && <p className="text-red-500 text-xs mt-1">{errors.fullName}</p>}
                    </div>

                    <div>
                        <label className="text-xs font-medium text-gray-500 uppercase">Email liên hệ <span className="text-red-500">*</span></label>
                        <div className="flex items-center gap-2 mt-1 border rounded-md px-3 py-2 bg-gray-50">
                            <Mail size={16} className="text-gray-400"/>
                            <input 
                                type="text" name="email" 
                                value={formData.email} onChange={handleChange} 
                                className={`w-full bg-transparent outline-none text-sm ${errors.email ? 'text-red-600' : ''}`}
                                placeholder="email@example.com"
                            />
                        </div>
                        {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
                    </div>

                    <div>
                        <label className="text-xs font-medium text-gray-500 uppercase">Số điện thoại</label>
                        <div className="flex items-center gap-2 mt-1 border rounded-md px-3 py-2 focus-within:border-blue-500">
                            <Phone size={16} className="text-gray-400"/>
                            <input 
                                type="text" name="phoneNumber" 
                                value={formData.phoneNumber} onChange={handleChange} 
                                className="w-full outline-none text-sm" 
                                placeholder="0912..."
                            />
                        </div>
                        {errors.phoneNumber && <p className="text-red-500 text-xs mt-1 flex items-center gap-1"><AlertCircle size={10}/> {errors.phoneNumber}</p>}
                    </div>
                    
                    <div>
                        <label className="text-xs font-medium text-gray-500 uppercase">Địa chỉ</label>
                        <div className="flex items-center gap-2 mt-1 border rounded-md px-3 py-2 focus-within:border-blue-500">
                            <MapPin size={16} className="text-gray-400"/>
                            <input 
                                type="text" name="address" 
                                value={formData.address} onChange={handleChange} 
                                className="w-full outline-none text-sm" 
                                placeholder="Tỉnh/Thành phố"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="font-semibold text-gray-800 mb-4">Mạng xã hội</h3>
                <div className="space-y-3">
                    <div className="flex items-center gap-2 border rounded-md px-3 py-2 focus-within:border-blue-500">
                        <Linkedin size={16} className="text-blue-700"/>
                        <input type="text" name="linkedInUrl" value={formData.linkedInUrl} onChange={handleChange} className="w-full outline-none text-sm" placeholder="LinkedIn URL"/>
                    </div>
                    <div className="flex items-center gap-2 border rounded-md px-3 py-2 focus-within:border-blue-500">
                        <Globe size={16} className="text-green-600"/>
                        <input type="text" name="websiteUrl" value={formData.websiteUrl} onChange={handleChange} className="w-full outline-none text-sm" placeholder="Website / Portfolio"/>
                    </div>
                </div>
            </div>
            
             <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="font-semibold text-gray-800 mb-4 flex items-center gap-2">
                    <Code size={18} className="text-purple-600"/> Kỹ năng
                </h3>
                
                <input 
                    type="text" 
                    value={formData.skills.join(', ')}
                    onChange={handleSkillChange}
                    className="w-full p-3 border rounded-lg text-sm focus:border-purple-500 outline-none"
                    placeholder="Java, Spring Boot, ReactJS (Phân cách bằng dấu phẩy)"
                />
                
                <div className="mt-3 flex flex-wrap gap-2">
                    {formData.skills.map((skill, idx) => (
                        skill && (
                            <span key={idx} className="bg-blue-50 text-blue-700 px-3 py-1 rounded-full text-sm font-medium border border-blue-100">
                                {skill}
                            </span>
                        )
                    ))}
                </div>
            </div>

        </div>

        {/* Cột phải: About & Experience */}
        <div className="lg:col-span-2 space-y-6">
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <h3 className="font-semibold text-gray-800 mb-4 flex items-center gap-2">
                    <Book size={18} className="text-orange-500"/> Giới thiệu bản thân
                </h3>
                <textarea 
                    name="aboutMe" rows={4}
                    value={formData.aboutMe} onChange={handleChange}
                    className="w-full p-3 border rounded-lg focus:border-blue-500 outline-none text-sm leading-relaxed"
                    placeholder="Mục tiêu nghề nghiệp, điểm mạnh..."
                />
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
                <div className="flex justify-between items-center mb-6">
                    <h3 className="font-semibold text-gray-800 flex items-center gap-2">
                        <Briefcase size={18} className="text-teal-600"/> Kinh nghiệm làm việc
                    </h3>
                    <button type="button" onClick={addExperience} className="text-sm flex items-center gap-1 text-blue-600 bg-blue-50 px-3 py-1.5 rounded hover:bg-blue-100 transition">
                        <Plus size={16}/> Thêm mới
                    </button>
                </div>

                <div className="space-y-6">
                    {experiences.map((exp, index) => (
                        <div key={index} className="relative bg-gray-50 rounded-lg p-5 border border-gray-200 group">
                            <button onClick={() => removeExperience(index)} className="absolute top-4 right-4 text-gray-400 hover:text-red-500 p-1">
                                <Trash2 size={18}/>
                            </button>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                                <div>
                                    <label className="text-xs font-semibold text-gray-600 mb-1 block">Công ty</label>
                                    <input type="text" value={exp.companyName} onChange={(e) => handleExpChange(index, 'companyName', e.target.value)} className="w-full p-2 border rounded bg-white text-sm outline-none focus:border-blue-500"/>
                                </div>
                                <div>
                                    <label className="text-xs font-semibold text-gray-600 mb-1 block">Vị trí</label>
                                    <input type="text" value={exp.role} onChange={(e) => handleExpChange(index, 'role', e.target.value)} className="w-full p-2 border rounded bg-white text-sm outline-none focus:border-blue-500"/>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                                <div>
                                    <label className="text-xs font-semibold text-gray-600 mb-1 block">Bắt đầu</label>
                                    <input 
                                        type="month" 
                                        value={exp.startDate}
                                        onChange={(e) => handleExpChange(index, 'startDate', e.target.value)}
                                        className="w-full p-2 border rounded bg-white text-sm outline-none focus:border-blue-500"
                                    />
                                </div>
                                <div>
                                    <label className="text-xs font-semibold text-gray-600 mb-1 block">Kết thúc</label>
                                    <input 
                                        type="month" 
                                        value={exp.endDate}
                                        onChange={(e) => handleExpChange(index, 'endDate', e.target.value)}
                                        className="w-full p-2 border rounded bg-white text-sm outline-none focus:border-blue-500"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="text-xs font-semibold text-gray-600 mb-1 block">Mô tả công việc</label>
                                <textarea rows={3} value={exp.description} onChange={(e) => handleExpChange(index, 'description', e.target.value)} className="w-full p-2 border rounded bg-white text-sm outline-none focus:border-blue-500"/>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
      </div>
    </div>
  );
}