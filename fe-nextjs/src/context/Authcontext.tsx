// fe-nextjs/src/context/Authcontext.tsx
"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { getToken, removeToken, getUserRole } from "@/utils/authStorage";
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
        setUser(JSON.parse(storedUser));
      } else {
        // Nếu không có token nhưng có user rác -> xóa đi
        if (!token) localStorage.removeItem('currentUser');
      }
      setIsLoading(false);
    };
    initAuth();
  }, []);

  const login = (userData: User) => {
    setUser(userData);
    localStorage.setItem('currentUser', JSON.stringify(userData));

    // ĐIỀU HƯỚNG DỰA TRÊN ROLE
    // Căn cứ vào cấu trúc thư mục bạn đã upload
    switch (userData.userRole) {
      case UserRole.ADMIN:
        router.push('/admin/dashboard'); 
        break;
      case UserRole.RECRUITER:
        router.push('/dashboard-recruiter');
        break;
      case UserRole.CANDIDATE:
      default:
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