package com.artemisdocs.backend.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Client for interacting with Google Gemini via LangChain4j.
 * Builds structured prompts with retrieved context and generates answers.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiClient {

    private final ChatLanguageModel chatLanguageModel;

    /**
     * Generates an answer to a question using the provided document context.
     *
     * @param question the user's question
     * @param contextChunks relevant text chunks retrieved from the document
     * @return the AI-generated answer
     */
    public String generateAnswer(String question, List<String> contextChunks) {
        String context = String.join("\n\n---\n\n", contextChunks);

        String prompt = buildPrompt(question, context);
        log.info("Sending prompt to Gemini (context length: {} chars)", context.length());

        try {
            String response = chatLanguageModel.generate(prompt);
            log.info("Received response from Gemini ({} chars)", response.length());
            return response;
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "I apologize, but I encountered an error while processing your question. Please try again.";
        }
    }

    /**
     * Builds a structured RAG prompt that instructs the model to answer
     * based on the provided context, and to indicate uncertainty when applicable.
     */
    private String buildPrompt(String question, String context) {
        return """
                You are ArtemisDocs AI, an intelligent document assistant. Your task is to answer questions \
                based ONLY on the provided document context. Follow these rules strictly:

                1. Answer the question using ONLY the information found in the context below.
                2. If the context contains relevant information, provide a clear, well-structured answer.
                3. If the context does NOT contain enough information to answer the question, explicitly state: \
                   "I could not find sufficient information in the document to answer this question."
                4. Do NOT make up information or use knowledge outside of the provided context.
                5. When possible, reference the specific section or content from the document.
                6. Use bullet points or numbered lists for clarity when appropriate.
                7. Keep your answer concise but thorough.

                === DOCUMENT CONTEXT ===
                %s
                === END CONTEXT ===

                USER QUESTION: %s

                Please provide your answer:
                """.formatted(context, question);
    }
}
