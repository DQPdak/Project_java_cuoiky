import api from './api';
import { AuthResponse, User, LoginRequest } from '@/types/auth';
import { setToken, setRefreshToken, removeToken } from '@/utils/authStorage';

// Interface riêng cho Register khớp với Request Body của BE
interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  userRole: string; // Backend yêu cầu 'userRole', KHÔNG phải 'role'
}

export const login = async (email: string, password: string): Promise<AuthResponse> => {
  const loginData: LoginRequest = { email, password };
  
  // Gọi API
  const response = await api.post('/auth/login', loginData);
  
  // Cấu trúc response của BE: { success: true, message: "...", data: { accessToken, ... } }
  // Chúng ta cần lấy data bên trong
  const authData = response.data.data; 

  if (authData?.accessToken) {
    setToken(authData.accessToken);
    if (authData.refreshToken) {
      setRefreshToken(authData.refreshToken);
    }
  }
  
  return authData; // Trả về AuthResponse chứa cả User info
};

export const register = async (data: RegisterRequest): Promise<any> => {
  // Gọi API Register
  const response = await api.post('/auth/register', data);
  return response.data;
};

export const fetchCurrentUser = async (): Promise<User> => {
  const response = await api.get('/users/me');
  return response.data.data; // Lấy data từ MessageResponse
};

export const logout = () => {
  removeToken();
  // api.post('/auth/logout'); // Gọi thêm nếu cần
};