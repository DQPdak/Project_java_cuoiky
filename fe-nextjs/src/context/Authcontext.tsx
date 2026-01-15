"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { getToken, removeToken } from "@/utils/authStorage";
import { useRouter } from "next/navigation";
import { User, UserRole } from "@/types/auth";

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (userData: User) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // Logic khôi phục user khi F5 trang
    const initAuth = async () => {
      const token = getToken();
      const storedUser = localStorage.getItem('currentUser');
      
      if (token && storedUser) {
        try {
            setUser(JSON.parse(storedUser));
        } catch (e) {
            console.error("Lỗi parse user từ localStorage", e);
            localStorage.removeItem('currentUser');
        }
      } else {
        // Nếu không có token nhưng có user rác -> xóa đi
        if (!token) localStorage.removeItem('currentUser');
      }
      setIsLoading(false);
    };
    initAuth();
  }, []);

  const login = (userData: User) => {
    // --- KHỐI KIỂM TRA AN TOÀN (MỚI) ---
    // Ngăn chặn lỗi crash nếu userData bị null/undefined
    if (!userData) {
        console.error("❌ AuthContext: Hàm login được gọi nhưng không có dữ liệu user!", userData);
        return; 
    }

    if (!userData.userRole) {
        console.error("❌ AuthContext: User không có quyền (userRole)", userData);
        // Có thể mặc định gán role nếu cần thiết, hoặc return
        // return; 
    }
    // ------------------------------------

    console.log("✅ AuthContext: Đăng nhập thành công với user:", userData);

    setUser(userData);
    localStorage.setItem('currentUser', JSON.stringify(userData));

    // ĐIỀU HƯỚNG DỰA TRÊN ROLE
    // Sử dụng Optional Chaining (?) để an toàn hơn
    switch (userData?.userRole) {
      case UserRole.ADMIN:
        router.push('/admin/dashboard'); 
        break;
      case UserRole.RECRUITER:
        router.push('/dashboard-recruiter');
        break;
      case UserRole.CANDIDATE:
        router.push('/dashboard-candidate');
        break;
      default:
        // Nếu không có role hoặc role lạ, về trang chủ candidate
        console.warn("⚠️ Role không xác định, chuyển về trang Dashboard Candidate");
        router.push('/dashboard-candidate');
        break;
    }
  };

  const logout = () => {
    setUser(null);
    removeToken();
    localStorage.removeItem('currentUser');
    router.push("/login");
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};