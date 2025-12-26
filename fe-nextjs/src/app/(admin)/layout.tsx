'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, Users, FileText, Settings, Shield, LogOut } from 'lucide-react';
import { logout } from '@/services/authService';
import { useRouter } from 'next/navigation';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const navItems = [
    { name: 'Tổng quan', href: '/admin/dashboard', icon: LayoutDashboard },
    { name: 'Người dùng', href: '/admin/users', icon: Users },
    { name: 'Nội dung', href: '/admin/content', icon: FileText },
    { name: 'Phân quyền', href: '/admin/roles', icon: Shield },
    { name: 'Cài đặt', href: '/admin/settings', icon: Settings },
  ];

  return (
    <div className="flex min-h-screen bg-gray-100">
      {/* Sidebar - Dark Theme for Admin */}
      <aside className="w-64 bg-slate-900 text-white hidden md:flex flex-col fixed h-full">
        <div className="h-16 flex items-center px-6 border-b border-slate-700">
           <span className="text-xl font-bold tracking-wider">CM ADMIN</span>
        </div>
        
        <nav className="flex-1 p-4 space-y-1">
            {navItems.map((item) => {
                const Icon = item.icon;
                const isActive = pathname.startsWith(item.href);
                return (
                    <Link 
                        key={item.name} 
                        href={item.href}
                        className={`flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-colors ${
                            isActive 
                            ? 'bg-blue-600 text-white shadow-lg' 
                            : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                        }`}
                    >
                        <Icon className="w-5 h-5 mr-3" />
                        {item.name}
                    </Link>
                )
            })}
        </nav>

        <div className="p-4 border-t border-slate-700">
            <button 
                onClick={handleLogout}
                className="flex items-center w-full px-4 py-2 text-sm font-medium text-red-400 rounded-lg hover:bg-slate-800 transition"
            >
                <LogOut className="w-5 h-5 mr-3" />
                Thoát hệ thống
            </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 md:ml-64 p-8">
        {children}
      </main>
    </div>
  );
}