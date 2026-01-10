"use client";

import { useState, FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { register } from "@/services/authService";
import { UserRole } from "@/types/auth";
import toast, { Toaster } from "react-hot-toast";

export default function RegisterPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
    role: UserRole.CANDIDATE,
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // --- HÀM KIỂM TRA MẬT KHẨU MỚI ---
  const validatePassword = (password: string) => {
    // Regex khớp với backend: Ít nhất 1 thường, 1 hoa, 1 số
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/;
    if (password.length < 6) return "Mật khẩu phải có ít nhất 6 ký tự.";
    if (!passwordRegex.test(password)) {
      return "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số.";
    }
    return null;
  };

  const handleRegister = async (e: FormEvent) => {
    e.preventDefault();

    // 1. Validate Client: Password khớp nhau
    if (formData.password !== formData.confirmPassword) {
      toast.error("Mật khẩu xác nhận không khớp!");
      return;
    }

    // 2. Validate Client: Độ mạnh mật khẩu (FIX LỖI 400)
    const passwordError = validatePassword(formData.password);
    if (passwordError) {
      toast.error(passwordError);
      return;
    }

    setLoading(true);
    const loadingToast = toast.loading("Đang tạo tài khoản...");

    try {
      const response = await register({
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
        userRole: formData.role,
      });

      toast.dismiss(loadingToast);
      toast.success(response.message || "Đăng ký thành công!", {
        duration: 3000,
        icon: "🎉",
        style: {
          borderRadius: "10px",
          background: "#333",
          color: "#fff",
        },
      });

      setTimeout(() => {
        router.push("/login");
      }, 1500);
      
    } catch (err: any) {
      toast.dismiss(loadingToast);
      console.error("Register Error:", err);

      // Lấy message lỗi từ Backend trả về (thường chứa chi tiết lỗi validation)
      // Backend thường trả về: { message: "Validation failed", errors: [...] } hoặc chuỗi text
      let msg = "Đăng ký thất bại.";
      
      if (err.response && err.response.data) {
          // Trường hợp Backend trả về message trực tiếp
          if (err.response.data.message) {
              msg = err.response.data.message;
          } 
          // Trường hợp Backend trả về danh sách lỗi validation (Spring Boot Default)
          else if (typeof err.response.data === 'string') {
              msg = err.response.data;
          }
      }

      toast.error(msg, {
        duration: 4000,
        style: {
          borderRadius: "10px",
          background: "#fee2e2",
          color: "#b91c1c",
          border: "1px solid #fca5a5",
        },
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md mx-auto p-6">
      <Toaster position="top-center" reverseOrder={false} />

      <h2 className="mb-6 text-center text-2xl font-bold text-gray-900">
        Tạo tài khoản mới
      </h2>

      <form className="space-y-4" onSubmit={handleRegister}>
        {/* ... Các trường nhập liệu giữ nguyên như cũ ... */}
        <div>
          <label htmlFor="fullName" className="block text-sm font-medium text-gray-700">Họ và tên</label>
          <input id="fullName" name="fullName" type="text" required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
            value={formData.fullName} onChange={handleChange}
          />
        </div>

        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email</label>
          <input id="email" name="email" type="email" required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
            value={formData.email} onChange={handleChange}
          />
        </div>

        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700">Mật khẩu</label>
          <input id="password" name="password" type="password" required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
            value={formData.password} onChange={handleChange}
            placeholder="VD: Matkhau123 (Có hoa, thường, số)"
          />
          {/* Gợi ý cho người dùng */}
          <p className="text-xs text-gray-500 mt-1">Mật khẩu cần ít nhất 1 chữ hoa, 1 thường và 1 số.</p>
        </div>

        <div>
          <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">Xác nhận mật khẩu</label>
          <input id="confirmPassword" name="confirmPassword" type="password" required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
            value={formData.confirmPassword} onChange={handleChange}
          />
        </div>

        <div>
          <label htmlFor="role" className="block text-sm font-medium text-gray-700">Bạn là?</label>
          <select id="role" name="role"
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm bg-white focus:ring-blue-500 focus:border-blue-500"
            value={formData.role} onChange={handleChange}
          >
            <option value={UserRole.CANDIDATE}>Ứng viên (Tìm việc)</option>
            <option value={UserRole.RECRUITER}>Nhà tuyển dụng (Đăng tin)</option>
          </select>
        </div>

        <div className="pt-2">
          <button type="submit" disabled={loading}
            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 transition-colors"
          >
            {loading ? "Đang xử lý..." : "Đăng ký"}
          </button>
        </div>
      </form>

      <p className="mt-6 text-center text-sm text-gray-600">
        Đã có tài khoản?{" "}
        <Link href="/login" className="font-medium text-blue-600 hover:text-blue-500">
          Đăng nhập ngay
        </Link>
      </p>
    </div>
  );
}