package com.artemisdocs.backend.controller;

import com.artemisdocs.backend.dto.ChatRequest;
import com.artemisdocs.backend.dto.ChatResponse;
import com.artemisdocs.backend.entity.ChatMessage;
import com.artemisdocs.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for chat operations.
 * Handles asking questions and retrieving conversation history.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "http://localhost:*")
public class ChatController {

    private final ChatService chatService;

    /**
     * Asks a question about a document using RAG.
     * POST /api/chat/ask
     *
     * @param request the chat request with session ID, document ID, and question
     * @return ChatResponse with AI answer and confidence score
     */
    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(@Valid @RequestBody ChatRequest request) {
        log.info("Chat request: session={}, question='{}'",
                request.sessionId(), request.question());
        ChatResponse response = chatService.askQuestion(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves conversation history for a session.
     * GET /api/chat/history/{sessionId}
     *
     * @param sessionId the session identifier
     * @return ordered list of chat messages
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        List<ChatMessage> history = chatService.getHistory(sessionId);
        return ResponseEntity.ok(history);
    }
}
