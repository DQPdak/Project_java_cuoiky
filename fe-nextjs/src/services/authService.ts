import api from "./api";
import {
  RegisterRequest,
  LoginRequest,
  AuthResponseData,
  BackendResponse,
} from "@/types/auth";
import {
  setToken,
  setRefreshToken,
  removeToken,
  setUserRole,
} from "@/utils/authStorage";

// --- ĐĂNG KÝ ---
export const register = async (
  data: RegisterRequest
): Promise<BackendResponse<AuthResponseData>> => {
  // Gọi API: POST /api/auth/register
  const response = await api.post<BackendResponse<AuthResponseData>>(
    "/auth/register",
    data
  );
  return response.data;
};

// --- ĐĂNG NHẬP ---
export const login = async (data: LoginRequest): Promise<AuthResponseData> => {
  const response = await api.post<BackendResponse<AuthResponseData>>(
    "/auth/login",
    data
  );
  const authData = response.data.data;

  // Lưu token nếu đăng nhập thành công
  if (authData?.accessToken) {
    setToken(authData.accessToken);
    setRefreshToken(authData.refreshToken);
    setUserRole(authData.user.userRole);
  }
  return authData;
};

// --- ĐĂNG XUẤT ---
export const logout = async () => {
  try {
    await api.post("/auth/logout");
  } catch (error) {
    console.error("Logout error:", error);
  } finally {
    removeToken();
    if (typeof window !== "undefined") {
      window.location.href = "/login";
    }
  }
};

// --- QUÊN MẬT KHẨU (Gửi mail) ---
export const forgotPassword = async (email: string) => {
  const response = await api.post("/auth/forgot-password", { email });
  return response.data;
};

// --- ĐẶT LẠI MẬT KHẨU (Nhập token) ---
export const resetPassword = async (token: string, newPassword: string) => {
  const response = await api.post("/auth/reset-password", {
    token,
    newPassword,
  });
  return response.data;
};

// --- XÁC THỰC EMAIL (Quan trọng cho bước đăng ký) ---
export const verifyEmail = async (email: string, code: string) => {
  // Backend yêu cầu: POST /api/auth/verify-email?email=...&code=...
  const response = await api.post("/auth/verify-email", null, {
    params: { email, code },
  });
  return response.data;
};

// --- ĐĂNG NHẬP BẰNG GOOGLE ---
export const googleLogin = async (
  googleToken: string,
  userRole: string = "CANDIDATE"
) => {
  const response = await api.post("/auth/google", {
    googleToken,
    userRole,
  });

  // LOG RA CONSOLE ĐỂ KIỂM TRA (Nhấn F12 tab Console để xem)
  console.log("👉 Raw Response from Google API:", response.data);

  // FIX QUAN TRỌNG: Kiểm tra dữ liệu nằm ở đâu
  // Ưu tiên 1: response.data.data (Nếu Backend có bọc wrapper)
  // Ưu tiên 2: response.data (Nếu Backend trả về trực tiếp)
  const authData = response.data.data || response.data;

  // Kiểm tra kỹ xem đã lấy được accessToken chưa
  if (!authData || !authData.accessToken) {
    console.error("❌ Không lấy được Auth Data hợp lệ:", authData);
    throw new Error("Dữ liệu trả về từ Server không hợp lệ");
  }

  // Lưu token vào localStorage
  if (authData.accessToken) {
    setToken(authData.accessToken);
    setRefreshToken(authData.refreshToken);

    // Lưu Role nếu có user
    if (authData.user) {
      setUserRole(authData.user.userRole);
    }
  }

  return authData;
};
