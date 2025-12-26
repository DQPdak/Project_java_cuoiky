export enum UserRole {
  ADMIN = 'ADMIN',
  RECRUITER = 'RECRUITER',
  CANDIDATE = 'CANDIDATE'
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: UserRole;
  avatarUrl?: string;
  phone?: string;
  status?: string;
}

// Khớp với AuthResponse.java của Backend
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}