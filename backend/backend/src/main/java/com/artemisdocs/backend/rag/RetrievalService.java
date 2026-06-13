package com.artemisdocs.backend.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the retrieval step of the RAG pipeline.
 * Takes a user query, searches the vector store, and returns
 * the most relevant document chunks with similarity scores.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RetrievalService {

    private final VectorStoreService vectorStoreService;

    @Value("${rag.max-results}")
    private int maxResults;

    /**
     * Retrieves the most relevant document chunks for a given query.
     *
     * @param query the user's question
     * @return list of retrieval results with text and similarity scores
     */
    public List<VectorStoreService.RetrievalResult> retrieve(String query) {
        log.info("Retrieving context for query: '{}'", query);

        List<VectorStoreService.RetrievalResult> results = vectorStoreService.search(query, maxResults);

        if (results.isEmpty()) {
            log.warn("No relevant chunks found for query: '{}'", query);
        } else {
            log.info("Found {} relevant chunks. Top score: {}", results.size(),
                    results.get(0).score());
        }

        return results;
    }
}
