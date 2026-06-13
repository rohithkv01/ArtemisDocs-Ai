/**
 * Request payload for asking a question.
 */
export interface ChatRequest {
  sessionId: string;
  documentId: number;
  question: string;
}

/**
 * Response from the AI chat endpoint.
 */
export interface ChatResponse {
  sessionId: string;
  answer: string;
  confidenceScore: number;
  isEscalated: boolean;
  sources: string[];
}

/**
 * A single chat message in the conversation history.
 */
export interface ChatMessage {
  id: number;
  sessionId: string;
  question: string;
  answer: string;
  confidenceScore: number;
  documentId: number;
  createdAt: string;
}
