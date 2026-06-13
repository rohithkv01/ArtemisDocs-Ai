package com.artemisdocs.backend.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service wrapping the in-memory embedding store.
 * Handles adding, searching, and removing document chunk embeddings.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VectorStoreService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    /** Tracks embedding IDs per document for targeted removal */
    private final Map<Long, List<String>> documentEmbeddingIds = new ConcurrentHashMap<>();

    /**
     * Result record for a retrieved chunk with its similarity score.
     */
    public record RetrievalResult(String text, double score, long documentId, int chunkIndex) {}

    /**
     * Generates embeddings for text chunks and stores them in the vector store.
     *
     * @param chunks list of text chunks to embed and store
     */
    public void addChunks(List<ChunkingService.TextChunk> chunks) {
        if (chunks.isEmpty()) {
            log.warn("No chunks provided for embedding");
            return;
        }

        long documentId = chunks.get(0).documentId();
        List<String> embeddingIds = new java.util.ArrayList<>();

        for (ChunkingService.TextChunk chunk : chunks) {
            // Create a text segment with metadata
            TextSegment segment = TextSegment.from(
                    chunk.content(),
                    dev.langchain4j.data.document.Metadata.from("documentId", String.valueOf(chunk.documentId()))
                            .put("chunkIndex", String.valueOf(chunk.chunkIndex()))
            );

            // Generate embedding
            Response<Embedding> embeddingResponse = embeddingModel.embed(segment);
            Embedding embedding = embeddingResponse.content();

            // Store in vector store
            String id = embeddingStore.add(embedding, segment);
            embeddingIds.add(id);
        }

        documentEmbeddingIds.put(documentId, embeddingIds);
        log.info("Stored {} embeddings for document {}", chunks.size(), documentId);
    }

    /**
     * Searches the vector store for chunks most similar to the query.
     *
     * @param query      the search query text
     * @param maxResults maximum number of results to return
     * @return list of retrieval results ordered by descending similarity
     */
    public List<RetrievalResult> search(String query, int maxResults) {
        // Generate query embedding
        Response<Embedding> queryEmbedding = embeddingModel.embed(query);

        // Search the store
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
                queryEmbedding.content(), maxResults
        );

        return matches.stream()
                .map(match -> {
                    TextSegment segment = match.embedded();
                    long docId = Long.parseLong(
                            segment.metadata().getString("documentId")
                    );
                    int chunkIdx = Integer.parseInt(
                            segment.metadata().getString("chunkIndex")
                    );
                    return new RetrievalResult(
                            segment.text(),
                            match.score(),
                            docId,
                            chunkIdx
                    );
                })
                .toList();
    }

    /**
     * Removes all embeddings associated with a specific document.
     *
     * @param documentId the document whose embeddings should be removed
     */
    public void removeByDocumentId(long documentId) {
        List<String> ids = documentEmbeddingIds.remove(documentId);
        if (ids != null && !ids.isEmpty()) {
            ids.forEach(embeddingStore::remove);
            log.info("Removed {} embeddings for document {}", ids.size(), documentId);
        }
    }
}
