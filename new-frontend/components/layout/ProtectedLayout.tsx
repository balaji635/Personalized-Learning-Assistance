import { redirect } from "next/navigation";
import Sidebar from "@/components/layout/Sidebar";
import { getCurrentUser } from "@/lib/server-auth";

export default async function ProtectedLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const user = await getCurrentUser();

  if (!user) {
    redirect("/");
  }

  return (
    <div className="app-layout">
      <Sidebar user={user} />
      <div className="main-content">{children}</div>
    </div>
  );
}
