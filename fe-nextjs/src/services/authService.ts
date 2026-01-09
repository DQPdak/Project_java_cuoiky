import api from './api';
import { 
  RegisterRequest, 
  LoginRequest, 
  AuthResponseData, 
  BackendResponse 
} from '@/types/auth';
import { setToken, setRefreshToken, removeToken, setUserRole } from '@/utils/authStorage';

// Đăng ký
export const register = async (data: RegisterRequest): Promise<BackendResponse<AuthResponseData>> => {
  // Gọi vào: http://localhost:8080/api/auth/register
  const response = await api.post<BackendResponse<AuthResponseData>>('/auth/register', data);
  return response.data;
};

// Đăng nhập
export const login = async (data: LoginRequest): Promise<AuthResponseData> => {
  const response = await api.post<BackendResponse<AuthResponseData>>('/auth/login', data);
  const authData = response.data.data;

  if (authData?.accessToken) {
    setToken(authData.accessToken);
    setRefreshToken(authData.refreshToken);
    setUserRole(authData.user.userRole);
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