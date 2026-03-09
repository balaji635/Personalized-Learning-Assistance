"use client";

import { useEffect, useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { conversationsApi } from "@/lib/api";
import type { Conversation, DifficultyLevel } from "@/lib/types";

export default function ChatPage() {
  const router = useRouter();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [loading, setLoading] = useState(true);
  const [showNew, setShowNew] = useState(false);
  const [isPending, startTransition] = useTransition();

  const [newForm, setNewForm] = useState({
    title: "",
    difficultyLevel: "INTERMEDIATE" as DifficultyLevel,
  });

  useEffect(() => {
    conversationsApi.list().then((data) => {
      setConversations(data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    startTransition(async () => {
      const c = await conversationsApi.create(newForm);
      router.push(`/chat/${c.id}`);
    });
  }

  async function handleDelete(id: number, e: React.MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    await conversationsApi.delete(id);
    setConversations((prev) => prev.filter((c) => c.id !== id));
  }

  const diffColors: Record<DifficultyLevel, string> = {
    BEGINNER: "var(--success)",
    INTERMEDIATE: "var(--warning)",
    ADVANCED: "var(--error)",
  };

  return (
    <div>
      <header className="page-header">
        <div>
          <h1 className="page-title">Conversations</h1>
          <p className="page-subtitle">Your AI-powered study sessions</p>
        </div>
        <button className="btn btn-primary btn-sm" onClick={() => setShowNew(true)}>
          + New conversation
        </button>
      </header>

      <div className="page-body">
        {/* New Conversation modal */}
        {showNew && (
          <div className="modal-backdrop animate-fade-in" onClick={() => setShowNew(false)}>
            <div className="modal animate-fade-up" onClick={(e) => e.stopPropagation()}>
              <div className="modal-head">
                <h2 className="font-display modal-title">New Conversation</h2>
                <button className="btn btn-ghost btn-sm" onClick={() => setShowNew(false)}>✕</button>
              </div>
              <form onSubmit={handleCreate} className="modal-form">
                <div className="form-group">
                  <label className="label">Topic / Title</label>
                  <input
                    className="input"
                    placeholder="e.g. Quantum Entanglement"
                    value={newForm.title}
                    onChange={(e) => setNewForm((f) => ({ ...f, title: e.target.value }))}
                    required
                    autoFocus
                  />
                </div>
                <div className="form-group">
                  <label className="label">Difficulty Level</label>
                  <select
                    className="input select"
                    value={newForm.difficultyLevel}
                    onChange={(e) =>
                      setNewForm((f) => ({ ...f, difficultyLevel: e.target.value as DifficultyLevel }))
                    }
                  >
                    <option value="BEGINNER">Beginner</option>
                    <option value="INTERMEDIATE">Intermediate</option>
                    <option value="ADVANCED">Advanced</option>
                  </select>
                </div>
                <div style={{ display: "flex", gap: 10, justifyContent: "flex-end" }}>
                  <button type="button" className="btn btn-secondary" onClick={() => setShowNew(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={isPending}>
                    {isPending ? "Creating…" : "Start conversation"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {/* List */}
        {loading ? (
          <div className="convo-grid">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="skeleton" style={{ height: 120, borderRadius: 12 }} />
            ))}
          </div>
        ) : conversations.length === 0 ? (
          <div className="empty-full">
            <div className="empty-icon">⟡</div>
            <h2 className="font-display">Start your first conversation</h2>
            <p>Ask anything—your AI tutor adapts to your level.</p>
            <button className="btn btn-primary" onClick={() => setShowNew(true)}>
              New conversation
            </button>
          </div>
        ) : (
          <div className="convo-grid">
            {conversations.map((c, i) => (
              <a
                key={c.id}
                href={`/chat/${c.id}`}
                className={`convo-card animate-fade-up delay-${Math.min(i + 1, 5)}`}
              >
                <div className="convo-card-top">
                  <div
                    className="convo-dot"
                    style={{ background: diffColors[c.difficultyLevel] }}
                  />
                  <span className={`badge badge-${c.difficultyLevel.toLowerCase()}`}>
                    {c.difficultyLevel}
                  </span>
                </div>
                <h3 className="convo-title font-display">{c.title}</h3>
                <div className="convo-meta font-mono">
                  Updated {new Date(c.updatedAt).toLocaleDateString()}
                </div>
                <button
                  className="convo-delete"
                  onClick={(e) => handleDelete(c.id, e)}
                  title="Delete"
                >
                  ✕
                </button>
              </a>
            ))}
          </div>
        )}
      </div>

      <style jsx>{`
        .convo-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
          gap: 16px;
        }
        .convo-card {
          background: var(--bg-surface);
          border: 1px solid var(--border-subtle);
          border-radius: var(--radius-lg);
          padding: 20px;
          text-decoration: none;
          display: flex;
          flex-direction: column;
          gap: 10px;
          position: relative;
          transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
        }
        .convo-card:hover {
          border-color: var(--border);
          transform: translateY(-2px);
          box-shadow: 0 8px 24px rgba(0,0,0,0.3);
        }
        .convo-card-top { display: flex; align-items: center; gap: 8px; }
        .convo-dot { width: 6px; height: 6px; border-radius: 50%; }
        .convo-title {
          font-size: 17px;
          font-weight: 400;
          color: var(--cream);
          letter-spacing: -0.01em;
          margin: 0;
          line-height: 1.3;
        }
        .convo-meta { font-size: 10px; letter-spacing: 0.06em; color: var(--text-muted); }
        .convo-delete {
          position: absolute;
          top: 12px; right: 12px;
          background: none; border: none;
          color: var(--text-muted);
          font-size: 11px;
          cursor: pointer;
          opacity: 0;
          transition: opacity 0.15s, color 0.15s;
          padding: 4px;
        }
        .convo-card:hover .convo-delete { opacity: 1; }
        .convo-delete:hover { color: var(--error); }
        .empty-full {
          text-align: center;
          padding: 80px 40px;
          display: flex; flex-direction: column; align-items: center; gap: 12px;
        }
        .empty-icon { font-size: 40px; color: var(--text-muted); opacity: 0.5; }
        .empty-full h2 { font-size: 26px; font-weight: 300; color: var(--cream); margin: 0; }
        .empty-full p  { color: var(--text-muted); font-size: 14px; margin: 0; }
        .modal-backdrop {
          position: fixed; inset: 0;
          background: rgba(8,8,16,0.75);
          backdrop-filter: blur(4px);
          z-index: 200;
          display: flex; align-items: center; justify-content: center;
        }
        .modal {
          background: var(--bg-surface);
          border: 1px solid var(--border);
          border-radius: var(--radius-lg);
          padding: 28px;
          width: 100%; max-width: 440px;
        }
        .modal-head {
          display: flex; align-items: center; justify-content: space-between;
          margin-bottom: 24px;
        }
        .modal-title { font-size: 22px; font-weight: 400; margin: 0; color: var(--cream); }
        .modal-form { display: flex; flex-direction: column; gap: 18px; }
        .form-group { display: flex; flex-direction: column; }
      `}</style>
    </div>
  );
}
