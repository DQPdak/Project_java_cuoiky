import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from "@/context/Authcontext";
import GoogleOAuthWrapper from "@/components/providers/GoogleOAuthWrapper";
import AIChatWidget from "@/components/features/chat/AIChatWidget";
import { Toaster } from "react-hot-toast";
import { ConfirmDialogProvider } from "@/context/ConfirmDialogContext";

export const metadata: Metadata = {
  title: "CareerMate",
  description: "Nền tảng tuyển dụng việc làm",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <GoogleOAuthWrapper>
          <AuthProvider>
            <ConfirmDialogProvider>
              {/* Cấu hình Toaster để thông báo tự động tắt */}
              <Toaster
                position="top-center"
                reverseOrder={false}
                containerStyle={{
                  zIndex: 999999,
                }}
                toastOptions={{
                  // Thời gian hiển thị mặc định: 3 giây (3000ms)
                  duration: 3000,

                  // Style mặc định (nền tối, chữ trắng)
                  style: {
                    background: "#363636",
                    color: "#fff",
                  },

                  // Cấu hình riêng cho thông báo thành công (Success)
                  success: {
                    duration: 3000,
                    style: {
                      background: "green",
                      color: "white",
                    },
                  },

                  // Cấu hình riêng cho thông báo lỗi (Error) - hiện lâu hơn 1 chút
                  error: {
                    duration: 4000,
                    style: {
                      background: "#ef4444", // Màu đỏ
                      color: "white",
                    },
                  },
                }}
              />

              {children}
            </ConfirmDialogProvider>

            <AIChatWidget />
          </AuthProvider>
        </GoogleOAuthWrapper>
      </body>
    </html>
  );
}
