import ProtectedLayout from "@/components/layout/ProtectedLayout";

export default function TestsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <ProtectedLayout>{children}</ProtectedLayout>;
}
