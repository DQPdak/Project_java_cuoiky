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
              <Toaster 
              position="top-right" 
              reverseOrder={false} 
              containerStyle={{
                zIndex: 999999,
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