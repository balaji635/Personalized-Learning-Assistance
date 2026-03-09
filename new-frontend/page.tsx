"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { authApi } from "@/lib/api";
import { ApiError } from "@/lib/types";

type Mode = "login" | "register";

export default function AuthPage() {
  const router = useRouter();
  const [mode, setMode] = useState<Mode>("login");
  const [error, setError] = useState("");
  const [isPending, startTransition] = useTransition();

  const [form, setForm] = useState({
    email: "",
    password: "",
    firstName: "",
    lastName: "",
  });

  function update(k: keyof typeof form) {
    return (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm((f) => ({ ...f, [k]: e.target.value }));
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");

    startTransition(async () => {
      try {
        if (mode === "login") {
          await authApi.login({ email: form.email, password: form.password });
        } else {
          await authApi.register({
            email: form.email,
            password: form.password,
            firstName: form.firstName,
            lastName: form.lastName,
          });
        }
        router.push("/dashboard");
      } catch (err) {
        setError(err instanceof ApiError ? err.message : "Something went wrong");
      }
    });
  }

  return (
    <div className="auth-page">
      {/* ── Left panel ─── */}
      <aside className="auth-left">
        <div className="auth-brand">
          <span className="auth-logo">N</span>
          <span className="auth-wordmark font-display">Nexus</span>
        </div>

        <div className="auth-tagline">
          <h1 className="font-display">
            Learn deeper.<br />
            Think sharper.
          </h1>
          <p>An AI-powered study companion that adapts to the way you think.</p>
        </div>

        <ul className="auth-features">
          {[
            ["⟡", "Adaptive conversations", "Discuss any topic at your level"],
            ["◈", "Document intelligence", "Upload & query your own materials"],
            ["◇", "Dynamic assessments", "AI-generated tests with instant feedback"],
          ].map(([icon, title, desc]) => (
            <li key={title}>
              <span className="feature-icon">{icon}</span>
              <div>
                <strong>{title}</strong>
                <p>{desc}</p>
              </div>
            </li>
          ))}
        </ul>

        <div className="auth-left-footer">
          <span className="font-mono" style={{ fontSize: 10, color: "var(--text-muted)", letterSpacing: "0.1em" }}>
            POWERED BY SPRING AI
          </span>
        </div>
      </aside>

      {/* ── Right panel ─── */}
      <main className="auth-right">
        <div className="auth-form-container animate-fade-up">
          <div className="auth-mode-toggle">
            <button
              className={mode === "login" ? "active" : ""}
              onClick={() => setMode("login")}
            >
              Sign In
            </button>
            <button
              className={mode === "register" ? "active" : ""}
              onClick={() => setMode("register")}
            >
              Create Account
            </button>
          </div>

          <h2 className="font-display auth-form-title">
            {mode === "login" ? "Welcome back" : "Start learning"}
          </h2>
          <p className="auth-form-sub">
            {mode === "login"
              ? "Enter your credentials to continue"
              : "Create your account in seconds"}
          </p>

          <form onSubmit={handleSubmit} className="auth-form">
            {mode === "register" && (
              <div className="form-row">
                <div className="form-group">
                  <label className="label">First name</label>
                  <input
                    className="input"
                    placeholder="Ada"
                    value={form.firstName}
                    onChange={update("firstName")}
                    required
                    autoComplete="given-name"
                  />
                </div>
                <div className="form-group">
                  <label className="label">Last name</label>
                  <input
                    className="input"
                    placeholder="Lovelace"
                    value={form.lastName}
                    onChange={update("lastName")}
                    required
                    autoComplete="family-name"
                  />
                </div>
              </div>
            )}

            <div className="form-group">
              <label className="label">Email</label>
              <input
                className="input"
                type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={update("email")}
                required
                autoComplete="email"
              />
            </div>

            <div className="form-group">
              <label className="label">Password</label>
              <input
                className="input"
                type="password"
                placeholder="••••••••"
                value={form.password}
                onChange={update("password")}
                required
                autoComplete={mode === "login" ? "current-password" : "new-password"}
              />
            </div>

            {error && (
              <div className="auth-error">
                <span>⚠</span> {error}
              </div>
            )}

            <button
              type="submit"
              className="btn btn-primary btn-lg"
              style={{ width: "100%", justifyContent: "center", marginTop: 4 }}
              disabled={isPending}
            >
              {isPending
                ? "Please wait…"
                : mode === "login"
                ? "Sign in"
                : "Create account"}
            </button>
          </form>

          <p className="auth-switch">
            {mode === "login" ? "New here?" : "Already have an account?"}{" "}
            <button onClick={() => setMode(mode === "login" ? "register" : "login")}>
              {mode === "login" ? "Create an account" : "Sign in"}
            </button>
          </p>
        </div>
      </main>

      <style jsx>{`
        .auth-page {
          display: grid;
          grid-template-columns: 1fr 1fr;
          min-height: 100vh;
        }

        /* ── Left ── */
        .auth-left {
          background: var(--bg-surface);
          border-right: 1px solid var(--border-subtle);
          padding: 48px 56px;
          display: flex;
          flex-direction: column;
          position: relative;
          overflow: hidden;
        }

        .auth-left::before {
          content: '';
          position: absolute;
          top: -120px; right: -120px;
          width: 400px; height: 400px;
          border-radius: 50%;
          background: radial-gradient(circle, rgba(212,168,75,0.08) 0%, transparent 70%);
          pointer-events: none;
        }

        .auth-brand {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: auto;
        }

        .auth-logo {
          width: 36px; height: 36px;
          background: var(--gold-bright);
          color: #080810;
          display: flex; align-items: center; justify-content: center;
          border-radius: 8px;
          font-family: var(--font-display);
          font-size: 20px;
          font-weight: 600;
        }

        .auth-wordmark {
          font-size: 22px;
          font-weight: 400;
          letter-spacing: 0.04em;
          color: var(--cream);
        }

        .auth-tagline {
          margin-top: 80px;
        }

        .auth-tagline h1 {
          font-size: 48px;
          font-weight: 300;
          line-height: 1.15;
          color: var(--cream);
          margin: 0 0 16px;
          letter-spacing: -0.02em;
        }

        .auth-tagline p {
          color: var(--text-secondary);
          font-size: 15px;
          line-height: 1.6;
          max-width: 340px;
        }

        .auth-features {
          list-style: none;
          padding: 0; margin: 48px 0 0;
          display: flex;
          flex-direction: column;
          gap: 24px;
        }

        .auth-features li {
          display: flex;
          align-items: flex-start;
          gap: 14px;
        }

        .feature-icon {
          font-size: 18px;
          color: var(--gold-bright);
          margin-top: 2px;
          flex-shrink: 0;
        }

        .auth-features strong {
          display: block;
          font-size: 14px;
          font-weight: 500;
          color: var(--text-primary);
          margin-bottom: 2px;
        }

        .auth-features p {
          margin: 0;
          font-size: 13px;
          color: var(--text-muted);
        }

        .auth-left-footer {
          margin-top: 48px;
        }

        /* ── Right ── */
        .auth-right {
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 48px;
          background: var(--bg-base);
        }

        .auth-form-container {
          width: 100%;
          max-width: 400px;
        }

        .auth-mode-toggle {
          display: flex;
          gap: 0;
          background: var(--bg-surface);
          border: 1px solid var(--border-subtle);
          border-radius: var(--radius);
          padding: 4px;
          margin-bottom: 32px;
          width: fit-content;
        }

        .auth-mode-toggle button {
          padding: 7px 18px;
          border-radius: 6px;
          border: none;
          background: transparent;
          color: var(--text-muted);
          font-family: var(--font-body);
          font-size: 13px;
          cursor: pointer;
          transition: all 0.18s;
        }

        .auth-mode-toggle button.active {
          background: var(--bg-elevated);
          color: var(--text-primary);
          box-shadow: 0 1px 3px rgba(0,0,0,0.3);
        }

        .auth-form-title {
          font-size: 30px;
          font-weight: 400;
          letter-spacing: -0.02em;
          color: var(--cream);
          margin: 0 0 6px;
        }

        .auth-form-sub {
          color: var(--text-muted);
          font-size: 13.5px;
          margin: 0 0 28px;
        }

        .auth-form {
          display: flex;
          flex-direction: column;
          gap: 18px;
        }

        .form-row {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 12px;
        }

        .form-group {
          display: flex;
          flex-direction: column;
        }

        .auth-error {
          background: rgba(192, 88, 88, 0.12);
          border: 1px solid rgba(192, 88, 88, 0.25);
          border-radius: var(--radius);
          padding: 10px 14px;
          color: var(--error);
          font-size: 13px;
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .auth-switch {
          text-align: center;
          margin-top: 24px;
          font-size: 13px;
          color: var(--text-muted);
        }

        .auth-switch button {
          background: none;
          border: none;
          color: var(--gold-bright);
          cursor: pointer;
          font-size: 13px;
          padding: 0;
          text-decoration: underline;
          text-underline-offset: 3px;
        }

        @media (max-width: 900px) {
          .auth-page { grid-template-columns: 1fr; }
          .auth-left { display: none; }
          .auth-right { padding: 32px 24px; }
        }
      `}</style>
    </div>
  );
}
