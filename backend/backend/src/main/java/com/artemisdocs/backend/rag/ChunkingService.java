package com.artemisdocs.backend.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for splitting extracted PDF text into overlapping chunks.
 * Overlapping ensures that context at chunk boundaries is not lost during retrieval.
 */
@Service
@Slf4j
public class ChunkingService {

    @Value("${rag.chunk-size}")
    private int chunkSize;

    @Value("${rag.chunk-overlap}")
    private int chunkOverlap;

    /**
     * Record representing a text chunk with its position metadata.
     */
    public record TextChunk(String content, int chunkIndex, long documentId) {}

    /**
     * Splits text into overlapping word-based chunks.
     *
     * @param text       the full extracted text
     * @param documentId the ID of the source document
     * @return list of text chunks with metadata
     */
    public List<TextChunk> chunkText(String text, long documentId) {
        if (text == null || text.isBlank()) {
            log.warn("Empty text provided for chunking, document ID: {}", documentId);
            return List.of();
        }

        List<TextChunk> chunks = new ArrayList<>();
        // Split on whitespace to get words
        String[] words = text.split("\\s+");

        if (words.length <= chunkSize) {
            // Text is small enough to be a single chunk
            chunks.add(new TextChunk(text.trim(), 0, documentId));
            log.info("Document {} is a single chunk ({} words)", documentId, words.length);
            return chunks;
        }

        int start = 0;
        int chunkIndex = 0;

        while (start < words.length) {
            int end = Math.min(start + chunkSize, words.length);

            // Build chunk text from word array
            StringBuilder chunkBuilder = new StringBuilder();
            for (int i = start; i < end; i++) {
                if (i > start) chunkBuilder.append(" ");
                chunkBuilder.append(words[i]);
            }

            chunks.add(new TextChunk(chunkBuilder.toString(), chunkIndex, documentId));
            chunkIndex++;

            // Move start forward by (chunkSize - overlap), ensuring progress
            start += Math.max(1, chunkSize - chunkOverlap);
        }

        log.info("Document {} split into {} chunks (size={}, overlap={})",
                documentId, chunks.size(), chunkSize, chunkOverlap);
        return chunks;
    }
}
