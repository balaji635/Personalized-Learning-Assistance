"use client";

import { useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { authApi } from "@/lib/api";
import { ApiError } from "@/lib/types";

type Mode = "login" | "register";

export default function AuthForm() {
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

  function update(key: keyof typeof form, value: string) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  const canSubmit =
    mode === "login"
      ? form.email.trim().length > 0 && form.password.trim().length > 0
      : form.email.trim().length > 0 &&
        form.password.trim().length >= 6 &&
        form.firstName.trim().length > 0 &&
        form.lastName.trim().length > 0;

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    if (!canSubmit || isPending) {
      return;
    }

    setError("");

    startTransition(async () => {
      try {
        if (mode === "login") {
          await authApi.login({
            email: form.email.trim(),
            password: form.password,
          });
        } else {
          await authApi.register({
            email: form.email.trim(),
            password: form.password,
            firstName: form.firstName.trim(),
            lastName: form.lastName.trim(),
          });
        }

        router.push("/dashboard");
      } catch (err: unknown) {
        if (err instanceof ApiError) {
          setError(err.message);
          return;
        }

        setError("Could not connect to backend. Make sure MainProject is running on port 8080.");
      }
    });
  }

  return (
    <div className="auth-shell">
      <section className="auth-copy">
        <p className="auth-badge">Connected to MainProject backend</p>
        <h1>Learn faster with guided AI study sessions.</h1>
        <p>
          Sign in to continue your conversations, documents, and assessments from one place.
        </p>
        <ul>
          <li>Context-aware chat based on your chosen difficulty</li>
          <li>Document ingestion with searchable chunks</li>
          <li>Auto-generated tests with instant scoring</li>
        </ul>
      </section>

      <section className="auth-card-wrapper">
        <div className="auth-card animate-fade-up">
          <div className="auth-switcher" role="tablist" aria-label="Auth mode">
            <button
              type="button"
              className={mode === "login" ? "active" : ""}
              onClick={() => setMode("login")}
            >
              Sign in
            </button>
            <button
              type="button"
              className={mode === "register" ? "active" : ""}
              onClick={() => setMode("register")}
            >
              Create account
            </button>
          </div>

          <h2>{mode === "login" ? "Welcome back" : "Create your account"}</h2>
          <p className="auth-muted">
            {mode === "login"
              ? "Use your email and password to open the dashboard."
              : "Registration requires your full name, email, and a password of at least 6 characters."}
          </p>

          <form onSubmit={handleSubmit} className="auth-form" noValidate>
            {mode === "register" && (
              <div className="auth-row">
                <label className="form-group">
                  <span className="label">First name</span>
                  <input
                    className="input"
                    value={form.firstName}
                    onChange={(e) => update("firstName", e.target.value)}
                    autoComplete="given-name"
                    required
                  />
                </label>
                <label className="form-group">
                  <span className="label">Last name</span>
                  <input
                    className="input"
                    value={form.lastName}
                    onChange={(e) => update("lastName", e.target.value)}
                    autoComplete="family-name"
                    required
                  />
                </label>
              </div>
            )}

            <label className="form-group">
              <span className="label">Email</span>
              <input
                className="input"
                type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={(e) => update("email", e.target.value)}
                autoComplete="email"
                required
              />
            </label>

            <label className="form-group">
              <span className="label">Password</span>
              <input
                className="input"
                type="password"
                placeholder="Enter your password"
                value={form.password}
                onChange={(e) => update("password", e.target.value)}
                autoComplete={mode === "login" ? "current-password" : "new-password"}
                minLength={mode === "register" ? 6 : undefined}
                required
              />
            </label>

            {error && <p className="auth-error">{error}</p>}

            <button
              type="submit"
              className="btn btn-primary auth-submit"
              disabled={!canSubmit || isPending}
            >
              {isPending
                ? "Please wait..."
                : mode === "login"
                ? "Sign in"
                : "Create account"}
            </button>
          </form>
        </div>
      </section>

      <style jsx>{`
        .auth-shell {
          min-height: 100vh;
          display: grid;
          grid-template-columns: 1.1fr 1fr;
          background: var(--bg-base);
        }

        .auth-copy {
          padding: 88px 72px;
          display: flex;
          flex-direction: column;
          gap: 18px;
          justify-content: center;
          background: linear-gradient(180deg, #f8fbff 0%, #eef3fb 100%);
          border-right: 1px solid var(--border-subtle);
        }

        .auth-badge {
          display: inline-flex;
          align-items: center;
          width: fit-content;
          margin: 0;
          padding: 6px 10px;
          border-radius: 999px;
          font-size: 11px;
          font-family: var(--font-mono);
          letter-spacing: 0.05em;
          text-transform: uppercase;
          color: var(--gold-muted);
          background: rgba(37, 99, 235, 0.1);
        }

        .auth-copy h1 {
          margin: 0;
          max-width: 540px;
          font-size: 48px;
          line-height: 1.1;
          letter-spacing: -0.03em;
          color: var(--text-primary);
          font-family: var(--font-display);
          font-weight: 600;
        }

        .auth-copy > p {
          margin: 0;
          max-width: 520px;
          font-size: 17px;
          color: var(--text-secondary);
        }

        .auth-copy ul {
          margin: 8px 0 0;
          padding-left: 18px;
          max-width: 540px;
          color: var(--text-secondary);
          display: flex;
          flex-direction: column;
          gap: 10px;
        }

        .auth-card-wrapper {
          padding: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .auth-card {
          width: 100%;
          max-width: 460px;
          border: 1px solid var(--border-subtle);
          border-radius: var(--radius-lg);
          background: var(--bg-surface);
          box-shadow: var(--shadow-soft);
          padding: 28px;
        }

        .auth-switcher {
          display: inline-flex;
          gap: 4px;
          background: var(--bg-muted);
          border: 1px solid var(--border-subtle);
          border-radius: var(--radius);
          padding: 4px;
          margin-bottom: 20px;
        }

        .auth-switcher button {
          border: none;
          background: transparent;
          border-radius: 8px;
          padding: 8px 14px;
          font-size: 13px;
          font-weight: 600;
          color: var(--text-muted);
          cursor: pointer;
        }

        .auth-switcher button.active {
          background: #ffffff;
          color: var(--text-primary);
          box-shadow: 0 1px 2px rgba(15, 23, 42, 0.08);
        }

        .auth-card h2 {
          margin: 0;
          color: var(--text-primary);
          font-size: 30px;
          letter-spacing: -0.02em;
          font-family: var(--font-display);
          font-weight: 600;
        }

        .auth-muted {
          margin: 8px 0 0;
          color: var(--text-muted);
          font-size: 14px;
          line-height: 1.5;
        }

        .auth-form {
          margin-top: 22px;
          display: flex;
          flex-direction: column;
          gap: 14px;
        }

        .auth-row {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 10px;
        }

        .form-group {
          display: flex;
          flex-direction: column;
        }

        .auth-error {
          margin: 0;
          border-radius: var(--radius);
          border: 1px solid rgba(220, 38, 38, 0.25);
          background: rgba(220, 38, 38, 0.08);
          color: var(--error);
          padding: 9px 12px;
          font-size: 13px;
        }

        .auth-submit {
          width: 100%;
          justify-content: center;
          margin-top: 4px;
        }

        @media (max-width: 980px) {
          .auth-shell {
            grid-template-columns: 1fr;
          }

          .auth-copy {
            border-right: none;
            border-bottom: 1px solid var(--border-subtle);
            padding: 40px 24px 30px;
          }

          .auth-copy h1 {
            font-size: 36px;
          }

          .auth-card-wrapper {
            padding: 24px;
          }
        }
      `}</style>
    </div>
  );
}