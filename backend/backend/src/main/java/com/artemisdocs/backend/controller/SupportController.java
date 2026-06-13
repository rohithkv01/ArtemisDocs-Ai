package com.artemisdocs.backend.controller;

import com.artemisdocs.backend.dto.SupportRespondRequest;
import com.artemisdocs.backend.dto.SupportTicketResponse;
import com.artemisdocs.backend.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the human support dashboard.
 * Handles viewing, responding to, and resolving escalated tickets.
 */
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "http://localhost:*")
public class SupportController {

    private final SupportService supportService;

    /**
     * Retrieves all support tickets, optionally filtered by status.
     * GET /api/support/tickets?status=OPEN
     *
     * @param status optional status filter (OPEN, IN_PROGRESS, RESOLVED)
     * @return list of support ticket DTOs
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicketResponse>> getAllTickets(
            @RequestParam(required = false) String status) {
        List<SupportTicketResponse> tickets = supportService.getAllTickets(status);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Retrieves a single support ticket by ID.
     * GET /api/support/tickets/{id}
     *
     * @param id the ticket ID
     * @return ticket details
     */
    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicketResponse> getTicketById(@PathVariable Long id) {
        SupportTicketResponse ticket = supportService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Adds a human agent's response to a support ticket.
     * POST /api/support/respond/{id}
     *
     * @param id      the ticket ID
     * @param request the response text
     * @return updated ticket
     */
    @PostMapping("/respond/{id}")
    public ResponseEntity<SupportTicketResponse> respondToTicket(
            @PathVariable Long id,
            @Valid @RequestBody SupportRespondRequest request) {
        log.info("Support response for ticket {}", id);
        SupportTicketResponse ticket = supportService.respondToTicket(id, request.response());
        return ResponseEntity.ok(ticket);
    }

    /**
     * Marks a support ticket as resolved.
     * PUT /api/support/resolve/{id}
     *
     * @param id the ticket ID
     * @return updated ticket
     */
    @PutMapping("/resolve/{id}")
    public ResponseEntity<SupportTicketResponse> resolveTicket(@PathVariable Long id) {
        log.info("Resolving ticket {}", id);
        SupportTicketResponse ticket = supportService.resolveTicket(id);
        return ResponseEntity.ok(ticket);
    }
}
