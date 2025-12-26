import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "CareerMate - Job Marketplace",
  description: "AI-Powered Job Platform",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi">
      <body>{children}</body>
    </html>
  );
}