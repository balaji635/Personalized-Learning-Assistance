import Link from "next/link";

interface SectionCardProps {
  title: string;
  actionHref?: string;
  actionLabel?: string;
  children: React.ReactNode;
}

export default function SectionCard({
  title,
  actionHref,
  actionLabel,
  children,
}: SectionCardProps) {
  return (
    <section className="card section-card">
      <header className="section-head">
        <h2>{title}</h2>
        {actionHref && actionLabel && (
          <Link className="section-link" href={actionHref}>
            {actionLabel}
          </Link>
        )}
      </header>
      <div className="section-body">{children}</div>

      <style jsx>{`
        .section-card {
          padding: 0;
          overflow: hidden;
        }

        .section-head {
          padding: 18px 20px;
          border-bottom: 1px solid var(--border-subtle);
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 16px;
        }

        .section-head h2 {
          margin: 0;
          font-size: 18px;
          color: var(--text-primary);
          letter-spacing: -0.01em;
        }

        .section-link {
          color: var(--gold-muted);
          text-decoration: none;
          font-family: var(--font-mono);
          font-size: 11px;
          letter-spacing: 0.05em;
          text-transform: uppercase;
          font-weight: 600;
        }

        .section-link:hover {
          color: var(--gold-bright);
        }

        .section-body {
          padding: 12px;
        }
      `}</style>
    </section>
  );
}