'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Briefcase, FileText, User, Bell, LogOut, LayoutDashboard } from 'lucide-react';
import { logout } from '@/services/authService';
import { useRouter } from 'next/navigation';

export default function CandidateLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const isActive = (path: string) => pathname === path ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-500 hover:text-gray-700';

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navbar */}
      <nav className="bg-white shadow-sm sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex">
              <div className="flex-shrink-0 flex items-center gap-2 cursor-pointer" onClick={() => router.push('/dashboard')}>
                <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center text-white font-bold">C</div>
                <span className="font-bold text-xl text-gray-800">CareerMate</span>
              </div>
              <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
                <Link href="/dashboard" className={`inline-flex items-center px-1 pt-1 text-sm font-medium ${isActive('/dashboard')}`}>
                  <LayoutDashboard className="w-4 h-4 mr-2" /> Tổng quan
                </Link>
                <Link href="/jobs" className={`inline-flex items-center px-1 pt-1 text-sm font-medium ${isActive('/jobs')}`}>
                  <Briefcase className="w-4 h-4 mr-2" /> Việc làm
                </Link>
                <Link href="/cv-analysis" className={`inline-flex items-center px-1 pt-1 text-sm font-medium ${isActive('/cv-analysis')}`}>
                  <FileText className="w-4 h-4 mr-2" /> Phân tích CV
                </Link>
              </div>
            </div>
            <div className="flex items-center gap-4">
              <button className="p-2 rounded-full text-gray-400 hover:text-gray-500">
                <Bell className="w-6 h-6" />
              </button>
              <div className="relative flex items-center gap-2">
                <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold">
                  U
                </div>
                <button onClick={handleLogout} className="text-sm text-red-500 hover:text-red-700 font-medium">
                  Đăng xuất
                </button>
              </div>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        {children}
      </main>
    </div>
  );
}