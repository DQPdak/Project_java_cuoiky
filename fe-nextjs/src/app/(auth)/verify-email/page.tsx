"use client";

import { useState, Suspense, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { verifyEmail } from "@/services/authService";
import toast, { Toaster } from "react-hot-toast";
import Link from "next/link";

function VerifyEmailForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  
  // Lấy email từ URL: localhost:3000/verify-email?email=abc@gmail.com
  const emailFromUrl = searchParams.get("email") || "";

  const [email, setEmail] = useState("");
  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);

  // Cập nhật state khi URL thay đổi (hoặc khi load trang lần đầu)
  useEffect(() => {
    if (emailFromUrl) {
      setEmail(emailFromUrl);
    }
  }, [emailFromUrl]);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!email || !code) {
      toast.error("Vui lòng nhập đầy đủ Email và Mã xác thực");
      return;
    }

    setLoading(true);
    const loadingToast = toast.loading("Đang kiểm tra mã...");

    try {
      // Gọi API verifyEmail đã viết trong authService
      await verifyEmail(email, code);

      toast.dismiss(loadingToast);
      toast.success("Xác thực thành công! Đang chuyển hướng...", {
        duration: 3000,
        style: {
          borderRadius: "10px",
          background: "#333",
          color: "#fff",
        },
      });

      // Chuyển về trang đăng nhập sau 2 giây
      setTimeout(() => {
        router.push("/login");
      }, 2000);

    } catch (err: any) {
      toast.dismiss(loadingToast);
      console.error("Verify Error:", err);
      
      const msg = err.response?.data?.message || "Mã xác thực không chính xác hoặc đã hết hạn.";
      
      toast.error(msg, {
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

      <div className="text-center mb-8">
        <h2 className="text-3xl font-bold text-gray-900">Xác thực tài khoản</h2>
        <p className="mt-2 text-sm text-gray-600">
          Chúng tôi đã gửi mã 6 số đến email: <br />
          <span className="font-medium text-blue-600">{email || "..."}</span>
        </p>
      </div>

      <form className="space-y-6" onSubmit={handleVerify}>
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700">
            Email nhận mã
          </label>
          <div className="mt-1">
            <input
              id="email"
              type="email"
              required
              className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
        </div>

        <div>
          <label htmlFor="code" className="block text-sm font-medium text-gray-700">
            Mã xác thực (6 ký tự)
          </label>
          <div className="mt-1">
            <input
              id="code"
              type="text"
              required
              maxLength={6}
              placeholder="VD: A1B2C3"
              className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm tracking-widest uppercase font-bold text-center"
              value={code}
              onChange={(e) => setCode(e.target.value.toUpperCase())}
            />
          </div>
        </div>

        <div>
          <button
            type="submit"
            disabled={loading}
            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-colors"
          >
            {loading ? "Đang xác thực..." : "Kích hoạt tài khoản"}
          </button>
        </div>
      </form>

      <div className="mt-6 text-center">
        <p className="text-sm text-gray-600">
          Chưa nhận được mã?{" "}
          <button 
            type="button"
            className="font-medium text-blue-600 hover:text-blue-500"
            onClick={() => toast("Tính năng gửi lại đang phát triển", { icon: "🚧" })}
          >
            Gửi lại
          </button>
        </p>
        <div className="mt-4">
          <Link href="/login" className="text-sm font-medium text-gray-500 hover:text-gray-900">
            ← Quay lại đăng nhập
          </Link>
        </div>
      </div>
    </div>
  );
}

// Bắt buộc phải bọc trong Suspense khi dùng useSearchParams trong Next.js App Router
export default function VerifyPage() {
  return (
    <Suspense fallback={<div className="text-center p-10">Đang tải...</div>}>
      <VerifyEmailForm />
    </Suspense>
  );
}