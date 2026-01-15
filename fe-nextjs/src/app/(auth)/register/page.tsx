"use client";

import { useState, FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { register } from "@/services/authService";
import toast, { Toaster } from "react-hot-toast";
import { UserRole } from "@/types/auth"; // Import Enum UserRole
import GoogleLoginButton from "@/components/features/auth/GoogleLoginButton";

export default function RegisterPage() {
  const router = useRouter();

  // State cho form
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
    userRole: "CANDIDATE", // Giá trị mặc định là string
  });

  const [loading, setLoading] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e: FormEvent) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      toast.error("Mật khẩu xác nhận không khớp!");
      return;
    }

    setLoading(true);
    const loadingToast = toast.loading("Đang xử lý đăng ký...");

    try {
      await register({
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
        // --- SỬA LỖI TẠI ĐÂY: Ép kiểu string thành UserRole ---
        userRole: formData.userRole as UserRole,
      });

      toast.dismiss(loadingToast);
      toast.success("Đăng ký thành công! Vui lòng kiểm tra email.");

      // Chuyển sang trang login sau 2 giây
      setTimeout(() => router.push("/login"), 2000);
    } catch (err: any) {
      toast.dismiss(loadingToast);
      const msg = err.response?.data?.message || "Đăng ký thất bại.";
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md mx-auto p-6 bg-white rounded-lg shadow-md my-10">
      <Toaster position="top-center" />

      <h2 className="mb-6 text-center text-2xl font-bold text-gray-900">
        Đăng ký tài khoản mới
      </h2>

      <form className="space-y-4" onSubmit={handleRegister}>
        {/* --- Họ tên --- */}
        <div>
          <label
            htmlFor="fullName"
            className="block text-sm font-medium text-gray-700"
          >
            Họ và tên
          </label>
          <input
            id="fullName"
            name="fullName"
            type="text"
            required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            onChange={handleChange}
          />
        </div>

        {/* --- Email --- */}
        <div>
          <label
            htmlFor="email"
            className="block text-sm font-medium text-gray-700"
          >
            Email
          </label>
          <input
            id="email"
            name="email"
            type="email"
            required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            onChange={handleChange}
          />
        </div>

        {/* --- Mật khẩu --- */}
        <div>
          <label
            htmlFor="password"
            className="block text-sm font-medium text-gray-700"
          >
            Mật khẩu
          </label>
          <input
            id="password"
            name="password"
            type="password"
            required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            onChange={handleChange}
          />
        </div>

        {/* --- Xác nhận mật khẩu --- */}
        <div>
          <label
            htmlFor="confirmPassword"
            className="block text-sm font-medium text-gray-700"
          >
            Xác nhận mật khẩu
          </label>
          <input
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            onChange={handleChange}
          />
        </div>

        {/* --- Chọn vai trò --- */}
        <div>
          <label
            htmlFor="userRole"
            className="block text-sm font-medium text-gray-700"
          >
            Bạn là?
          </label>
          <select
            id="userRole"
            name="userRole"
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            onChange={handleChange}
            value={formData.userRole}
          >
            <option value="CANDIDATE">Ứng viên tìm việc</option>
            <option value="RECRUITER">Nhà tuyển dụng</option>
          </select>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none disabled:opacity-50 transition-colors"
        >
          {loading ? "Đang xử lý..." : "Đăng ký"}
        </button>
      </form>

      {/* --- PHẦN NÚT ĐĂNG KÝ BẰNG GOOGLE --- */}
      <div className="mt-6">
        <div className="relative">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-gray-300"></div>
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="px-2 bg-white text-gray-500">
              Hoặc đăng ký với
            </span>
          </div>
        </div>

        <div className="mt-4">
          <GoogleLoginButton textType="signup_with" />
        </div>
      </div>

      <p className="mt-6 text-center text-sm text-gray-600">
        Đã có tài khoản?{" "}
        <Link
          href="/login"
          className="font-medium text-blue-600 hover:text-blue-500"
        >
          Đăng nhập ngay
        </Link>
      </p>
    </div>
  );
}
