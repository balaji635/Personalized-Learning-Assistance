import { ApiEnvelope, ApiError, AuthResponse } from "./types";

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

async function parseEnvelope<T>(response: Response): Promise<T> {
  const contentType = response.headers.get("Content-Type") || "";

  if (!contentType.includes("application/json")) {
    if (!response.ok) throw new ApiError(response.statusText, response.status);
    return response as unknown as T;
  }

  const envelope: ApiEnvelope<T> = await response.json();

  if (!response.ok || !envelope.success) {
    throw new ApiError(envelope.message || response.statusText, response.status);
  }

  return envelope.data;
}

let isRefreshing = false;

async function request<T>(
  path: string,
  options: RequestInit = {},
  isRetry = false
): Promise<T> {
  const isFormData = options.body instanceof FormData;

  const headers: Record<string, string> = {
    Accept: "application/json",
    ...(isFormData ? {} : { "Content-Type": "application/json" }),
    ...(options.headers as Record<string, string>),
  };

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    credentials: "include",
  });

  if (response.status === 401 && !isRetry && !isRefreshing) {
    isRefreshing = true;
    try {
      await request<AuthResponse>("/api/auth/refresh", {
        method: "POST",
        body: JSON.stringify({}),
      });
      isRefreshing = false;
      return request<T>(path, options, true);
    } catch {
      isRefreshing = false;
      throw new ApiError("Session expired. Please log in again.", 401);
    }
  }

  return parseEnvelope<T>(response);
}

// ─── Auth ─────────────────────────────────────────────────────────────────────
export const authApi = {
  login: (payload: import("./types").LoginPayload) =>
    request<import("./types").AuthResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  register: (payload: import("./types").RegisterPayload) =>
    request<import("./types").AuthResponse>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  me: () => request<import("./types").UserProfile>("/api/auth/me"),

  logout: () =>
    request<void>("/api/auth/logout", { method: "POST", body: JSON.stringify({}) }),
};

// ─── Conversations ────────────────────────────────────────────────────────────
export const conversationsApi = {
  list: () => request<import("./types").Conversation[]>("/api/conversations"),

  create: (payload: import("./types").CreateConversationPayload) =>
    request<import("./types").Conversation>("/api/conversations", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  messages: (id: number) =>
    request<import("./types").Message[]>(`/api/conversations/${id}/messages`),

  delete: (id: number) =>
    request<void>(`/api/conversations/${id}`, { method: "DELETE" }),
};

// ─── Chat ─────────────────────────────────────────────────────────────────────
export const chatApi = {
  send: (conversationId: number, payload: import("./types").ChatPayload) =>
    request<import("./types").ChatResponse>(`/api/chat/${conversationId}`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),
};

// ─── Documents ────────────────────────────────────────────────────────────────
export const documentsApi = {
  list: () => request<import("./types").DocumentItem[]>("/api/documents"),

  upload: (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return request<import("./types").DocumentItem>("/api/documents/upload", {
      method: "POST",
      body: formData,
    });
  },

  download: (id: number) =>
    request<Response>(`/api/documents/${id}/download`),

  delete: (id: number) =>
    request<void>(`/api/documents/${id}`, { method: "DELETE" }),
};

// ─── Tests ────────────────────────────────────────────────────────────────────
export const testsApi = {
  list: () => request<import("./types").TestSession[]>("/api/tests"),

  generate: (payload: import("./types").GenerateTestPayload) =>
    request<import("./types").TestSession>("/api/tests/generate", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  results: (id: number) =>
    request<import("./types").TestSession>(`/api/tests/${id}/results`),

  submit: (id: number, payload: import("./types").SubmitTestPayload) =>
    request<import("./types").TestSession>(`/api/tests/${id}/submit`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),
};
