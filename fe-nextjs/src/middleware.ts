import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const protectedRoutes = ['/dashboard-candidate', '/dashboard-recruiter', '/dashboard-admin'];
const authRoutes = ['/login', '/register'];

export function middleware(request: NextRequest) {
  // // Lấy token từ Cookie (tên cookie phải khớp với 'accessToken' trong authStorage)
  // const token = request.cookies.get('accessToken')?.value;
  // const { pathname } = request.nextUrl;

  // // 1. Chưa đăng nhập mà vào Dashboard -> Về Login
  // if (!token && protectedRoutes.some(route => pathname.startsWith(route))) {
  //   const url = new URL('/login', request.url);
  //   // Có thể thêm ?callbackUrl=... để redirect lại sau khi login
  //   return NextResponse.redirect(url);
  // }

  // // 2. Đã đăng nhập mà vào Login/Register -> Về Dashboard mặc định
  // if (token && authRoutes.includes(pathname)) {
  //   // Lý tưởng nhất: Decode token để biết role và redirect đúng dashboard
  //   // Tạm thời redirect về candidate, AuthContext ở client sẽ redirect lại nếu sai role
  //   return NextResponse.redirect(new URL('/dashboard-candidate', request.url));
  // }

  // return NextResponse.next();
}

export const config = {
  matcher: [
    '/dashboard-candidate/:path*',
    '/dashboard-recruiter/:path*',
    '/dashboard-admin/:path*',
    '/login',
    '/register'
  ],
};