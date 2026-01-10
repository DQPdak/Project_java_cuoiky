import axios from 'axios';
import { getToken } from '@/utils/authStorage'; // Hàm lấy token từ localStorage/cookie

const api = axios.create({
  // Backend chạy port 8080, prefix chung là /api
  baseURL: 'http://localhost:8080/api', 
  headers: {
    'Content-Type': 'application/json',
  },
});

// Tự động gắn Token vào header nếu đã đăng nhập
api.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;