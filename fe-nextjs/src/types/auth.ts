export enum UserRole {
  CANDIDATE = 'CANDIDATE',
  RECRUITER = 'RECRUITER',
  ADMIN = 'ADMIN',
}

// Khớp với Java RegisterRequest
export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  userRole: UserRole; // Enum
}

// Khớp với Java LoginRequest
export interface LoginRequest {
  email: string;
  password: string;
}

// Khớp với Java User Entity (trả về trong AuthResponse)
export interface User {
  id: number;
  fullName: string;
  email: string;
  userRole: UserRole;
  profileImageUrl?: string;
}

// Khớp với Java AuthResponse
export interface AuthResponseData {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

// Khớp với Java MessageResponse (Wrapper)
export interface BackendResponse<T> {
  message: string;
  data: T; // Java: MessageResponse.success(..., data)
}