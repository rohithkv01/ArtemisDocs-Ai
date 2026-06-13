package com.artemisdocs.backend.service;

import com.artemisdocs.backend.dto.SupportTicketResponse;
import com.artemisdocs.backend.entity.Document;
import com.artemisdocs.backend.entity.SupportTicket;
import com.artemisdocs.backend.entity.SupportTicket.TicketStatus;
import com.artemisdocs.backend.exception.GlobalExceptionHandler.EntityNotFoundException;
import com.artemisdocs.backend.repository.DocumentRepository;
import com.artemisdocs.backend.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing escalated support tickets.
 * Provides CRUD operations and status lifecycle management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SupportService {

    private final SupportTicketRepository supportTicketRepository;
    private final DocumentRepository documentRepository;

    /**
     * Retrieves all support tickets, optionally filtered by status.
     *
     * @param status optional status filter
     * @return list of ticket response DTOs
     */
    public List<SupportTicketResponse> getAllTickets(String status) {
        List<SupportTicket> tickets;

        if (status != null && !status.isBlank()) {
            try {
                TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
                tickets = supportTicketRepository.findByStatus(ticketStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + status +
                        ". Valid values are: OPEN, IN_PROGRESS, RESOLVED");
            }
        } else {
            tickets = supportTicketRepository.findAllByOrderByCreatedAtDesc();
        }

        return tickets.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves a single ticket by ID.
     *
     * @param id the ticket ID
     * @return ticket response DTO
     */
    public SupportTicketResponse getTicketById(Long id) {
        SupportTicket ticket = findTicketOrThrow(id);
        return toResponse(ticket);
    }

    /**
     * Adds a human agent's response to a ticket and marks it IN_PROGRESS.
     *
     * @param id       the ticket ID
     * @param response the human agent's response text
     * @return updated ticket response DTO
     */
    public SupportTicketResponse respondToTicket(Long id, String response) {
        SupportTicket ticket = findTicketOrThrow(id);

        ticket.setResponse(response);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        ticket = supportTicketRepository.save(ticket);
        log.info("Response added to ticket {}", id);

        return toResponse(ticket);
    }

    /**
     * Marks a ticket as resolved.
     *
     * @param id the ticket ID
     * @return updated ticket response DTO
     */
    public SupportTicketResponse resolveTicket(Long id) {
        SupportTicket ticket = findTicketOrThrow(id);

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());

        ticket = supportTicketRepository.save(ticket);
        log.info("Ticket {} resolved", id);

        return toResponse(ticket);
    }

    /**
     * Finds a ticket by ID or throws EntityNotFoundException.
     */
    private SupportTicket findTicketOrThrow(Long id) {
        return supportTicketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Support ticket not found with ID: " + id));
    }

    /**
     * Converts a SupportTicket entity to its response DTO,
     * enriching it with the associated document filename.
     */
    private SupportTicketResponse toResponse(SupportTicket ticket) {
        String documentFilename = null;
        if (ticket.getDocumentId() != null) {
            documentFilename = documentRepository.findById(ticket.getDocumentId())
                    .map(Document::getFilename)
                    .orElse("Unknown Document");
        }

        return new SupportTicketResponse(
                ticket.getId(),
                ticket.getSessionId(),
                ticket.getQuestion(),
                ticket.getContext(),
                ticket.getStatus().name(),
                ticket.getResponse(),
                ticket.getAiAnswer(),
                ticket.getConfidenceScore(),
                documentFilename,
                ticket.getDocumentId(),
                ticket.getCreatedAt(),
                ticket.getResolvedAt()
        );
    }
}
