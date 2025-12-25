// chặn admin nếu chưa login / sai role
import { Navigate } from "react-router-dom";
import { getUser, isLoggedIn } from "../utils/authStorage";

export default function ProtectedRoute({ children, requireAdmin = false }) {
  if (!isLoggedIn()) return <Navigate to="/login" replace />;

  if (requireAdmin) {
    const user = getUser();
    const role = user?.role;
    const isAdmin = role === "ADMIN" || role === "ROLE_ADMIN";
    if (!isAdmin) return <Navigate to="/dashboard" replace />;
  }

  return children;
}
