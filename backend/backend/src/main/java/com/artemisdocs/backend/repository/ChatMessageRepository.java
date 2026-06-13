package com.artemisdocs.backend.repository;

import com.artemisdocs.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ChatMessage entities.
 * Provides session-based conversation history retrieval.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Retrieves all chat messages for a given session, ordered chronologically.
     *
     * @param sessionId the session identifier
     * @return ordered list of chat messages
     */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    /**
     * Retrieves all chat messages for a given document.
     *
     * @param documentId the document identifier
     * @return list of chat messages
     */
    List<ChatMessage> findByDocumentId(Long documentId);
}
