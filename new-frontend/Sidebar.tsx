"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { authApi } from "@/lib/api";

const NAV = [
  { href: "/dashboard",  label: "Overview",     icon: "⊡" },
  { href: "/chat",       label: "Conversations", icon: "⟡" },
  { href: "/documents",  label: "Documents",     icon: "◈" },
  { href: "/tests",      label: "Assessments",   icon: "◇" },
];

interface Props {
  user?: { firstName: string; lastName: string; email: string; role: string };
}

export default function Sidebar({ user }: Props) {
  const pathname = usePathname();
  const router = useRouter();

  async function handleLogout() {
    try { await authApi.logout(); } catch {}
    router.push("/");
  }

  function isActive(href: string) {
    if (href === "/dashboard") return pathname === "/dashboard";
    return pathname.startsWith(href);
  }

  return (
    <aside className="sidebar">
      {/* Brand */}
      <div className="sb-brand">
        <div className="sb-logo font-display">N</div>
        <div>
          <div className="sb-name font-display">Nexus</div>
          <div className="sb-tagline font-mono">Learning Platform</div>
        </div>
      </div>

      <div className="divider" style={{ margin: "0 16px 8px" }} />

      {/* Nav */}
      <nav style={{ flex: 1, paddingTop: 8 }}>
        <p className="nav-section-label">NAVIGATION</p>
        {NAV.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className={`nav-item ${isActive(item.href) ? "active" : ""}`}
          >
            <span className="nav-icon">{item.icon}</span>
            {item.label}
          </Link>
        ))}
      </nav>

      {/* User */}
      <div className="sb-footer">
        <div className="divider" style={{ marginBottom: 12 }} />
        {user && (
          <div className="sb-user">
            <div className="sb-avatar font-display">
              {user.firstName[0]}{user.lastName[0]}
            </div>
            <div className="sb-user-info">
              <div className="sb-user-name">
                {user.firstName} {user.lastName}
              </div>
              <div className="sb-user-role font-mono">{user.role}</div>
            </div>
          </div>
        )}
        <button className="btn btn-ghost btn-sm sb-logout" onClick={handleLogout}>
          <span>↪</span> Sign out
        </button>
      </div>

      <style jsx>{`
        .sb-brand {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 20px 20px 16px;
        }
        .sb-logo {
          width: 34px; height: 34px;
          background: var(--gold-bright);
          color: #080810;
          display: flex; align-items: center; justify-content: center;
          border-radius: 7px;
          font-size: 18px;
          font-weight: 600;
          flex-shrink: 0;
        }
        .sb-name {
          font-size: 17px;
          font-weight: 400;
          letter-spacing: 0.04em;
          color: var(--cream);
          line-height: 1.2;
        }
        .sb-tagline {
          font-size: 9px;
          letter-spacing: 0.1em;
          color: var(--text-muted);
          text-transform: uppercase;
        }
        .nav-section-label {
          font-family: var(--font-mono);
          font-size: 9px;
          letter-spacing: 0.12em;
          color: var(--text-muted);
          padding: 0 24px 6px;
          margin: 0;
        }
        .nav-icon {
          width: 18px;
          text-align: center;
          font-size: 14px;
          flex-shrink: 0;
        }
        .sb-footer {
          padding: 0 8px 16px;
        }
        .sb-user {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 10px 12px;
          margin-bottom: 4px;
        }
        .sb-avatar {
          width: 32px; height: 32px;
          border-radius: 50%;
          background: var(--gold-dim);
          border: 1px solid var(--gold-muted);
          display: flex; align-items: center; justify-content: center;
          font-size: 12px;
          color: var(--gold-bright);
          flex-shrink: 0;
        }
        .sb-user-name {
          font-size: 13px;
          font-weight: 500;
          color: var(--text-primary);
          line-height: 1.2;
        }
        .sb-user-role {
          font-size: 9px;
          letter-spacing: 0.1em;
          text-transform: uppercase;
          color: var(--text-muted);
        }
        .sb-logout {
          width: 100%;
          justify-content: flex-start;
          color: var(--text-muted);
          font-size: 12px;
        }
        .sb-logout:hover { color: var(--error); }
      `}</style>
    </aside>
  );
}
