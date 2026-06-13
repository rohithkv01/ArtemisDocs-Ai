package com.artemisdocs.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an escalated support ticket.
 * Created when AI confidence is below the threshold.
 */
@Entity
@Table(name = "support_tickets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Session ID linking to the conversation that triggered escalation */
    @Column(nullable = false)
    private String sessionId;

    /** The question that could not be answered confidently */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    /** Relevant document context retrieved during the attempt */
    @Column(columnDefinition = "TEXT")
    private String context;

    /** Ticket status: OPEN, IN_PROGRESS, or RESOLVED */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    /** Human agent's response to the question */
    @Column(columnDefinition = "TEXT")
    private String response;

    /** Reference to the document this ticket is about */
    private Long documentId;

    /** The AI-generated answer (even if low confidence) */
    @Column(columnDefinition = "TEXT")
    private String aiAnswer;

    /** The confidence score that triggered escalation */
    private Double confidenceScore;

    /** Timestamp when the ticket was created */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the ticket was resolved */
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TicketStatus.OPEN;
        }
    }

    /** Enum representing ticket lifecycle states */
    public enum TicketStatus {
        OPEN,
        IN_PROGRESS,
        RESOLVED
    }
}
