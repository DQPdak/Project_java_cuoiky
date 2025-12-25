import api from "./api";

export const login = async (email, password) => {
  const res = await api.post("/api/auth/login", { email, password });
  return res.data; // MessageResponse từ backend
};
