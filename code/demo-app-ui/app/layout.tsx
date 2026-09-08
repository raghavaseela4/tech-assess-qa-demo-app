import type { Metadata } from "next";
import { ToastProvider } from "@/src/components/toast-provider";
import { ThemeProvider } from "@/src/features/theme/components/theme-provider";
import "./globals.css";

const geistSans = { variable: "--font-geist-sans", className: "" };
const geistMono = { variable: "--font-geist-mono", className: "" };

export const metadata: Metadata = {
  title: "Demo App",
  description: "Insurance Claims Management Application",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <ThemeProvider />
        <ToastProvider />
        {children}
      </body>
    </html>
  );
}
