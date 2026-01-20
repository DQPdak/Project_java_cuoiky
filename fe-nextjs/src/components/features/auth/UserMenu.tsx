"use client";

import { useState, useRef, useEffect } from "react";
import Link from "next/link";
import { useAuth } from "@/context/Authcontext"; // Đảm bảo đường dẫn import đúng với file Authcontext của bạn
import { useRouter } from "next/navigation";

export default function UserMenu() {
  const { user, logout } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const router = useRouter();

  // Đóng menu khi click ra ngoài
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [menuRef]);

  const handleLogout = () => {
    logout();
    setIsOpen(false);
    // AuthContext thường đã xử lý redirect, nhưng thêm router.push cho chắc chắn
    router.push("/login");
  };

  if (!user) return null;

  // Xử lý avatar: ưu tiên avatar từ user, nếu không có thì dùng ảnh mặc định
  // Lưu ý: Kiểm tra xem User type của bạn dùng field 'avatarUrl' hay 'profileImageUrl'
  const userAvatar =
    user.profileImageUrl ||
    user.profileImageUrl ||
    "https://res.cloudinary.com/dpym64zg9/image/upload/v1768898865/phantichcv/avatar/gqwoyrmv8osjl5hjlygz.png";

  return (
    <div className="relative" ref={menuRef}>
      {/* Nút kích hoạt Dropdown */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-2 focus:outline-none hover:bg-gray-100 p-2 rounded-lg transition duration-150"
      >
        <img
          src={userAvatar}
          alt={user.fullName}
          className="w-8 h-8 md:w-10 md:h-10 rounded-full object-cover border border-gray-200"
        />
        <span className="font-medium text-gray-700 hidden md:block text-sm md:text-base">
          {user.fullName}
        </span>
        {/* Icon mũi tên nhỏ (Optional) */}
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className={`h-4 w-4 text-gray-500 transition-transform ${isOpen ? "rotate-180" : ""}`}
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M19 9l-7 7-7-7"
          />
        </svg>
      </button>

      {/* Menu Dropdown */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-56 bg-white rounded-md shadow-lg py-1 z-50 border border-gray-100 ring-1 ring-black ring-opacity-5">
          {/* Header của Menu */}
          <div className="px-4 py-3 border-b border-gray-100">
            <p className="text-sm font-semibold text-gray-900">
              {user.fullName}
            </p>
            <p className="text-xs text-gray-500 truncate">{user.email}</p>
            <p className="text-xs text-blue-600 mt-1 font-medium bg-blue-50 inline-block px-2 py-0.5 rounded">
              {/* Hiển thị Role */}
              {user.userRole === "CANDIDATE"
                ? "Ứng viên"
                : user.userRole === "RECRUITER"
                  ? "Nhà tuyển dụng"
                  : "Admin"}
            </p>
          </div>

          {/* Các lựa chọn */}
          <div className="py-1">
            {user.userRole !== "ADMIN" && (
              <Link
                href="/profile" // Hoặc '/candidate/profile' tùy route của bạn
                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 hover:text-blue-600"
                onClick={() => setIsOpen(false)}
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-4 w-4 mr-2"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                  />
                </svg>
                Chỉnh sửa profile
              </Link>
            )}

            {/* Link Dashboard tùy theo Role (Optional) */}
            {user.userRole !== "ADMIN" && (
              <Link
                href={
                  user.userRole === "RECRUITER"
                    ? "/dashboard-recruiter"
                    : "/dashboard-candidate"
                }
                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 hover:text-blue-600"
                onClick={() => setIsOpen(false)}
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-4 w-4 mr-2"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"
                  />
                </svg>
                Dashboard
              </Link>
            )}
          </div>

          <div className="border-t border-gray-100 py-1">
            <button
              onClick={handleLogout}
              className="flex w-full items-center px-4 py-2 text-sm text-red-600 hover:bg-red-50"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-4 w-4 mr-2"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                />
              </svg>
              Đăng xuất
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
