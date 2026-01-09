// fe-nextjs/src/middleware.ts
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// Định nghĩa các route cần bảo vệ và role tương ứng
const roleRoutes: Record<string, string[]> = {
  '/admin': ['ADMIN'],
  '/dashboard-recruiter': ['RECRUITER'],
  '/dashboard-candidate': ['CANDIDATE'],
};

const authRoutes = ['/login', '/register'];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  
  // Lấy token và role từ cookie
  const token = request.cookies.get('accessToken')?.value;
  const userRole = request.cookies.get('userRole')?.value;

  // 1. Nếu chưa đăng nhập mà cố vào trang bảo mật -> Đá về Login
  const isProtectedRoute = Object.keys(roleRoutes).some(route => pathname.startsWith(route));
  
  if (isProtectedRoute && !token) {
    const url = new URL('/login', request.url);
    // Lưu lại url muốn vào để redirect lại sau khi login xong (tuỳ chọn)
    return NextResponse.redirect(url);
  }

  // 2. Nếu đã đăng nhập mà cố vào trang Login/Register -> Đá về dashboard tương ứng
  if (token && authRoutes.includes(pathname)) {
    if (userRole === 'ADMIN') return NextResponse.redirect(new URL('/admin/dashboard', request.url));
    if (userRole === 'RECRUITER') return NextResponse.redirect(new URL('/dashboard-recruiter', request.url));
    return NextResponse.redirect(new URL('/dashboard-candidate', request.url));
  }

  // 3. Kiểm tra quyền truy cập (Role-based access control)
  if (token && userRole) {
    // Nếu vào route /admin mà không phải ADMIN
    if (pathname.startsWith('/admin') && userRole !== 'ADMIN') {
       return NextResponse.redirect(new URL('/403', request.url)); // Hoặc đá về home
    }
    // Nếu vào route /dashboard-recruiter mà không phải RECRUITER
    if (pathname.startsWith('/dashboard-recruiter') && userRole !== 'RECRUITER') {
       return NextResponse.redirect(new URL('/403', request.url));
    }
    // Nếu vào route /dashboard-candidate mà không phải CANDIDATE
    if (pathname.startsWith('/dashboard-candidate') && userRole !== 'CANDIDATE') {
       return NextResponse.redirect(new URL('/403', request.url));
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/admin/:path*',
    '/dashboard-candidate/:path*',
    '/dashboard-recruiter/:path*',
    '/login',
    '/register'
  ],
};