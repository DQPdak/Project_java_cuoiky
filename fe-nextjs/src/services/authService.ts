import api from './api';
import { 
  RegisterRequest, 
  LoginRequest, 
  AuthResponseData, 
  BackendResponse 
} from '@/types/auth';
import { setToken, setRefreshToken, removeToken } from '@/utils/authStorage';

// Đăng ký
export const register = async (data: RegisterRequest): Promise<BackendResponse<AuthResponseData>> => {
  // Gọi vào: http://localhost:8080/api/auth/register
  const response = await api.post<BackendResponse<AuthResponseData>>('/auth/register', data);
  return response.data;
};

// Đăng nhập
export const login = async (data: LoginRequest): Promise<AuthResponseData> => {
  // Gọi vào: http://localhost:8080/api/auth/login
  const response = await api.post<BackendResponse<AuthResponseData>>('/auth/login', data);
  
  // Backend trả về: { message: "...", data: { accessToken, ... } }
  const authData = response.data.data;

  if (authData?.accessToken) {
    setToken(authData.accessToken);
    setRefreshToken(authData.refreshToken);
  }

  return authData;
};

// Logout
export const logout = async () => {
  try {
    await api.post('/auth/logout');
  } catch (error) {
    console.error(error);
  } finally {
    removeToken();
    window.location.href = '/login';
  }
};