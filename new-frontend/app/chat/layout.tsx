import ProtectedLayout from "@/components/layout/ProtectedLayout";

export default function ChatLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <ProtectedLayout>{children}</ProtectedLayout>;
}
