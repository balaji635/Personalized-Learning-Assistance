interface StatTileProps {
  label: string;
  value: string | number;
  hint?: string;
}

export default function StatTile({ label, value, hint }: StatTileProps) {
  return (
    <article className="stat-card">
      <p className="stat-label">{label}</p>
      <p className="stat-value">{value}</p>
      {hint && <p className="stat-hint">{hint}</p>}

      <style jsx>{`
        .stat-hint {
          margin: 0;
          font-size: 12px;
          color: var(--text-muted);
        }
      `}</style>
    </article>
  );
}