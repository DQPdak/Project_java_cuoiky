"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/context/Authcontext";
import { useRouter } from "next/navigation";
import { paymentService } from "@/services/paymentService";
import { toast, Toaster } from "react-hot-toast";
import { useConfirm } from "@/context/ConfirmDialogContext"; // Đảm bảo bạn đã tạo Context này

// --- CẤU HÌNH GÓI DỊCH VỤ ---
const VIP_PACKAGES = {
  CANDIDATE: {
    name: "Candidate Pro",
    description: "Nâng cấp hồ sơ, chinh phục nhà tuyển dụng với quyền năng AI.",
    price: "200k",
    period: "/ 30 ngày",
    themeGradient: "from-blue-600 via-indigo-700 to-purple-800",
    buttonGradient: "from-blue-500 to-indigo-600",
    features: [
      "AI Phân tích & Chấm điểm CV chi tiết",
      "Phỏng vấn thử 1-1 với AI (Mock Interview)",
      "Huy hiệu Ứng viên Tài năng (VIP)",
    ],
  },
  RECRUITER: {
    name: "Recruiter Premium",
    description:
      "Tìm kiếm nhân tài nhanh chóng, tối ưu hóa quy trình tuyển dụng.",
    price: "100k",
    period: "/ 30 ngày",
    themeGradient: "from-orange-600 via-red-700 to-pink-800",
    buttonGradient: "from-orange-500 to-red-600",
    features: ["Phân tích hồ sơ ứng viên bằng AI"],
  },
};

