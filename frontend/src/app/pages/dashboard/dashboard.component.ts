import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupportService } from '../../services/support.service';
import { SupportTicket } from '../../models/support-ticket.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  tickets: SupportTicket[] = [];
  selectedTicket: SupportTicket | null = null;
  isLoading = true;
  isResponding = false;
  responseText = '';
  statusFilter = '';
  successMessage = '';

  constructor(private supportService: SupportService) {}

  ngOnInit(): void {
    this.loadTickets();
  }

  /** Load tickets with optional status filter */
  loadTickets(): void {
    this.isLoading = true;
    this.supportService.getAllTickets(this.statusFilter || undefined).subscribe({
      next: (tickets) => {
        this.tickets = tickets;
        this.isLoading = false;
        // Refresh selected ticket if still exists
        if (this.selectedTicket) {
          const updated = tickets.find(t => t.id === this.selectedTicket!.id);
          this.selectedTicket = updated || null;
        }
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  /** Select a ticket to view details */
  selectTicket(ticket: SupportTicket): void {
    this.selectedTicket = ticket;
    this.responseText = '';
    this.successMessage = '';
  }

  /** Close the ticket detail panel */
  closePanel(): void {
    this.selectedTicket = null;
    this.responseText = '';
  }

  /** Submit a human response to the ticket */
  submitResponse(): void {
    if (!this.selectedTicket || !this.responseText.trim()) return;

    this.isResponding = true;
    this.supportService.respondToTicket(this.selectedTicket.id, this.responseText.trim()).subscribe({
      next: (updated) => {
        this.selectedTicket = updated;
        this.responseText = '';
        this.isResponding = false;
        this.successMessage = 'Response submitted successfully';
        this.loadTickets();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: () => {
        this.isResponding = false;
      }
    });
  }

  /** Resolve a ticket */
  resolveTicket(): void {
    if (!this.selectedTicket) return;

    this.supportService.resolveTicket(this.selectedTicket.id).subscribe({
      next: (updated) => {
        this.selectedTicket = updated;
        this.successMessage = 'Ticket resolved successfully';
        this.loadTickets();
        setTimeout(() => this.successMessage = '', 3000);
      }
    });
  }

  /** Filter by status */
  filterByStatus(status: string): void {
    this.statusFilter = status;
    this.loadTickets();
  }

  /** Get status display info */
  getStatusLabel(status: string): string {
    return this.supportService.getStatusLabel(status);
  }

  /** Format date relative */
  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  /** Format confidence */
  formatConfidence(score: number): string {
    return (score * 100).toFixed(1) + '%';
  }

  /** Get ticket counts by status */
  getTicketCount(status: string): number {
    if (!status) return this.tickets.length;
    return this.tickets.filter(t => t.status === status).length;
  }
}
