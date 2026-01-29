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
  Sparkles,
} from "lucide-react";
import { useAuth } from "@/context/Authcontext";
import UserMenu from "@/components/features/auth/UserMenu";

interface NavItem {
  label: string;
  href: string;
  icon: React.ElementType;
}

// 1. Tách Menu ra biến riêng để tái sử dụng cho VIP
const CANDIDATE_NAV: NavItem[] = [
  { label: "Tổng quan", href: "/dashboard-candidate", icon: LayoutDashboard },
  { label: "Việc làm", href: "/jobs", icon: Briefcase },
  { label: "Phỏng vấn", href: "/interview", icon: Users },
];

const RECRUITER_NAV: NavItem[] = [
  { label: "Tổng quan", href: "/dashboard-recruiter", icon: LayoutDashboard },
  { label: "Đăng tin", href: "/recruiter/manage-jobs", icon: PlusCircle },
  { label: "Ứng viên", href: "/applications", icon: Users }, // Lưu ý: thêm dấu / ở đầu để đúng path
  { label: "Công ty", href: "/recruiter/company", icon: Building },
  { label: "AI Matching", href: "/recruiter/candidates", icon: Sparkles },
];

const ADMIN_NAV: NavItem[] = [
  { label: "Dashboard", href: "/admin/dashboard", icon: LayoutDashboard },
  { label: "Người dùng", href: "/admin/users", icon: Users },
  { label: "Nội dung", href: "/admin/content", icon: FileText },
  { label: "Cài đặt", href: "/admin/settings", icon: Settings },
  { label: "Phân quyền", href: "/admin/roles", icon: Shield },
];

// 2. Map Role vào Menu (Thêm VIP vào đây)
const ROLE_MENUS: Record<string, NavItem[]> = {
  CANDIDATE: CANDIDATE_NAV,
  CANDIDATE_VIP: CANDIDATE_NAV, // VIP dùng chung menu candidate

  RECRUITER: RECRUITER_NAV,
  RECRUITER_VIP: RECRUITER_NAV, // VIP dùng chung menu recruiter

  ADMIN: ADMIN_NAV,
};

export default function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const { user } = useAuth();

  const currentNavItems = user?.userRole ? ROLE_MENUS[user.userRole] || [] : [];

  const isActive = (path: string) =>
    pathname === path || pathname.startsWith(path + "/")
      ? "text-blue-600 border-b-2 border-blue-600"
      : "text-gray-500 hover:text-gray-700 hover:border-b-2 hover:border-gray-300";

  // Hàm xử lý click Logo (Sửa lại logic so sánh Role)
  const handleLogoClick = () => {
    if (!user) {
      router.push("/");
      return;
    }

    // Dùng includes để bắt cả VIP
    if (user.userRole === "ADMIN") {
      router.push("/admin/dashboard");
    } else if (user.userRole.includes("RECRUITER")) {
      router.push("/dashboard-recruiter");
    } else {
      router.push("/dashboard-candidate");
    }
  };

  return (
    <nav className="bg-white shadow-sm sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          {/* Left Side: Logo & Menu */}
          <div className="flex">
            {/* Logo */}
            <div
              className="flex-shrink-0 flex items-center gap-2 cursor-pointer"
              onClick={handleLogoClick}
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
            <UserMenu />
          </div>
        </div>
      </div>
    </nav>
  );
}
