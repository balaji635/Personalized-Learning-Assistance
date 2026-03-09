// ─── Enums ────────────────────────────────────────────────────────────────────
export type DifficultyLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type MessageRole = "USER" | "ASSISTANT";
export type DocumentStatus = "PROCESSING" | "READY" | "FAILED";
export type TestStatus = "GENERATED" | "SUBMITTED";

// ─── API Envelope ─────────────────────────────────────────────────────────────
export interface ApiEnvelope<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number
  ) {
    super(message);
    this.name = "ApiError";
  }
}

// ─── Auth ─────────────────────────────────────────────────────────────────────
export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload extends LoginPayload {
  firstName: string;
  lastName: string;
}

export interface UserProfile {
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
}

export interface AuthResponse extends UserProfile {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

// ─── Conversations ────────────────────────────────────────────────────────────
export interface CreateConversationPayload {
  title: string;
  difficultyLevel: DifficultyLevel;
}

export interface Conversation {
  id: number;
  title: string;
  difficultyLevel: DifficultyLevel;
  createdAt: string;
  updatedAt: string;
}

export interface Message {
  id: number | string;
  role: MessageRole;
  content: string;
  createdAt: string;
}

export interface ChatPayload {
  message: string;
}

export interface ChatResponse {
  messageId: number | null;
  role: string;
  content: string;
  conversationId: number;
  timestamp: string;
}

// ─── Documents ────────────────────────────────────────────────────────────────
export interface DocumentItem {
  id: number;
  originalFileName: string;
  fileType: string;
  fileSize: number;
  chunkCount: number | null;
  status: DocumentStatus;
  uploadedAt: string;
}

// ─── Tests ────────────────────────────────────────────────────────────────────
export interface GenerateTestPayload {
  topic: string;
  questionCount: number;
  difficultyLevel: DifficultyLevel;
  documentId?: number;
}

export interface SubmitTestPayload {
  answers: Record<number, number>;
}

export interface QuestionOption {
  id: number;
  optionIndex: number;
  optionText: string;
}

export interface TestQuestion {
  id: number;
  question: string;
  correctOptionIndex: number;
  explanation: string;
  questionOrder: number;
  selectedOptionIndex: number | null;
  options: QuestionOption[];
}

export interface TestSession {
  id: number;
  topic: string;
  difficultyLevel: DifficultyLevel;
  status: TestStatus;
  totalQuestions: number;
  correctAnswers: number | null;
  scorePercentage: number | null;
  createdAt: string;
  submittedAt: string | null;
  questions?: TestQuestion[];
}
