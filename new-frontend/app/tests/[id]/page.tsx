"use client";

import { useEffect, useState, useTransition } from "react";
import { useParams, useRouter } from "next/navigation";
import { testsApi } from "@/lib/api";
import type { TestSession, TestQuestion } from "@/lib/types";

export default function TestPage() {
  const params = useParams();
  const router = useRouter();
  const id = Number(params.id);

  const [session, setSession] = useState<TestSession | null>(null);
  const [loading, setLoading] = useState(true);
  const [answers, setAnswers] = useState<Record<number, number>>({});
  const [submitted, setSubmitted] = useState(false);
  const [isPending, startTransition] = useTransition();
  const [current, setCurrent] = useState(0);

  useEffect(() => {
    testsApi.results(id).then((s) => {
      setSession(s);
      if (s.status === "SUBMITTED") setSubmitted(true);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [id]);

  function handleSelect(questionId: number, optionIndex: number) {
    if (submitted) return;
    setAnswers((a) => ({ ...a, [questionId]: optionIndex }));
  }

  function handleSubmit() {
    startTransition(async () => {
      const result = await testsApi.submit(id, { answers });
      setSession(result);
      setSubmitted(true);
      setCurrent(0);
    });
  }

  if (loading) {
    return (
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100vh" }}>
        <div className="loading-spinner" />
      </div>
    );
  }

  if (!session || !session.questions?.length) {
    return (
      <div style={{ padding: 40, textAlign: "center", color: "var(--text-muted)" }}>
        Test not found.
      </div>
    );
  }

  const questions = session.questions;
  const q = questions[current];
  const answeredCount = Object.keys(answers).length;
  const progress = submitted ? 100 : (answeredCount / questions.length) * 100;
  const isGood = (session.scorePercentage ?? 0) >= 60;

  return (
    <div className="test-page">
      {/* ── Header ── */}
      <header className="test-header">
        <div>
          <div className="font-mono test-breadcrumb">
            <button onClick={() => router.push("/tests")}>Assessments</button>
            <span>›</span>
            <span>{session.topic}</span>
          </div>
          <h1 className="font-display test-title">{session.topic}</h1>
        </div>

        <div className="test-meta">
          <span className={`badge badge-${session.difficultyLevel.toLowerCase()}`}>
            {session.difficultyLevel}
          </span>
          <span className="font-mono test-count">
            {questions.length} questions
          </span>
          {submitted && session.scorePercentage != null && (
            <div className={`score-badge font-mono ${isGood ? "good" : "bad"}`}>
              {Math.round(session.scorePercentage)}%
            </div>
          )}
        </div>
      </header>

      {/* Progress */}
      <div className="test-progress">
        <div className="progress-bar">
          <div className="progress-fill" style={{ width: `${progress}%` }} />
        </div>
        {!submitted && (
          <span className="font-mono progress-label">
            {answeredCount} / {questions.length} answered
          </span>
        )}
      </div>

      <div className="test-body">
        {/* Results summary */}
        {submitted && session.correctAnswers != null && (
          <div className={`results-summary animate-fade-up ${isGood ? "good" : "bad"}`}>
            <div className="results-score font-display">
              {Math.round(session.scorePercentage ?? 0)}
              <span>%</span>
            </div>
            <div>
              <div className="results-label">
                {isGood ? "Great work!" : "Keep practicing"}
              </div>
              <div className="results-sub font-mono">
                {session.correctAnswers} of {session.totalQuestions} correct
              </div>
            </div>
          </div>
        )}

        <div className="test-layout">
          {/* Question nav */}
          <div className="question-nav">
            <p className="font-mono nav-label">QUESTIONS</p>
            {questions.map((q, i) => {
              let state = "unanswered";
              if (answers[q.id] !== undefined && !submitted) state = "answered";
              if (submitted) {
                state =
                  q.selectedOptionIndex === q.correctOptionIndex
                    ? "correct"
                    : "incorrect";
              }
              return (
                <button
                  key={q.id}
                  className={`q-nav-btn font-mono ${state} ${i === current ? "active" : ""}`}
                  onClick={() => setCurrent(i)}
                >
                  {i + 1}
                </button>
              );
            })}
          </div>

          {/* Question body */}
          <div className="question-area animate-fade-up" key={current}>
            <div className="q-number font-mono">Question {current + 1} of {questions.length}</div>
            <h2 className="font-display q-text">{q.question}</h2>

            <div className="options-list">
              {q.options.map((opt) => {
                const isSelected =
                  submitted
                    ? q.selectedOptionIndex === opt.optionIndex
                    : answers[q.id] === opt.optionIndex;
                const isCorrect = submitted && opt.optionIndex === q.correctOptionIndex;
                const isWrong =
                  submitted &&
                  isSelected &&
                  opt.optionIndex !== q.correctOptionIndex;

                return (
                  <button
                    key={opt.id}
                    className={`option-btn ${isSelected && !submitted ? "selected" : ""} ${isCorrect ? "correct" : ""} ${isWrong ? "incorrect" : ""}`}
                    onClick={() => handleSelect(q.id, opt.optionIndex)}
                    disabled={submitted}
                  >
                    <span className="opt-index font-mono">{String.fromCharCode(65 + opt.optionIndex)}</span>
                    <span>{opt.optionText}</span>
                    {isCorrect && <span className="opt-indicator">✓</span>}
                    {isWrong && <span className="opt-indicator wrong">✕</span>}
                  </button>
                );
              })}
            </div>

            {/* Explanation (after submit) */}
            {submitted && (
              <div className="explanation animate-fade-up">
                <div className="font-mono explanation-label">EXPLANATION</div>
                <p>{q.explanation}</p>
              </div>
            )}

            {/* Navigation */}
            <div className="q-nav-controls">
              <button
                className="btn btn-secondary"
                disabled={current === 0}
                onClick={() => setCurrent((c) => c - 1)}
              >
                ← Previous
              </button>
              {current < questions.length - 1 ? (
                <button
                  className="btn btn-secondary"
                  onClick={() => setCurrent((c) => c + 1)}
                >
                  Next →
                </button>
              ) : !submitted ? (
                <button
                  className="btn btn-primary"
                  onClick={handleSubmit}
                  disabled={isPending || answeredCount < questions.length}
                >
                  {isPending
                    ? "Submitting…"
                    : answeredCount < questions.length
                    ? `Answer all (${questions.length - answeredCount} left)`
                    : "Submit test"}
                </button>
              ) : (
                <button className="btn btn-secondary" onClick={() => router.push("/tests")}>
                  Back to tests
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      <style jsx>{`
        .test-page { display: flex; flex-direction: column; min-height: 100vh; }
        .test-header {
          display: flex; align-items: flex-start; justify-content: space-between;
          padding: 32px 40px 20px;
          border-bottom: 1px solid var(--border-subtle);
          gap: 16px;
        }
        .test-breadcrumb {
          font-size: 11px; letter-spacing: 0.06em; color: var(--text-muted);
          display: flex; align-items: center; gap: 6px; margin-bottom: 8px;
        }
        .test-breadcrumb button { background: none; border: none; color: var(--text-muted); cursor: pointer; font: inherit; padding: 0; }
        .test-breadcrumb button:hover { color: var(--gold-bright); }
        .test-title { font-size: 26px; font-weight: 300; color: var(--cream); margin: 0; letter-spacing: -0.02em; }
        .test-meta { display: flex; align-items: center; gap: 10px; flex-shrink: 0; padding-top: 4px; }
        .test-count { font-size: 11px; letter-spacing: 0.06em; color: var(--text-muted); }
        .score-badge { font-size: 14px; font-weight: 600; letter-spacing: 0.06em; }
        .score-badge.good { color: var(--success); }
        .score-badge.bad  { color: var(--error); }
        .test-progress { padding: 12px 40px; border-bottom: 1px solid var(--border-subtle); display: flex; align-items: center; gap: 12px; }
        .test-progress .progress-bar { flex: 1; }
        .progress-label { font-size: 10px; letter-spacing: 0.08em; color: var(--text-muted); flex-shrink: 0; }
        .test-body { padding: 32px 40px; flex: 1; }

        .results-summary {
          border-radius: var(--radius-lg);
          border: 1px solid;
          padding: 20px 24px;
          display: flex; align-items: center; gap: 20px;
          margin-bottom: 28px;
        }
        .results-summary.good { background: rgba(92,158,106,0.08); border-color: rgba(92,158,106,0.25); }
        .results-summary.bad  { background: rgba(192,88,88,0.08);  border-color: rgba(192,88,88,0.25); }
        .results-score {
          font-size: 48px; font-weight: 300;
          line-height: 1;
        }
        .results-summary.good .results-score { color: var(--success); }
        .results-summary.bad  .results-score { color: var(--error); }
        .results-score span { font-size: 24px; }
        .results-label { font-size: 16px; font-weight: 500; color: var(--text-primary); margin-bottom: 2px; }
        .results-sub { font-size: 11px; letter-spacing: 0.06em; color: var(--text-muted); }

        .test-layout { display: grid; grid-template-columns: 120px 1fr; gap: 32px; align-items: start; }

        .question-nav { position: sticky; top: 20px; }
        .nav-label { font-size: 9px; letter-spacing: 0.12em; color: var(--text-muted); margin: 0 0 10px; }
        .q-nav-btn-wrap { display: flex; flex-wrap: wrap; gap: 6px; }
        .q-nav-btn {
          width: 32px; height: 32px;
          border-radius: var(--radius-sm);
          border: 1px solid var(--border-subtle);
          background: var(--bg-elevated);
          color: var(--text-muted);
          font-size: 11px; letter-spacing: 0.04em;
          cursor: pointer;
          transition: all 0.15s;
          display: flex; align-items: center; justify-content: center;
          margin-bottom: 6px;
        }
        .q-nav-btn.answered { border-color: var(--gold-muted); background: var(--gold-glow); color: var(--gold-bright); }
        .q-nav-btn.correct  { border-color: var(--success); background: rgba(92,158,106,0.1); color: var(--success); }
        .q-nav-btn.incorrect { border-color: var(--error); background: rgba(192,88,88,0.1); color: var(--error); }
        .q-nav-btn.active   { outline: 2px solid var(--gold-bright); outline-offset: 2px; }

        .question-area { display: flex; flex-direction: column; gap: 20px; max-width: 680px; }
        .q-number { font-size: 10px; letter-spacing: 0.1em; color: var(--text-muted); }
        .q-text { font-size: 22px; font-weight: 400; color: var(--cream); margin: 0; letter-spacing: -0.01em; line-height: 1.35; }
        .options-list { display: flex; flex-direction: column; gap: 10px; }
        .opt-index {
          width: 22px; height: 22px;
          border-radius: 4px;
          background: var(--bg-overlay);
          display: flex; align-items: center; justify-content: center;
          font-size: 11px; letter-spacing: 0;
          flex-shrink: 0;
        }
        .opt-indicator { margin-left: auto; font-size: 12px; }
        .opt-indicator.wrong { color: var(--error); }
        .explanation {
          background: var(--bg-elevated);
          border: 1px solid var(--border-subtle);
          border-left: 3px solid var(--gold-muted);
          border-radius: var(--radius);
          padding: 16px 18px;
        }
        .explanation-label { font-size: 9px; letter-spacing: 0.1em; color: var(--text-muted); margin-bottom: 8px; }
        .explanation p { margin: 0; font-size: 14px; color: var(--text-secondary); line-height: 1.6; }
        .q-nav-controls { display: flex; justify-content: space-between; padding-top: 8px; }
        .loading-spinner {
          width: 36px; height: 36px;
          border: 2px solid var(--border-subtle);
          border-top-color: var(--gold-bright);
          border-radius: 50%;
          animation: spin 0.8s linear infinite;
        }
        @keyframes spin { to { transform: rotate(360deg); } }
        @media (max-width: 720px) { .test-layout { grid-template-columns: 1fr; } .question-nav { position: static; } }
      `}</style>
    </div>
  );
}
