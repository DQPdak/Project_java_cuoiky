import { useState } from "react";
import { login } from "../services/authService";
import { saveAuth } from "../utils/authStorage";
import { useNavigate } from "react-router-dom";

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const res = await login(email, password);

      if (!res?.success) {
        setError(res?.message || "Đăng nhập thất bại");
        return;
      }

      saveAuth(res.data);

      const role = res.data?.user?.role; // tùy backend trả về
      if (role === "ADMIN" || role === "ROLE_ADMIN") {
        navigate("/admin");
      } else {
        navigate("/dashboard");
      }
    } catch (err) {
      // Nếu backend trả 401/400, axios sẽ vào catch
      setError("Email hoặc mật khẩu không đúng (hoặc backend chưa chạy/CORS)");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.wrap}>
      <form onSubmit={handleSubmit} style={styles.card}>
        <h2 style={{ margin: 0 }}>Đăng nhập CareerMate</h2>
        <p style={{ marginTop: 6, color: "#666" }}>
          Nhập email và mật khẩu để tiếp tục
        </p>

        {error && <div style={styles.error}>{error}</div>}

        <label style={styles.label}>Email</label>
        <input
          style={styles.input}
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
          required
        />

        <label style={styles.label}>Mật khẩu</label>
        <input
          style={styles.input}
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          required
        />

        <button style={styles.button} type="submit" disabled={loading}>
          {loading ? "Đang đăng nhập..." : "Đăng nhập"}
        </button>

        <div style={{ marginTop: 10, fontSize: 13, color: "#666" }}>
          Quên mật khẩu? (sẽ làm sau)
        </div>
      </form>
    </div>
  );
}

const styles = {
  wrap: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "#f3f4f6",
    padding: 16,
  },
  card: {
    width: 360,
    background: "white",
    borderRadius: 10,
    padding: 20,
    boxShadow: "0 10px 30px rgba(0,0,0,.08)",
    display: "flex",
    flexDirection: "column",
    gap: 10,
  },
  label: { fontSize: 13, fontWeight: 600 },
  input: {
    padding: "10px 12px",
    borderRadius: 8,
    border: "1px solid #ddd",
    outline: "none",
  },
  button: {
    marginTop: 6,
    padding: "10px 12px",
    borderRadius: 8,
    border: "none",
    background: "#111827",
    color: "white",
    cursor: "pointer",
    fontWeight: 600,
  },
  error: {
    padding: 10,
    borderRadius: 8,
    background: "#fee2e2",
    color: "#991b1b",
    fontSize: 13,
  },
};
