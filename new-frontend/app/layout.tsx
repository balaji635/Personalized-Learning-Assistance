import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Nexus - AI Learning Platform",
  description: "Learning platform UI",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <div className="noise-overlay" aria-hidden />
        {children}
      </body>
    </html>
  );
}
