import axios from 'axios';
import { getToken } from '@/utils/authStorage';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// ✅ Request: gắn token
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

// ✅ Response: bắt bảo trì / unauthorized
api.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error?.response?.status;
    const data = error?.response?.data;

    // 1) Maintenance mode: 503
    if (status === 503) {
      const msg = data?.message || 'Hệ thống đang bảo trì, vui lòng thử lại sau.';

      if (typeof window !== 'undefined') {
        localStorage.setItem('maintenance_message', msg);
        // Nếu đang ở admin thì vẫn cho admin làm việc (tuỳ bạn)
        // ✅ Nếu đang ở trang /login thì KHÔNG redirect
        if (!window.location.pathname.startsWith('/login')) {
              window.location.href = '/maintenance';
        }
      }
      return Promise.reject(error);
    }

    // 2) Unauthorized: 401 -> về login (tuỳ bạn)
    if (status === 401) {
      if (typeof window !== 'undefined') {
        // tránh loop nếu đang ở trang login
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href = '/login';
        }
      }
      return Promise.reject(error);
    }

    return Promise.reject(error);
  }
);

export default api;
