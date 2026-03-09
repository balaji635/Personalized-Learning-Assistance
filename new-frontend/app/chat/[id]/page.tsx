"use client";

import { useEffect, useRef, useState, useTransition } from "react";
import { useParams } from "next/navigation";
import { chatApi, conversationsApi } from "@/lib/api";
import type { Message } from "@/lib/types";

export default function ConversationPage() {
  const params = useParams();
  const id = Number(params.id);

  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(true);
  const [isSending, startTransition] = useTransition();
  const bottomRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    conversationsApi.messages(id).then((msgs) => {
      setMessages(msgs);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  function send(e?: React.FormEvent) {
    e?.preventDefault();
    const text = input.trim();
    if (!text || isSending) return;

    const userMsg: Message = {
      id: `temp-${Date.now()}`,
      role: "USER",
      content: text,
      createdAt: new Date().toISOString(),
    };

    setMessages((m) => [...m, userMsg]);
    setInput("");

    startTransition(async () => {
      try {
        const res = await chatApi.send(id, { message: text });
        const assistantMsg: Message = {
          id: res.messageId ?? `ai-${Date.now()}`,
          role: "ASSISTANT",
          content: res.content,
          createdAt: res.timestamp,
        };
        setMessages((m) => [...m, assistantMsg]);
      } catch {
        setMessages((m) => m.filter((x) => x.id !== userMsg.id));
        setInput(text);
      }
    });
  }

  function handleKey(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  }

  return (
    <div className="chat-layout">
      {/* Messages */}
      <div className="chat-messages">
        {loading ? (
          <div className="chat-loading">
            <div className="loading-dots">
              <span /><span /><span />
            </div>
          </div>
        ) : messages.length === 0 ? (
          <div className="chat-welcome animate-fade-up">
            <div className="welcome-icon">⟡</div>
            <h2 className="font-display">Ask me anything</h2>
            <p>I&apos;ll adapt my explanations to your level.</p>
            <div className="suggestion-chips">
              {[
                "Explain the core concept",
                "Give me an example",
                "What are common mistakes?",
                "Quiz me on this",
              ].map((s) => (
                <button
                  key={s}
                  className="suggestion-chip"
                  onClick={() => { setInput(s); inputRef.current?.focus(); }}
                >
                  {s}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="messages-inner">
            {messages.map((msg, i) => (
              <div
                key={msg.id}
                className={`message-wrap ${msg.role === "USER" ? "user" : "assistant"} animate-fade-up`}
                style={{ animationDelay: `${Math.min(i * 0.04, 0.3)}s` }}
              >
                {msg.role === "ASSISTANT" && (
                  <div className="msg-avatar font-mono">AI</div>
                )}
                <div className={`message-bubble message-${msg.role === "USER" ? "user" : "assistant"}`}>
                  <p style={{ margin: 0, whiteSpace: "pre-wrap" }}>{msg.content}</p>
                </div>
                {msg.role === "USER" && (
                  <div className="msg-avatar msg-avatar-user font-mono">Me</div>
                )}
              </div>
            ))}

            {isSending && (
              <div className="message-wrap assistant animate-fade-in">
                <div className="msg-avatar font-mono">AI</div>
                <div className="message-bubble message-assistant typing-indicator">
                  <span /><span /><span />
                </div>
              </div>
            )}
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="chat-input-bar">
        <form onSubmit={send} className="chat-input-form">
          <textarea
            ref={inputRef}
            className="chat-textarea"
            placeholder="Send a message… (Enter to send, Shift+Enter for newline)"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKey}
            rows={1}
            disabled={isSending}
          />
          <button
            type="submit"
            className="btn btn-primary chat-send-btn"
            disabled={!input.trim() || isSending}
          >
            Send
          </button>
        </form>
        <p className="chat-hint font-mono">
          SHIFT+ENTER for newlines · ENTER to send
        </p>
      </div>

      <style jsx>{`
        .chat-layout {
          display: flex;
          flex-direction: column;
          height: 100vh;
          overflow: hidden;
        }
        .chat-messages {
          flex: 1;
          overflow-y: auto;
          padding: 32px 40px;
          display: flex;
          flex-direction: column;
        }
        .messages-inner {
          display: flex;
          flex-direction: column;
          gap: 20px;
          max-width: 760px;
          width: 100%;
          margin: 0 auto;
        }
        .message-wrap {
          display: flex;
          align-items: flex-end;
          gap: 10px;
        }
        .message-wrap.user { flex-direction: row-reverse; }
        .msg-avatar {
          width: 28px; height: 28px;
          border-radius: 50%;
          background: var(--bg-overlay);
          border: 1px solid var(--border-subtle);
          display: flex; align-items: center; justify-content: center;
          font-size: 9px;
          letter-spacing: 0.05em;
          color: var(--text-muted);
          flex-shrink: 0;
        }
        .msg-avatar-user {
          background: var(--gold-dim);
          border-color: var(--gold-muted);
          color: var(--gold-bright);
        }
        .chat-welcome {
          margin: auto;
          text-align: center;
          padding: 48px;
          display: flex; flex-direction: column; align-items: center; gap: 12px;
        }
        .welcome-icon { font-size: 36px; color: var(--gold-bright); opacity: 0.7; }
        .chat-welcome h2 { font-size: 28px; font-weight: 300; color: var(--cream); margin: 0; }
        .chat-welcome p { color: var(--text-muted); font-size: 14px; margin: 0; }
        .suggestion-chips { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; margin-top: 8px; }
        .suggestion-chip {
          padding: 7px 14px;
          border-radius: 99px;
          border: 1px solid var(--border-subtle);
          background: var(--bg-elevated);
          color: var(--text-secondary);
          font-size: 13px;
          cursor: pointer;
          transition: all 0.15s;
          font-family: var(--font-body);
        }
        .suggestion-chip:hover {
          border-color: var(--gold-muted);
          color: var(--cream);
          background: var(--bg-overlay);
        }
        .typing-indicator {
          display: flex; gap: 4px; align-items: center;
          padding: 14px 18px;
        }
        .typing-indicator span {
          width: 6px; height: 6px;
          border-radius: 50%;
          background: var(--text-muted);
          animation: pulse-gold 1.2s ease-in-out infinite;
        }
        .typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
        .typing-indicator span:nth-child(3) { animation-delay: 0.4s; }
        .chat-loading {
          display: flex; align-items: center; justify-content: center;
          flex: 1;
        }
        .loading-dots { display: flex; gap: 6px; }
        .loading-dots span {
          width: 8px; height: 8px;
          border-radius: 50%;
          background: var(--gold-muted);
          animation: pulse-gold 1.2s infinite;
        }
        .loading-dots span:nth-child(2) { animation-delay: 0.2s; }
        .loading-dots span:nth-child(3) { animation-delay: 0.4s; }
        .chat-input-bar {
          border-top: 1px solid var(--border-subtle);
          padding: 16px 40px 12px;
          background: var(--bg-base);
        }
        .chat-input-form {
          display: flex;
          gap: 10px;
          max-width: 760px;
          margin: 0 auto;
        }
        .chat-textarea {
          flex: 1;
          min-height: 42px;
          max-height: 180px;
          padding: 10px 14px;
          background: var(--bg-elevated);
          border: 1px solid var(--border-subtle);
          border-radius: var(--radius);
          color: var(--text-primary);
          font-family: var(--font-body);
          font-size: 14px;
          resize: none;
          outline: none;
          transition: border-color 0.18s, box-shadow 0.18s;
          line-height: 1.5;
        }
        .chat-textarea:focus {
          border-color: var(--gold-muted);
          box-shadow: 0 0 0 3px var(--gold-glow);
        }
        .chat-textarea::placeholder { color: var(--text-muted); }
        .chat-send-btn { align-self: flex-end; flex-shrink: 0; }
        .chat-hint {
          font-size: 9px; letter-spacing: 0.08em;
          color: var(--text-muted); text-align: center;
          max-width: 760px; margin: 6px auto 0;
        }
      `}</style>
    </div>
  );
}
