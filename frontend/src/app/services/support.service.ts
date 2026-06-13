import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SupportTicket, SupportRespondRequest } from '../models/support-ticket.model';
import { environment } from '../../environments/environment';

/**
 * Service for managing escalated support tickets.
 */
@Injectable({
  providedIn: 'root'
})
export class SupportService {
  private readonly apiUrl = `${environment.apiUrl}/support`;

  constructor(private http: HttpClient) {}

  /**
   * Retrieves all support tickets, optionally filtered by status.
   */
  getAllTickets(status?: string): Observable<SupportTicket[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<SupportTicket[]>(`${this.apiUrl}/tickets`, { params });
  }

  /**
   * Retrieves a single ticket by ID.
   */
  getTicketById(id: number): Observable<SupportTicket> {
    return this.http.get<SupportTicket>(`${this.apiUrl}/tickets/${id}`);
  }

  /**
   * Adds a human agent's response to a ticket.
   */
  respondToTicket(id: number, response: string): Observable<SupportTicket> {
    const request: SupportRespondRequest = { response };
    return this.http.post<SupportTicket>(`${this.apiUrl}/respond/${id}`, request);
  }

  /**
   * Marks a ticket as resolved.
   */
  resolveTicket(id: number): Observable<SupportTicket> {
    return this.http.put<SupportTicket>(`${this.apiUrl}/resolve/${id}`, {});
  }

  /**
   * Returns a CSS class for the ticket status badge.
   */
  getStatusClass(status: string): string {
    switch (status) {
      case 'OPEN': return 'status-open';
      case 'IN_PROGRESS': return 'status-in-progress';
      case 'RESOLVED': return 'status-resolved';
      default: return '';
    }
  }

  /**
   * Returns a display-friendly label for ticket status.
   */
  getStatusLabel(status: string): string {
    switch (status) {
      case 'OPEN': return 'Open';
      case 'IN_PROGRESS': return 'In Progress';
      case 'RESOLVED': return 'Resolved';
      default: return status;
    }
  }
}
