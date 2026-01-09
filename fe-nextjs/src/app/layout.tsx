import type { Metadata } from "next";
import { AuthProvider } from "@/context/AuthContext"; // Import AuthProvider
import "./globals.css";

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <AuthProvider> 
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}