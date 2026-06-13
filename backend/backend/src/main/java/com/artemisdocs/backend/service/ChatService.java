package com.artemisdocs.backend.service;

import com.artemisdocs.backend.ai.ConfidenceEvaluator;
import com.artemisdocs.backend.ai.GeminiClient;
import com.artemisdocs.backend.dto.ChatRequest;
import com.artemisdocs.backend.dto.ChatResponse;
import com.artemisdocs.backend.entity.ChatMessage;
import com.artemisdocs.backend.entity.SupportTicket;
import com.artemisdocs.backend.rag.RetrievalService;
import com.artemisdocs.backend.rag.VectorStoreService;
import com.artemisdocs.backend.repository.ChatMessageRepository;
import com.artemisdocs.backend.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service handling the complete chat flow:
 * 1. Retrieve relevant context from vector store
 * 2. Generate answer using Gemini
 * 3. Evaluate confidence
 * 4. Persist chat message
 * 5. Escalate if confidence is low
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final RetrievalService retrievalService;
    private final GeminiClient geminiClient;
    private final ConfidenceEvaluator confidenceEvaluator;
    private final ChatMessageRepository chatMessageRepository;
    private final SupportTicketRepository supportTicketRepository;

    /**
     * Processes a user question through the full RAG pipeline.
     *
     * @param request the chat request with session ID, document ID, and question
     * @return ChatResponse with answer, confidence, and escalation status
     */
    public ChatResponse askQuestion(ChatRequest request) {
        log.info("Processing question for session={}, doc={}: '{}'",
                request.sessionId(), request.documentId(), request.question());

        // Step 1: Retrieve relevant context
        List<VectorStoreService.RetrievalResult> retrievalResults =
                retrievalService.retrieve(request.question());

        // Step 2: Extract text chunks for the prompt context
        List<String> contextChunks = retrievalResults.stream()
                .map(VectorStoreService.RetrievalResult::text)
                .toList();

        // Step 3: Generate answer using Gemini
        String answer;
        if (contextChunks.isEmpty()) {
            answer = "I could not find sufficient information in the document to answer this question. " +
                    "The uploaded document may not contain relevant content for your query.";
        } else {
            answer = geminiClient.generateAnswer(request.question(), contextChunks);
        }

        // Step 4: Evaluate confidence
        ConfidenceEvaluator.ConfidenceResult confidence =
                confidenceEvaluator.evaluate(answer, retrievalResults);

        // Step 5: Persist the chat message
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(request.sessionId())
                .question(request.question())
                .answer(answer)
                .confidenceScore(confidence.score())
                .documentId(request.documentId())
                .build();

        chatMessageRepository.save(chatMessage);

        // Step 6: Create support ticket if confidence is low
        boolean isEscalated = false;
        if (confidence.shouldEscalate()) {
            createSupportTicket(request, answer, contextChunks, confidence.score());
            isEscalated = true;
            log.info("Question escalated for session={}, confidence={}",
                    request.sessionId(), confidence.score());
        }

        // Build source references
        List<String> sources = retrievalResults.stream()
                .map(r -> "Chunk " + r.chunkIndex() + " (score: " + String.format("%.2f", r.score()) + ")")
                .toList();

        return new ChatResponse(
                request.sessionId(),
                answer,
                confidence.score(),
                isEscalated,
                sources
        );
    }

    /**
     * Retrieves conversation history for a session.
     *
     * @param sessionId the session identifier
     * @return ordered list of chat messages
     */
    public List<ChatMessage> getHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    /**
     * Creates a support ticket for a low-confidence answer.
     */
    private void createSupportTicket(ChatRequest request, String answer,
                                      List<String> contextChunks, double confidenceScore) {
        String context = contextChunks.isEmpty()
                ? "No relevant context found in the document."
                : String.join("\n\n---\n\n", contextChunks);

        SupportTicket ticket = SupportTicket.builder()
                .sessionId(request.sessionId())
                .question(request.question())
                .context(context)
                .status(SupportTicket.TicketStatus.OPEN)
                .documentId(request.documentId())
                .aiAnswer(answer)
                .confidenceScore(confidenceScore)
                .build();

        supportTicketRepository.save(ticket);
        log.info("Support ticket created for question: '{}'", request.question());
    }
}