export default function VipUpgradePage() {
  const { user, updateUser } = useAuth();
  const router = useRouter();
  const confirm = useConfirm(); // Hook xác nhận đẹp
  const [loading, setLoading] = useState(false);
  const [checkingAuth, setCheckingAuth] = useState(true);

  // 1. Kiểm tra đăng nhập
  useEffect(() => {
    const timer = setTimeout(() => {
      if (!user) {
        const callbackUrl = encodeURIComponent("/vip-upgrade");
        router.push(`/login?callbackUrl=${callbackUrl}`);
      } else {
        setCheckingAuth(false);
      }
    }, 500);
    return () => clearTimeout(timer);
  }, [user, router]);

  if (checkingAuth)
    return (
      <div className="min-h-screen flex items-center justify-center font-medium text-gray-500">
        Đang tải thông tin gói...
      </div>
    );

  // 2. Xác định Role & Gói
  const isVip = user?.userRole?.includes("_VIP");
  // Nếu là Recruiter thì dùng gói RECRUITER, mặc định là CANDIDATE
  const packageType = user?.userRole?.includes("RECRUITER")
    ? "RECRUITER"
    : "CANDIDATE";
  const currentPkg = VIP_PACKAGES[packageType];

  const expiryDate = user?.vipExpirationDate
    ? new Date(user.vipExpirationDate).toLocaleDateString("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      })
    : null;

  // 3. Xử lý thanh toán
  const handlePayment = async () => {
    // A. Sử dụng Confirm Dialog đẹp thay cho window.confirm
    const isConfirmed = await confirm({
      title: isVip ? "Xác nhận gia hạn VIP" : "Xác nhận nâng cấp VIP",
      message: isVip
        ? `Gói ${currentPkg.name} của bạn sẽ được gia hạn thêm 30 ngày. Bạn có muốn tiếp tục?`
        : `Bạn sắp đăng ký gói ${currentPkg.name}. Bạn sẽ được mở khóa toàn bộ tính năng cao cấp ngay lập tức.`,
      confirmLabel: "Thanh toán ngay",
      cancelLabel: "Để sau",
      isDanger: false,
    });

    if (!isConfirmed) return;

    setLoading(true);
    const toastId = toast.loading("Đang xử lý giao dịch...");

    try {
      // B. Gọi API
      const authData = await paymentService.upgradeToVip();

      // C. Cập nhật Context
      updateUser(authData.user);

      // D. Hiển thị thông báo Thành công (Custom UI)
      toast.dismiss(toastId);
      toast.custom(
        (t) => (
          <div
            className={`${t.visible ? "animate-enter" : "animate-leave"} 
          max-w-md w-full bg-white shadow-2xl rounded-2xl ring-1 ring-black ring-opacity-5 overflow-hidden border-2 border-yellow-400`}
          >
            <div className="p-4 flex items-start">
              <div className="flex-shrink-0 pt-0.5">
                <div className="h-12 w-12 bg-yellow-100 rounded-full flex items-center justify-center animate-bounce">
                  <span className="text-2xl">👑</span>
                </div>
              </div>
              <div className="ml-3 flex-1">
                <p className="text-lg font-bold text-gray-900">
                  {isVip ? "Gia hạn thành công!" : "Chào mừng VIP mới!"}
                </p>
                <p className="mt-1 text-sm text-gray-500">
                  Quyền lợi <b>{currentPkg.name}</b> đã được kích hoạt.
                </p>
              </div>
            </div>
            <button
              onClick={() => toast.dismiss(t.id)}
              className="w-full border-t border-gray-100 p-3 flex justify-center text-sm font-bold text-indigo-600 hover:bg-gray-50 transition-colors"
            >
              Tuyệt vời
            </button>
          </div>
        ),
        { duration: 5000 },
      );

      router.refresh();
    } catch (error: any) {
      toast.dismiss(toastId);

      const msg =
        error.response?.data?.message ||
        error.response?.data ||
        "Giao dịch thất bại";

      // E. Hiển thị thông báo Lỗi (Custom UI)
      toast.custom(
        (t) => (
          <div
            className={`${t.visible ? "animate-enter" : "animate-leave"} 
          max-w-md w-full bg-white shadow-xl rounded-xl border-l-4 border-red-500 flex ring-1 ring-black ring-opacity-5`}
          >
            <div className="flex-shrink-0 p-4">
              <svg
                className="h-6 w-6 text-red-500"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div className="p-4 pl-0 w-full">
              <h3 className="font-bold text-gray-900">Giao dịch thất bại</h3>
              <p className="text-sm text-gray-500 mt-1">
                {typeof msg === "string" ? msg : "Lỗi hệ thống"}
              </p>

              {/* Gợi ý lỗi DB thường gặp */}
              {JSON.stringify(msg).includes("constraint") && (
                <p className="text-xs text-red-600 mt-2 font-mono bg-red-50 p-1 rounded border border-red-100">
                  Lỗi DB: Vui lòng chạy lệnh SQL sửa Role
                </p>
              )}
            </div>
            <button
              onClick={() => toast.dismiss(t.id)}
              className="ml-auto p-4 text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          </div>
        ),
        { duration: 5000 },
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8 flex justify-center items-center font-sans">
      <Toaster position="top-center" reverseOrder={false} />

      <div className="max-w-5xl w-full bg-white rounded-3xl shadow-2xl overflow-hidden flex flex-col md:flex-row min-h-[600px] transition-all hover:shadow-[0_20px_60px_rgba(0,0,0,0.15)]">
        {/* CỘT TRÁI: THÔNG TIN GÓI */}
        <div
          className={`w-full md:w-1/2 p-10 bg-gradient-to-br ${currentPkg.themeGradient} text-white flex flex-col justify-between relative overflow-hidden`}
        >
          {/* Họa tiết nền */}
          <div className="absolute top-0 left-0 w-full h-full opacity-20 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')]"></div>

          <div className="relative z-10">
            <div className="inline-block px-3 py-1 bg-white/20 rounded-full border border-white/30 mb-4 backdrop-blur-md">
              <span className="text-white text-xs font-bold tracking-wider uppercase">
                Gói{" "}
                {packageType === "CANDIDATE" ? "Ứng viên" : "Nhà tuyển dụng"}
              </span>
            </div>
            <h2 className="text-4xl font-extrabold mb-3 drop-shadow-md">
              {currentPkg.name}
            </h2>
            <p className="text-white/80 text-lg mb-8 leading-relaxed font-light">
              {currentPkg.description}
            </p>

            <div className="space-y-6">
              {currentPkg.features.map((feature, index) => (
                <FeatureItem key={index} text={feature} />
              ))}
            </div>
          </div>

          <div className="relative z-10 mt-10 pt-8 border-t border-white/20">
            <p className="text-sm text-white/60 mb-1 font-medium">
              Giá niêm yết
            </p>
            <div className="flex items-baseline gap-2">
              <span className="text-5xl font-bold text-white tracking-tight">
                {currentPkg.price}
              </span>
              <span className="text-white/60 font-medium">
                {currentPkg.period}
              </span>
            </div>
          </div>
        </div>

        {/* CỘT PHẢI: HÀNH ĐỘNG */}
        <div className="w-full md:w-1/2 p-10 flex flex-col justify-center items-center text-center bg-white relative">
          {isVip ? (
            <div className="bg-green-50 p-8 rounded-2xl border border-green-100 w-full mb-8 shadow-sm transition-transform hover:scale-[1.02]">
              <div className="text-6xl mb-4 animate-bounce">👑</div>
              <h3 className="text-2xl font-bold text-green-800 mb-2">
                Đang là thành viên VIP
              </h3>
              <p className="text-gray-600 mb-4">
                Hết hạn vào:{" "}
                <span className="font-bold text-gray-900">{expiryDate}</span>
              </p>
              <div className="h-2 w-full bg-green-200 rounded-full overflow-hidden">
                <div className="h-full bg-green-500 w-3/4 animate-pulse"></div>
              </div>
              <p className="text-xs text-green-600 mt-2 text-left font-semibold">
                Trạng thái: Hoạt động
              </p>
            </div>
          ) : (
            <div className="bg-gray-50 p-8 rounded-2xl border border-gray-100 w-full mb-8 shadow-sm">
              <div className="text-6xl mb-4 transform transition hover:rotate-12">
                {packageType === "CANDIDATE" ? "🚀" : "💎"}
              </div>
              <h3 className="text-xl font-bold text-gray-800 mb-2">
                Tài khoản thường
              </h3>
              <p className="text-gray-500 leading-relaxed">
                Nâng cấp ngay để mở khóa toàn bộ quyền năng của hệ thống.
              </p>
            </div>
          )}

          <button
            onClick={handlePayment}
            disabled={loading}
            className={`w-full py-5 px-6 rounded-xl font-bold text-lg text-white shadow-xl transition-all transform hover:-translate-y-1 active:scale-95 bg-gradient-to-r ${currentPkg.buttonGradient} ${loading ? "opacity-70 cursor-not-allowed" : ""}`}
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <svg
                  className="animate-spin h-5 w-5 text-white"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                Đang xử lý...
              </span>
            ) : isVip ? (
              "GIA HẠN NGAY"
            ) : (
              "NÂNG CẤP NGAY"
            )}
          </button>

          <p className="mt-6 text-xs text-gray-400 max-w-xs mx-auto flex items-center justify-center gap-1">
            <svg
              className="w-3 h-3"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
              ></path>
            </svg>
            Thanh toán an toàn (Demo Mode)
          </p>
        </div>
      </div>
    </div>
  );
}

// Sub-component hiển thị quyền lợi
function FeatureItem({ text }: { text: string }) {
  return (
    <div className="flex items-start gap-3 group">
      <div className="flex-shrink-0 mt-1 w-6 h-6 rounded-full bg-white/20 text-yellow-300 flex items-center justify-center group-hover:bg-yellow-400 group-hover:text-black transition-colors duration-300">
        <svg
          className="w-4 h-4"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2.5"
            d="M5 13l4 4L19 7"
          ></path>
        </svg>
      </div>
      <span className="font-medium text-lg text-white/90 group-hover:text-white transition-colors duration-300">
        {text}
      </span>
    </div>
  );
}
