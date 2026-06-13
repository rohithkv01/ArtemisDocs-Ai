import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatRequest, ChatResponse, ChatMessage } from '../models/chat.model';
import { environment } from '../../environments/environment';

/**
 * Service for AI chat operations and conversation history.
 */
@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly apiUrl = `${environment.apiUrl}/chat`;

  constructor(private http: HttpClient) {}

  /**
   * Sends a question to the AI and receives an answer.
   */
  askQuestion(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.apiUrl}/ask`, request);
  }

  /**
   * Retrieves conversation history for a session.
   */
  getHistory(sessionId: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/history/${sessionId}`);
  }

  /**
   * Generates a new UUID-based session ID.
   */
  generateSessionId(): string {
    return crypto.randomUUID();
  }

  /**
   * Returns a confidence level label based on the score.
   */
  getConfidenceLevel(score: number): 'high' | 'medium' | 'low' {
    if (score >= 0.8) return 'high';
    if (score >= 0.65) return 'medium';
    return 'low';
  }

  /**
   * Formats confidence score as a percentage string.
   */
  formatConfidence(score: number): string {
    return (score * 100).toFixed(1) + '%';
  }
}
