"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { getToken, removeToken } from "@/utils/authStorage";
import { useRouter } from "next/navigation";
import { User } from "@/types/auth"; // Import từ types chung

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (userData: User) => void; // Đơn giản hóa params vì Token đã lưu ở Service
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const initAuth = async () => {
      const token = getToken();
      if (token) {
        // Có thể thêm logic gọi fetchCurrentUser() ở đây để xác thực token còn sống không
        // Tạm thời lấy từ localStorage nếu bạn lưu user info (để tránh F5 mất data)
        const storedUser = localStorage.getItem('currentUser');
        if (storedUser) {
           setUser(JSON.parse(storedUser));
        }
      }
      setIsLoading(false);
    };
    initAuth();
  }, []);

  // Hàm login chỉ cập nhật State và Redirect (Token đã lưu ở Service)
  const login = (userData: User) => {
    setUser(userData);
    localStorage.setItem('currentUser', JSON.stringify(userData)); // Lưu tạm info

    // Check quyền dựa trên field 'userRole' (khớp với BE)
    if (userData.userRole === 'RECRUITER') router.push('/dashboard-recruiter');
    else if (userData.userRole === 'ADMIN') router.push('/dashboard-admin');
    else router.push('/dashboard-candidate');
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