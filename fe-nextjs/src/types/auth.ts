export enum UserRole {
  ADMIN = 'ADMIN',
  RECRUITER = 'RECRUITER',
  CANDIDATE = 'CANDIDATE'
}

// 1. Khớp với UserResponse.java của BE
export interface User {
  id: number;
  email: string;
  fullName: string;
  userRole: UserRole; // SỬA: BE trả về 'userRole', không phải 'role'
  avatarUrl?: string; // BE: profileImageUrl (cần map lại nếu muốn dùng)
  phone?: string;
  status?: string;
}

// 2. Khớp với AuthResponse.java của BE
export interface AuthResponseData {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User; // BE đã trả về object User ở đây
}

// 3. Khớp với MessageResponse.java (Wrapper của BE)
export interface ApiResponse<T> {
  success?: boolean; // Tùy vào implementation của MessageResponse
  message: string;
  data: T;           // Dữ liệu thực nằm ở đây
}

export interface LoginRequest {
  email: string;
  password: string;
}