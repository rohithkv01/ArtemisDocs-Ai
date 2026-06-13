package com.artemisdocs.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a single chat exchange (question + answer).
 * Grouped by sessionId for conversation history.
 */
@Entity
@Table(name = "chat_messages")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** UUID-based session identifier for grouping conversations */
    @Column(nullable = false)
    private String sessionId;

    /** The user's question */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    /** The AI-generated answer */
    @Column(columnDefinition = "TEXT")
    private String answer;

    /** Confidence score of the answer (0.0 to 1.0) */
    private Double confidenceScore;

    /** Reference to the document this question is about */
    private Long documentId;

    /** Timestamp when the message was created */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
