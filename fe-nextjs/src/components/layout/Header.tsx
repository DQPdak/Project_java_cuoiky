"use client";

import React from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  Briefcase,
  FileText,
  LayoutDashboard,
  Bell,
  Users,
  Settings,
  Building,
  PlusCircle,
  Shield,
} from "lucide-react";
import { useAuth } from "@/context/Authcontext";
import UserMenu from "@/components/features/auth/UserMenu";

// 1. Định nghĩa cấu trúc Item menu
interface NavItem {
  label: string;
  href: string;
  icon: React.ElementType;
}

// 2. Cấu hình Menu cho từng Role
const ROLE_MENUS: Record<string, NavItem[]> = {
  // Menu cho ỨNG VIÊN
  CANDIDATE: [
    { label: "Tổng quan", href: "/dashboard-candidate", icon: LayoutDashboard },
    { label: "Việc làm", href: "/jobs", icon: Briefcase },
    { label: "Phỏng vấn", href: "/interview", icon: Users }, // Ví dụ thêm
  ],

  // Menu cho NHÀ TUYỂN DỤNG
  RECRUITER: [
    { label: "Tổng quan", href: "/dashboard-recruiter", icon: LayoutDashboard },
    { label: "Đăng tin", href: "/post-job", icon: PlusCircle },
    { label: "Ứng viên", href: "applications", icon: Users },
    { label: "Công ty", href: "/company", icon: Building },
  ],

  // Menu cho ADMIN
  ADMIN: [
    { label: "Dashboard", href: "/admin/dashboard", icon: LayoutDashboard },
    { label: "Người dùng", href: "/admin/users", icon: Users },
    { label: "Nội dung", href: "/admin/content", icon: FileText },
    { label: "Cài đặt", href: "/admin/settings", icon: Settings },
    { label: "Phân quyền", href: "/admin/roles", icon: Shield },
  ],
};

export default function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const { user } = useAuth(); // Lấy thông tin user từ Context

  // 3. Logic lấy menu dựa trên Role
  // Nếu chưa đăng nhập hoặc không có role, trả về mảng rỗng (hoặc menu cho khách)
  const currentNavItems = user?.userRole ? ROLE_MENUS[user.userRole] || [] : [];

  const isActive = (path: string) =>
    pathname === path || pathname.startsWith(path + "/")
      ? "text-blue-600 border-b-2 border-blue-600"
      : "text-gray-500 hover:text-gray-700 hover:border-b-2 hover:border-gray-300";

  return (
    <nav className="bg-white shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          {/* Left Side: Logo & Menu */}
          <div className="flex">
            {/* Logo - Điều hướng về trang chủ tương ứng */}
            <div
              className="flex-shrink-0 flex items-center gap-2 cursor-pointer"
              onClick={() => {
                if (user?.userRole === "RECRUITER")
                  router.push("/dashboard-recruiter");
                else if (user?.userRole === "ADMIN")
                  router.push("/admin/dashboard");
                else router.push("/dashboard-candidate");
              }}
            >
              <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold">
                C
              </div>
              <span className="font-bold text-xl text-gray-800">
                CareerMate
              </span>
            </div>

            {/* Dynamic Menu Links */}
            <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
              {currentNavItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`inline-flex items-center px-1 pt-1 text-sm font-medium transition-colors duration-200 ${isActive(item.href)}`}
                >
                  <item.icon className="w-4 h-4 mr-2" />
                  {item.label}
                </Link>
              ))}
            </div>
          </div>

          {/* Right Side: Bell & UserMenu */}
          <div className="flex items-center gap-4">
            {user && (
              <button
                aria-label="Thông báo"
                className="p-2 rounded-full text-gray-400 hover:text-gray-500 transition-colors"
              >
                <Bell className="w-6 h-6" />
              </button>
            )}

            {/* User Dropdown Component (Đã xử lý logic login/logout bên trong) */}
            <UserMenu />
          </div>
        </div>
      </div>
    </nav>
  );
}
