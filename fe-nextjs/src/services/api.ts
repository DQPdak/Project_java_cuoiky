import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios';
import { getToken, setToken, removeToken, getRefreshToken } from '@/utils/authStorage'; 

// Khai báo biến để tránh loop vô hạn khi refresh token lỗi
let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

const api: AxiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1', 
  headers: {
    'Content-Type': 'application/json',
  },
});

// Hàm helper để chạy lại các request bị pending khi đang refresh token
const onRefreshed = (token: string) => {
  refreshSubscribers.map((callback) => callback(token));
  refreshSubscribers = [];
};

const addSubscriber = (callback: (token: string) => void) => {
  refreshSubscribers.push(callback);
};

// 1. Request Interceptor: Gắn token vào header
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 2. Response Interceptor: Xử lý lỗi 401 và Refresh Token
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Nếu lỗi 401 và chưa từng retry
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Nếu đang refresh thì đợi
        return new Promise((resolve) => {
          addSubscriber((token: string) => {
            if (originalRequest.headers) {
              originalRequest.headers['Authorization'] = `Bearer ${token}`;
            }
            resolve(api(originalRequest));
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = getRefreshToken(); // Cần đảm bảo hàm này lấy đúng refresh token từ storage
        // Gọi API refresh token của Backend (theo endpoint trong AuthController)
        const response = await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/auth/refresh-token`, {
          refreshToken: refreshToken
        });

        const { accessToken } = response.data.data; // Cấu trúc response dựa trên AuthResponse của BE
        
        setToken(accessToken); // Lưu token mới
        onRefreshed(accessToken);
        isRefreshing = false;

        if (originalRequest.headers) {
          originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
        }
        return api(originalRequest);

      } catch (refreshError) {
        // Nếu refresh cũng lỗi -> Token hết hạn hẳn -> Logout
        isRefreshing = false;
        removeToken();
        window.location.href = '/login'; // Chuyển hướng về trang login
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default api;