"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { documentsApi } from "@/lib/api";
import type { DocumentItem } from "@/lib/types";

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 ** 2) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 ** 2).toFixed(1)} MB`;
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short", day: "numeric", year: "numeric",
  });
}

export default function DocumentsPage() {
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const [error, setError] = useState("");
  const fileRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    documentsApi.list().then((d) => {
      setDocuments(d);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const handleUpload = useCallback(async (file: File) => {
    setUploading(true);
    setError("");
    try {
      const doc = await documentsApi.upload(file);
      setDocuments((d) => [doc, ...d]);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Upload failed");
    } finally {
      setUploading(false);
    }
  }, []);

  function onFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (file) handleUpload(file);
  }

  function onDrop(e: React.DragEvent) {
    e.preventDefault();
    setDragOver(false);
    const file = e.dataTransfer.files?.[0];
    if (file) handleUpload(file);
  }

  async function handleDelete(id: number) {
    await documentsApi.delete(id);
    setDocuments((d) => d.filter((doc) => doc.id !== id));
  }

  async function handleDownload(id: number, name: string) {
    const res = await documentsApi.download(id) as unknown as Response;
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url; a.download = name; a.click();
    URL.revokeObjectURL(url);
  }

  const statusIcon: Record<string, string> = {
    READY: "✓", PROCESSING: "◌", FAILED: "✕",
  };

  return (
    <div>
      <header className="page-header">
        <div>
          <h1 className="page-title">Documents</h1>
          <p className="page-subtitle">Upload materials to power your learning sessions</p>
        </div>
        <button
          className="btn btn-primary btn-sm"
          onClick={() => fileRef.current?.click()}
          disabled={uploading}
        >
          {uploading ? "Uploading…" : "+ Upload document"}
        </button>
        <input
          ref={fileRef}
          type="file"
          style={{ display: "none" }}
          accept=".pdf,.txt,.md,.docx"
          onChange={onFileChange}
        />
      </header>

      <div className="page-body">
        {error && (
          <div className="error-banner animate-fade-in">
            <span>⚠</span> {error}
            <button onClick={() => setError("")}>✕</button>
          </div>
        )}

        {/* Upload zone */}
        <div
          className={`upload-zone animate-fade-up ${dragOver ? "drag-over" : ""}`}
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={onDrop}
          onClick={() => fileRef.current?.click()}
          style={{ marginBottom: 28, cursor: "pointer" }}
        >
          {uploading ? (
            <div className="upload-uploading">
              <div className="loading-spinner" />
              <p>Processing your document…</p>
            </div>
          ) : (
            <>
              <div className="upload-icon">◈</div>
              <p className="upload-label">
                Drag & drop a file here, or <span>click to browse</span>
              </p>
              <p className="upload-sub font-mono">PDF · TXT · MD · DOCX</p>
            </>
          )}
        </div>

        {/* Documents grid */}
        {loading ? (
          <div className="docs-grid">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="skeleton" style={{ height: 140, borderRadius: 12 }} />
            ))}
          </div>
        ) : documents.length === 0 ? (
          <div className="docs-empty">
            <p>No documents yet. Upload one above to get started.</p>
          </div>
        ) : (
          <div className="docs-grid">
            {documents.map((doc, i) => (
              <div
                key={doc.id}
                className={`doc-card animate-fade-up delay-${Math.min(i + 1, 5)}`}
              >
                <div className="doc-card-top">
                  <div className="doc-type font-mono">{doc.fileType.toUpperCase()}</div>
                  <span className={`badge badge-${doc.status.toLowerCase()}`}>
                    {statusIcon[doc.status]} {doc.status}
                  </span>
                </div>

                <div className="doc-name">{doc.originalFileName}</div>

                <div className="doc-meta">
                  <span className="font-mono">{formatBytes(doc.fileSize)}</span>
                  {doc.chunkCount != null && (
                    <span className="font-mono">{doc.chunkCount} chunks</span>
                  )}
                  <span className="font-mono">{formatDate(doc.uploadedAt)}</span>
                </div>

                <div className="doc-actions">
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => handleDownload(doc.id, doc.originalFileName)}
                    disabled={doc.status !== "READY"}
                  >
                    Download
                  </button>
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={() => handleDelete(doc.id)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <style jsx>{`
        .docs-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 16px;
        }
        .doc-card {
          background: var(--bg-surface);
          border: 1px solid var(--border-subtle);
          border-radius: var(--radius-lg);
          padding: 20px;
          display: flex; flex-direction: column; gap: 12px;
          transition: border-color 0.2s;
        }
        .doc-card:hover { border-color: var(--border); }
        .doc-card-top { display: flex; align-items: center; justify-content: space-between; }
        .doc-type {
          font-size: 10px; letter-spacing: 0.1em;
          color: var(--text-muted);
          background: var(--bg-overlay);
          padding: 3px 8px;
          border-radius: 4px;
        }
        .doc-name {
          font-size: 14px;
          color: var(--text-primary);
          word-break: break-all;
          font-weight: 400;
          line-height: 1.4;
        }
        .doc-meta {
          display: flex; flex-wrap: wrap; gap: 8px;
          font-size: 10px; letter-spacing: 0.06em;
          color: var(--text-muted);
        }
        .doc-meta span::before { content: '·'; margin-right: 8px; }
        .doc-meta span:first-child::before { content: ''; margin: 0; }
        .doc-actions { display: flex; gap: 8px; }
        .upload-icon { font-size: 28px; color: var(--text-muted); margin-bottom: 10px; }
        .upload-label {
          font-size: 14px; color: var(--text-secondary); margin: 0 0 6px;
        }
        .upload-label span { color: var(--gold-bright); text-decoration: underline; text-underline-offset: 3px; }
        .upload-sub { font-size: 10px; letter-spacing: 0.1em; color: var(--text-muted); margin: 0; }
        .upload-uploading { display: flex; flex-direction: column; align-items: center; gap: 12px; }
        .upload-uploading p { color: var(--text-secondary); font-size: 14px; margin: 0; }
        .loading-spinner {
          width: 28px; height: 28px;
          border: 2px solid var(--border-subtle);
          border-top-color: var(--gold-bright);
          border-radius: 50%;
          animation: spin 0.8s linear infinite;
        }
        @keyframes spin { to { transform: rotate(360deg); } }
        .docs-empty {
          text-align: center;
          padding: 48px;
          color: var(--text-muted);
          font-size: 14px;
        }
        .error-banner {
          background: rgba(192,88,88,0.12);
          border: 1px solid rgba(192,88,88,0.25);
          border-radius: var(--radius);
          padding: 12px 16px;
          color: var(--error);
          font-size: 13px;
          display: flex; align-items: center; gap: 10px;
          margin-bottom: 20px;
        }
        .error-banner button {
          margin-left: auto;
          background: none; border: none;
          color: var(--error); cursor: pointer; font-size: 12px;
        }
      `}</style>
    </div>
  );
}
