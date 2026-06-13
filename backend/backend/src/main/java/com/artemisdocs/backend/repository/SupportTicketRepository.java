package com.artemisdocs.backend.repository;

import com.artemisdocs.backend.entity.SupportTicket;
import com.artemisdocs.backend.entity.SupportTicket.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for SupportTicket entities.
 * Provides status-based and session-based filtering.
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    /**
     * Retrieves tickets filtered by status.
     *
     * @param status the ticket status to filter by
     * @return list of matching tickets
     */
    List<SupportTicket> findByStatus(TicketStatus status);

    /**
     * Retrieves all tickets for a given session.
     *
     * @param sessionId the session identifier
     * @return list of tickets
     */
    List<SupportTicket> findBySessionId(String sessionId);

    /**
     * Retrieves all tickets ordered by creation date (newest first).
     *
     * @return ordered list of all tickets
     */
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
