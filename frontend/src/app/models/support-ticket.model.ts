/**
 * Represents an escalated support ticket.
 */
export interface SupportTicket {
  id: number;
  sessionId: string;
  question: string;
  context: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED';
  response: string | null;
  aiAnswer: string | null;
  confidenceScore: number;
  documentFilename: string;
  documentId: number;
  createdAt: string;
  resolvedAt: string | null;
}

/**
 * Request payload for responding to a support ticket.
 */
export interface SupportRespondRequest {
  response: string;
}
