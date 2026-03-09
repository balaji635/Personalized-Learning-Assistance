"use client";

import { useEffect, useState, useTransition } from "react";
import Link from "next/link";
import { testsApi, documentsApi } from "@/lib/api";
import type { TestSession, DifficultyLevel, DocumentItem } from "@/lib/types";

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short", day: "numeric", year: "numeric",
  });
}

export default function TestsPage() {
  const [tests, setTests] = useState<TestSession[]>([]);
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [showGenerate, setShowGenerate] = useState(false);
  const [isPending, startTransition] = useTransition();
  const [generated, setGenerated] = useState<TestSession | null>(null);

  const [form, setForm] = useState({
    topic: "",
    questionCount: 5,
    difficultyLevel: "INTERMEDIATE" as DifficultyLevel,
    documentId: "" as string,
  });

  useEffect(() => {
    Promise.all([testsApi.list(), documentsApi.list()]).then(([t, d]) => {
      setTests(t);
      setDocuments(d.filter((d) => d.status === "READY"));
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  function handleGenerate(e: React.FormEvent) {
    e.preventDefault();
    startTransition(async () => {
      const session = await testsApi.generate({
        topic: form.topic,
        questionCount: form.questionCount,
        difficultyLevel: form.difficultyLevel,
        documentId: form.documentId ? Number(form.documentId) : undefined,
      });
      setTests((t) => [session, ...t]);
      setGenerated(session);
      setShowGenerate(false);
    });
  }

  const pending = tests.filter((t) => t.status === "GENERATED");
  const done    = tests.filter((t) => t.status === "SUBMITTED");

  return (
    <div>
      <header className="page-header">
        <div>
          <h1 className="page-title">Assessments</h1>
          <p className="page-subtitle">AI-generated tests with instant feedback</p>
        </div>
        <button className="btn btn-primary btn-sm" onClick={() => setShowGenerate(true)}>
          + Generate test
        </button>
      </header>

      <div className="page-body">
        {/* Generated notification */}
        {generated && (
          <div className="notify-banner animate-fade-up">
            <span>✓</span>
            <div>
              <strong>Test generated!</strong> &quot;{generated.topic}&quot; is ready.
            </div>
            <Link href={`/tests/${generated.id}`} className="btn btn-primary btn-sm">
              Take test
            </Link>
            <button onClick={() => setGenerated(null)} style={{ background: "none", border: "none", color: "var(--success)", cursor: "pointer", marginLeft: "auto" }}>✕</button>
          </div>
        )}

        {/* Generate modal */}
        {showGenerate && (
          <div className="modal-backdrop animate-fade-in" onClick={() => setShowGenerate(false)}>
            <div className="modal animate-fade-up" onClick={(e) => e.stopPropagation()}>
              <div className="modal-head">
                <h2 className="font-display modal-title">Generate Assessment</h2>
                <button className="btn btn-ghost btn-sm" onClick={() => setShowGenerate(false)}>✕</button>
              </div>
              <form onSubmit={handleGenerate} className="modal-form">
                <div className="form-group">
                  <label className="label">Topic</label>
                  <input
                    className="input"
                    placeholder="e.g. Fourier Transforms"
                    value={form.topic}
                    onChange={(e) => setForm((f) => ({ ...f, topic: e.target.value }))}
                    required autoFocus
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label className="label">Questions</label>
                    <select
                      className="input select"
                      value={form.questionCount}
                      onChange={(e) => setForm((f) => ({ ...f, questionCount: Number(e.target.value) }))}
                    >
                      {[3, 5, 10, 15, 20].map((n) => (
                        <option key={n} value={n}>{n} questions</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="label">Difficulty</label>
                    <select
                      className="input select"
                      value={form.difficultyLevel}
                      onChange={(e) => setForm((f) => ({ ...f, difficultyLevel: e.target.value as DifficultyLevel }))}
                    >
                      <option value="BEGINNER">Beginner</option>
                      <option value="INTERMEDIATE">Intermediate</option>
                      <option value="ADVANCED">Advanced</option>
                    </select>
                  </div>
                </div>

                {documents.length > 0 && (
                  <div className="form-group">
                    <label className="label">Source Document (optional)</label>
                    <select
                      className="input select"
                      value={form.documentId}
                      onChange={(e) => setForm((f) => ({ ...f, documentId: e.target.value }))}
                    >
                      <option value="">None — use topic only</option>
                      {documents.map((d) => (
                        <option key={d.id} value={d.id}>{d.originalFileName}</option>
                      ))}
                    </select>
                  </div>
                )}

                <div style={{ display: "flex", gap: 10, justifyContent: "flex-end" }}>
                  <button type="button" className="btn btn-secondary" onClick={() => setShowGenerate(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={isPending}>
                    {isPending ? "Generating…" : "Generate"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {loading ? (
          <div className="tests-grid">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="skeleton" style={{ height: 130, borderRadius: 12 }} />
            ))}
          </div>
        ) : tests.length === 0 ? (
          <div className="empty-full">
            <div className="empty-icon">◇</div>
            <h2 className="font-display">No assessments yet</h2>
            <p>Generate an AI test to challenge your understanding.</p>
            <button className="btn btn-primary" onClick={() => setShowGenerate(true)}>
              Generate your first test
            </button>
          </div>
        ) : (
          <>
            {pending.length > 0 && (
              <section>
                <h2 className="section-label font-mono">IN PROGRESS</h2>
                <div className="tests-grid">
                  {pending.map((t, i) => (
                    <TestCard key={t.id} test={t} delay={i + 1} />
                  ))}
                </div>
              </section>
            )}
            {done.length > 0 && (
              <section style={{ marginTop: pending.length > 0 ? 28 : 0 }}>
                <h2 className="section-label font-mono">COMPLETED</h2>
                <div className="tests-grid">
                  {done.map((t, i) => (
                    <TestCard key={t.id} test={t} delay={i + 1} />
                  ))}
                </div>
              </section>
            )}
          </>
        )}
      </div>

      <style jsx>{`
        .tests-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 16px;
          margin-bottom: 8px;
        }
        .section-label {
          font-size: 10px; letter-spacing: 0.12em;
          color: var(--text-muted);
          margin: 0 0 12px;
          font-weight: 400;
        }
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
          width: 100%; max-width: 460px;
        }
        .modal-head {
          display: flex; align-items: center; justify-content: space-between;
          margin-bottom: 24px;
        }
        .modal-title { font-size: 22px; font-weight: 400; margin: 0; color: var(--cream); }
        .modal-form { display: flex; flex-direction: column; gap: 18px; }
        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
        .form-group { display: flex; flex-direction: column; }
        .notify-banner {
          background: rgba(92,158,106,0.12);
          border: 1px solid rgba(92,158,106,0.25);
          border-radius: var(--radius);
          padding: 14px 18px;
          color: var(--success);
          font-size: 13px;
          display: flex; align-items: center; gap: 12px;
          margin-bottom: 24px;
        }
        .notify-banner strong { color: var(--text-primary); }
      `}</style>
    </div>
  );
}

function TestCard({ test, delay }: { test: TestSession; delay: number }) {
  const isGood = (test.scorePercentage ?? 0) >= 60;

  return (
    <Link
      href={`/tests/${test.id}`}
      className={`test-card animate-fade-up delay-${Math.min(delay, 5)}`}
    >
      <div className="tc-top">
        <span className={`badge badge-${test.difficultyLevel.toLowerCase()}`}>
          {test.difficultyLevel}
        </span>
        {test.status === "SUBMITTED" && test.scorePercentage != null && (
          <div className={`tc-score font-mono ${isGood ? "good" : "bad"}`}>
            {Math.round(test.scorePercentage)}%
          </div>
        )}
        {test.status === "GENERATED" && (
          <div className="tc-pending font-mono">Pending</div>
        )}
      </div>

      <h3 className="tc-topic font-display">{test.topic}</h3>

      <div className="tc-meta">
        <span className="font-mono">{test.totalQuestions} questions</span>
        {test.status === "SUBMITTED" && test.correctAnswers != null && (
          <span className="font-mono">{test.correctAnswers} correct</span>
        )}
      </div>

      <div className="tc-date font-mono">{formatDate(test.createdAt)}</div>

      {test.status === "SUBMITTED" && test.scorePercentage != null && (
        <div className="tc-progress">
          <div className="progress-bar">
            <div
              className="progress-fill"
              style={{ width: `${test.scorePercentage}%` }}
            />
          </div>
        </div>
      )}

      <style jsx>{`
        .test-card {
          background: var(--bg-surface);
          border: 1px solid var(--border-subtle);
          border-radius: var(--radius-lg);
          padding: 20px;
          text-decoration: none;
          display: flex; flex-direction: column; gap: 10px;
          transition: border-color 0.2s, transform 0.2s, box-shadow 0.2s;
        }
        .test-card:hover {
          border-color: var(--border);
          transform: translateY(-2px);
          box-shadow: 0 8px 24px rgba(0,0,0,0.3);
        }
        .tc-top { display: flex; align-items: center; justify-content: space-between; }
        .tc-topic { font-size: 17px; font-weight: 400; color: var(--cream); margin: 0; letter-spacing: -0.01em; line-height: 1.3; }
        .tc-score { font-size: 13px; font-weight: 500; letter-spacing: 0.04em; }
        .tc-score.good { color: var(--success); }
        .tc-score.bad  { color: var(--error); }
        .tc-pending { font-size: 10px; letter-spacing: 0.08em; color: var(--info); }
        .tc-meta { display: flex; gap: 12px; font-size: 10px; letter-spacing: 0.06em; color: var(--text-muted); }
        .tc-date { font-size: 10px; letter-spacing: 0.06em; color: var(--text-muted); margin-top: auto; }
        .tc-progress { margin-top: 4px; }
      `}</style>
    </Link>
  );
}
