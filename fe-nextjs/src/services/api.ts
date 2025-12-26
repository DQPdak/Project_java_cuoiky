import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { getToken } from '@/utils/authStorage'; 

const api: AxiosInstance = axios.create({
  // Đảm bảo bạn đã tạo file .env.local chứa NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1', 
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor: Tự động gắn Token vào mỗi request
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

export default api;