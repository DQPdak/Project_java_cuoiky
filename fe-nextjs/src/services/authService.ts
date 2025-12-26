import api from './api';
import { AuthResponse, User, LoginRequest } from '@/types/auth';
import { setToken, setRefreshToken, removeToken } from '@/utils/authStorage';

// Thêm interface cho Register data (nếu chưa có trong types/auth.ts thì khai báo tạm ở đây hoặc bổ sung vào file types)
interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role: string; // 'CANDIDATE' | 'RECRUITER'
}

// Gọi API Login
export const login = async (email: string, password: string): Promise<AuthResponse> => {
  const loginData: LoginRequest = { email, password };
  
  // Endpoint này phải khớp với AuthController.java (@PostMapping("/auth/login"))
  const response = await api.post<AuthResponse>('/auth/login', loginData);
  
  // Lưu token sau khi login thành công
  if (response.data.accessToken) {
    setToken(response.data.accessToken);
    if (response.data.refreshToken) {
        setRefreshToken(response.data.refreshToken);
    }
  }
  
  return response.data;
};

// --- THÊM MỚI TỪ ĐÂY ---
// Gọi API Register
export const register = async (data: RegisterRequest): Promise<any> => {
  // Endpoint khớp với AuthController.java
  const response = await api.post('/auth/register', data);
  return response.data;
};
// --- KẾT THÚC THÊM MỚI ---

// Gọi API lấy thông tin User hiện tại
export const fetchCurrentUser = async (): Promise<User> => {
  // Endpoint này phải khớp với UserController.java (@GetMapping("/users/me"))
  const response = await api.get<User>('/users/me');
  return response.data;
};

export const logout = () => {
  removeToken();
  // Có thể gọi thêm API logout nếu backend yêu cầu
};