"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { authApi } from "@/lib/api";
import type { UserProfile } from "@/lib/types";

const NAV_ITEMS = [
  { href: "/dashboard", label: "Overview", icon: "OV" },
  { href: "/chat", label: "Conversations", icon: "CH" },
  { href: "/documents", label: "Documents", icon: "DOC" },
  { href: "/tests", label: "Assessments", icon: "TS" },
];

interface SidebarProps {
  user: UserProfile;
}

export default function Sidebar({ user }: SidebarProps) {
  const pathname = usePathname();
  const router = useRouter();

  function isActive(href: string) {
    if (href === "/dashboard") {
      return pathname === "/dashboard";
    }

    return pathname.startsWith(href);
  }

  async function handleLogout() {
    try {
      await authApi.logout();
    } catch {
      // logout endpoint failure should still clear UI state
    }
    router.push("/");
    router.refresh();
  }

  const initials = `${user.firstName?.[0] ?? "U"}${user.lastName?.[0] ?? "S"}`;

  return (
    <aside className="sidebar">
      <div className="sb-brand">
        <div className="sb-logo">N</div>
        <div className="sb-brand-text">
          <strong>Nexus Learn</strong>
          <span>MainProject Connected</span>
        </div>
      </div>

      <div className="divider" style={{ margin: "8px 14px" }} />

      <nav className="sb-nav">
        <p className="sb-section">Navigation</p>
        {NAV_ITEMS.map((item) => (
          <Link
            key={item.href}
            href={item.href}
            className={`nav-item ${isActive(item.href) ? "active" : ""}`}
          >
            <span className="nav-chip">{item.icon}</span>
            <span>{item.label}</span>
          </Link>
        ))}
      </nav>

      <div className="sb-footer">
        <div className="divider" style={{ marginBottom: 12 }} />
        <div className="sb-user">
          <div className="sb-avatar">{initials}</div>
          <div className="sb-user-copy">
            <p>{user.firstName} {user.lastName}</p>
            <span>{user.email}</span>
          </div>
        </div>
        <button className="btn btn-ghost btn-sm sb-logout" onClick={handleLogout}>
          Sign out
        </button>
      </div>

      <style jsx>{`
        .sb-brand {
          padding: 18px 18px 10px;
          display: flex;
          align-items: center;
          gap: 10px;
        }

        .sb-logo {
          width: 32px;
          height: 32px;
          border-radius: 10px;
          background: var(--gold-bright);
          color: #ffffff;
          font-weight: 700;
          display: grid;
          place-items: center;
        }

        .sb-brand-text {
          display: flex;
          flex-direction: column;
          line-height: 1.2;
        }

        .sb-brand-text strong {
          color: var(--text-primary);
          font-size: 14px;
        }

        .sb-brand-text span {
          color: var(--text-muted);
          font-size: 11px;
          font-family: var(--font-mono);
        }

        .sb-nav {
          padding: 6px 8px;
          display: flex;
          flex-direction: column;
          gap: 2px;
          flex: 1;
        }

        .sb-section {
          margin: 0 0 6px;
          padding: 0 14px;
          font-family: var(--font-mono);
          font-size: 10px;
          letter-spacing: 0.08em;
          text-transform: uppercase;
          color: var(--text-muted);
        }

        .nav-chip {
          min-width: 34px;
          height: 22px;
          padding: 0 8px;
          border-radius: 999px;
          border: 1px solid var(--border-subtle);
          background: var(--bg-muted);
          font-size: 9px;
          font-family: var(--font-mono);
          letter-spacing: 0.04em;
          display: inline-flex;
          align-items: center;
          justify-content: center;
        }

        .sb-footer {
          padding: 0 10px 14px;
        }

        .sb-user {
          display: flex;
          gap: 10px;
          align-items: center;
          padding: 8px;
        }

        .sb-avatar {
          width: 34px;
          height: 34px;
          border-radius: 50%;
          border: 1px solid rgba(37, 99, 235, 0.22);
          background: rgba(37, 99, 235, 0.08);
          color: var(--gold-muted);
          font-size: 12px;
          font-family: var(--font-mono);
          display: grid;
          place-items: center;
        }

        .sb-user-copy p {
          margin: 0;
          color: var(--text-primary);
          font-size: 13px;
          font-weight: 600;
        }

        .sb-user-copy span {
          display: block;
          color: var(--text-muted);
          font-size: 11px;
          line-height: 1.3;
          word-break: break-word;
        }

        .sb-logout {
          width: 100%;
          justify-content: center;
        }
      `}</style>
    </aside>
  );
}