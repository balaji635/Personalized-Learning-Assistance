"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { conversationsApi, documentsApi, testsApi } from "@/lib/api";
import type { Conversation, DocumentItem, TestSession } from "@/lib/types";
import StatTile from "@/components/dashboard/StatTile";
import SectionCard from "@/components/dashboard/SectionCard";

export default function DashboardPage() {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [tests, setTests] = useState<TestSession[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    Promise.all([conversationsApi.list(), documentsApi.list(), testsApi.list()])
      .then(([conversationData, documentData, testData]) => {
        if (cancelled) {
          return;
        }

        setConversations(conversationData);
        setDocuments(documentData);
        setTests(testData);
      })
      .catch(() => {
        if (!cancelled) {
          setError("Could not load dashboard data. Please check your backend connection.");
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const submittedTests = tests.filter((test) => test.status === "SUBMITTED");
  const generatedTests = tests.filter((test) => test.status === "GENERATED");
  const readyDocs = documents.filter((doc) => doc.status === "READY");

  const averageScore =
    submittedTests.length > 0
      ? Math.round(
          submittedTests.reduce((sum, test) => sum + (test.scorePercentage ?? 0), 0) /
            submittedTests.length,
        )
      : null;

  const recentConversations = conversations.slice(0, 5);
  const recentTests = tests.slice(0, 5);
  const recentDocuments = documents.slice(0, 4);

  const completionRate = useMemo(() => {
    if (tests.length === 0) {
      return 0;
    }

    return Math.round((submittedTests.length / tests.length) * 100);
  }, [submittedTests.length, tests.length]);

  const recommendedNext =
    documents.length === 0
      ? "Upload a study document so conversations and tests can use your own content."
      : tests.length === 0
      ? "Generate your first assessment to benchmark current understanding."
      : generatedTests.length > 0
      ? "Finish pending assessments to improve your score trend."
      : "Continue with a new conversation and explore advanced topics.";

  return (
    <div>
      <header className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-subtitle">Progress synced from MainProject backend APIs</p>
        </div>
        <div className="header-actions">
          <Link href="/chat" className="btn btn-secondary btn-sm">
            New conversation
          </Link>
          <Link href="/tests" className="btn btn-primary btn-sm">
            Start assessment
          </Link>
        </div>
      </header>

      <div className="page-body">
        {error && <div className="error-strip">{error}</div>}

        <div className="dashboard-grid stats-grid animate-fade-up">
          <StatTile
            label="Conversations"
            value={loading ? "--" : conversations.length}
            hint="Saved chat sessions"
          />
          <StatTile
            label="Documents"
            value={loading ? "--" : documents.length}
            hint={`${readyDocs.length} ready for querying`}
          />
          <StatTile
            label="Assessments"
            value={loading ? "--" : tests.length}
            hint={`${generatedTests.length} pending`}
          />
          <StatTile
            label="Average score"
            value={loading ? "--" : averageScore == null ? "N/A" : `${averageScore}%`}
            hint={`${submittedTests.length} submitted tests`}
          />
          <StatTile
            label="Completion"
            value={loading ? "--" : `${completionRate}%`}
            hint="Submitted / generated"
          />
        </div>

        <div className="dashboard-grid main-grid">
          <SectionCard title="Recent Conversations" actionHref="/chat" actionLabel="View all">
            {loading ? (
              <SkeletonList count={4} />
            ) : recentConversations.length === 0 ? (
              <EmptyState text="No conversations yet." actionHref="/chat" actionLabel="Create one" />
            ) : (
              <ul className="item-list">
                {recentConversations.map((conversation) => (
                  <li key={conversation.id}>
                    <Link href={`/chat/${conversation.id}`} className="item-row">
                      <div className="item-main">
                        <p className="item-title">{conversation.title}</p>
                        <p className="item-meta">Updated {formatDate(conversation.updatedAt)}</p>
                      </div>
                      <span className={`badge badge-${conversation.difficultyLevel.toLowerCase()}`}>
                        {conversation.difficultyLevel}
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>

          <SectionCard title="Recent Assessments" actionHref="/tests" actionLabel="View all">
            {loading ? (
              <SkeletonList count={4} />
            ) : recentTests.length === 0 ? (
              <EmptyState text="No assessments generated yet." actionHref="/tests" actionLabel="Generate one" />
            ) : (
              <ul className="item-list">
                {recentTests.map((test) => (
                  <li key={test.id}>
                    <Link href={`/tests/${test.id}`} className="item-row">
                      <div className="item-main">
                        <p className="item-title">{test.topic}</p>
                        <p className="item-meta">
                          {test.totalQuestions} questions - {formatDate(test.createdAt)}
                        </p>
                      </div>
                      {test.status === "SUBMITTED" && test.scorePercentage != null ? (
                        <span className="score-pill">{Math.round(test.scorePercentage)}%</span>
                      ) : (
                        <span className="badge badge-processing">In progress</span>
                      )}
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>
        </div>

        <div className="dashboard-grid secondary-grid">
          <SectionCard title="Document Pipeline" actionHref="/documents" actionLabel="Manage">
            {loading ? (
              <SkeletonList count={3} />
            ) : recentDocuments.length === 0 ? (
              <EmptyState text="No uploaded documents yet." actionHref="/documents" actionLabel="Upload file" />
            ) : (
              <ul className="item-list">
                {recentDocuments.map((document) => (
                  <li key={document.id} className="doc-row">
                    <div className="item-main">
                      <p className="item-title">{document.originalFileName}</p>
                      <p className="item-meta">
                        {formatBytes(document.fileSize)} - {formatDate(document.uploadedAt)}
                      </p>
                    </div>
                    <span className={`badge badge-${document.status.toLowerCase()}`}>
                      {document.status}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </SectionCard>

          <SectionCard title="Suggested Next Step">
            <div className="next-box">
              <p>{recommendedNext}</p>

              <div className="progress-block">
                <div>
                  <div className="progress-label">Assessment completion</div>
                  <div className="progress-bar">
                    <div className="progress-fill" style={{ width: `${completionRate}%` }} />
                  </div>
                </div>
                <div>
                  <div className="progress-label">Document readiness</div>
                  <div className="progress-bar">
                    <div
                      className="progress-fill"
                      style={{
                        width:
                          documents.length > 0
                            ? `${Math.round((readyDocs.length / documents.length) * 100)}%`
                            : "0%",
                      }}
                    />
                  </div>
                </div>
              </div>

              <div className="next-actions">
                <Link href="/documents" className="btn btn-secondary btn-sm">
                  Upload material
                </Link>
                <Link href="/tests" className="btn btn-primary btn-sm">
                  Generate test
                </Link>
              </div>
            </div>
          </SectionCard>
        </div>
      </div>

      <style jsx>{`
        .header-actions {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        .error-strip {
          margin-bottom: 16px;
          border-radius: var(--radius);
          border: 1px solid rgba(220, 38, 38, 0.22);
          background: rgba(220, 38, 38, 0.08);
          color: var(--error);
          padding: 10px 12px;
          font-size: 13px;
        }

        .dashboard-grid {
          display: grid;
          gap: 16px;
        }

        .stats-grid {
          grid-template-columns: repeat(5, minmax(0, 1fr));
          margin-bottom: 16px;
        }

        .main-grid {
          grid-template-columns: repeat(2, minmax(0, 1fr));
          margin-bottom: 16px;
        }

        .secondary-grid {
          grid-template-columns: 1.2fr 1fr;
        }

        .item-list {
          list-style: none;
          margin: 0;
          padding: 0;
          display: flex;
          flex-direction: column;
          gap: 8px;
        }

        .item-row {
          display: flex;
          align-items: center;
          gap: 12px;
          border: 1px solid transparent;
          border-radius: var(--radius);
          padding: 12px;
          text-decoration: none;
          transition: border-color 0.2s, background 0.2s;
        }

        .item-row:hover {
          border-color: var(--border-subtle);
          background: var(--bg-muted);
        }

        .item-main {
          min-width: 0;
          flex: 1;
        }

        .item-title {
          margin: 0;
          font-size: 14px;
          color: var(--text-primary);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          font-weight: 600;
        }

        .item-meta {
          margin: 3px 0 0;
          color: var(--text-muted);
          font-size: 12px;
          font-family: var(--font-mono);
        }

        .score-pill {
          border-radius: 999px;
          padding: 3px 9px;
          font-family: var(--font-mono);
          font-size: 12px;
          color: var(--success);
          background: rgba(22, 163, 74, 0.12);
          border: 1px solid rgba(22, 163, 74, 0.2);
          white-space: nowrap;
        }

        .doc-row {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px;
          border-radius: var(--radius);
          background: var(--bg-muted);
          border: 1px solid var(--border-subtle);
        }

        .next-box {
          padding: 6px;
          display: flex;
          flex-direction: column;
          gap: 18px;
        }

        .next-box p {
          margin: 0;
          color: var(--text-secondary);
          font-size: 14px;
          line-height: 1.5;
        }

        .progress-block {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .progress-label {
          font-size: 12px;
          color: var(--text-muted);
          margin-bottom: 6px;
          font-family: var(--font-mono);
        }

        .next-actions {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        @media (max-width: 1280px) {
          .stats-grid {
            grid-template-columns: repeat(3, minmax(0, 1fr));
          }
        }

        @media (max-width: 980px) {
          .stats-grid,
          .main-grid,
          .secondary-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
    </div>
  );
}

function SkeletonList({ count }: { count: number }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="skeleton" style={{ height: 52, borderRadius: 10 }} />
      ))}
    </div>
  );
}

function EmptyState({
  text,
  actionHref,
  actionLabel,
}: {
  text: string;
  actionHref: string;
  actionLabel: string;
}) {
  return (
    <div style={{ textAlign: "center", padding: "28px 14px", color: "var(--text-muted)" }}>
      <p style={{ margin: 0, fontSize: 14 }}>{text}</p>
      <Link href={actionHref} className="btn btn-secondary btn-sm" style={{ marginTop: 12 }}>
        {actionLabel}
      </Link>
    </div>
  );
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

function formatBytes(bytes: number) {
  if (bytes < 1024) {
    return `${bytes} B`;
  }

  if (bytes < 1024 ** 2) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }

  return `${(bytes / 1024 ** 2).toFixed(1)} MB`;
}