import {
  ApiError,
  type ApiEnvelope,
  type AuthResponse,
  type ChatPayload,
  type ChatResponse,
  type Conversation,
  type CreateConversationPayload,
  type DocumentItem,
  type GenerateTestPayload,
  type LoginPayload,
  type Message,
  type RegisterPayload,
  type SubmitTestPayload,
  type TestSession,
  type UserProfile,
} from "@/lib/types";

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

function redirectToLogin() {
  if (typeof window !== "undefined") {
    window.location.assign("/");
  }
}

async function parseEnvelope<T>(response: Response): Promise<T> {
  const contentType = response.headers.get("Content-Type") || "";

  if (!contentType.includes("application/json")) {
    if (!response.ok) {
      throw new ApiError(response.statusText || "Request failed", response.status);
    }

    return response as unknown as T;
  }

  const envelope = (await response.json()) as ApiEnvelope<T>;

  if (!response.ok || !envelope.success) {
    throw new ApiError(envelope.message || response.statusText, response.status);
  }

  return envelope.data;
}

let refreshPromise: Promise<void> | null = null;

async function refreshSession() {
  if (!refreshPromise) {
    refreshPromise = (async () => {
      const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify({}),
        credentials: "include",
      });

      await parseEnvelope<AuthResponse>(response);
    })().finally(() => {
      refreshPromise = null;
    });
  }

  await refreshPromise;
}

async function request<T>(
  path: string,
  options: RequestInit = {},
  isRetry = false,
): Promise<T> {
  const isFormData = options.body instanceof FormData;
  const headers: HeadersInit = {
    Accept: "application/json",
    ...(isFormData ? {} : { "Content-Type": "application/json" }),
    ...(options.headers || {}),
  };

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    credentials: "include",
  });

  if (response.status === 401 && !isRetry) {
    try {
      await refreshSession();
      return request<T>(path, options, true);
    } catch {
      redirectToLogin();
      throw new ApiError("Session expired. Please sign in again.", 401);
    }
  }

  if (response.status === 401 && isRetry) {
    redirectToLogin();
  }

  return parseEnvelope<T>(response);
}

export const authApi = {
  login: (payload: LoginPayload) =>
    request<AuthResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  register: (payload: RegisterPayload) =>
    request<AuthResponse>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  me: () => request<UserProfile>("/api/auth/me"),

  logout: () =>
    request<void>("/api/auth/logout", {
      method: "POST",
      body: JSON.stringify({}),
    }),
};

export const conversationsApi = {
  list: () => request<Conversation[]>("/api/conversations"),

  create: (payload: CreateConversationPayload) =>
    request<Conversation>("/api/conversations", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  messages: (id: number) =>
    request<Message[]>(`/api/conversations/${id}/messages`),

  delete: (id: number) =>
    request<void>(`/api/conversations/${id}`, { method: "DELETE" }),
};

export const chatApi = {
  send: (conversationId: number, payload: ChatPayload) =>
    request<ChatResponse>(`/api/chat/${conversationId}`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),
};

export const documentsApi = {
  list: () => request<DocumentItem[]>("/api/documents"),

  upload: (file: File) => {
    const formData = new FormData();
    formData.append("file", file);

    return request<DocumentItem>("/api/documents/upload", {
      method: "POST",
      body: formData,
    });
  },

  download: (id: number) => request<Response>(`/api/documents/${id}/download`),

  delete: (id: number) =>
    request<void>(`/api/documents/${id}`, { method: "DELETE" }),
};

export const testsApi = {
  list: () => request<TestSession[]>("/api/tests"),

  generate: (payload: GenerateTestPayload) =>
    request<TestSession>("/api/tests/generate", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  results: (id: number) => request<TestSession>(`/api/tests/${id}/results`),

  submit: (id: number, payload: SubmitTestPayload) =>
    request<TestSession>(`/api/tests/${id}/submit`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),
};