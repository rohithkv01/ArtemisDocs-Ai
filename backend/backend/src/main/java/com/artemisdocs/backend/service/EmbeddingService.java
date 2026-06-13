package com.artemisdocs.backend.service;

import com.artemisdocs.backend.entity.Document;
import com.artemisdocs.backend.rag.ChunkingService;
import com.artemisdocs.backend.rag.PdfProcessor;
import com.artemisdocs.backend.rag.VectorStoreService;
import com.artemisdocs.backend.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Service orchestrating the document embedding pipeline.
 * Handles processing documents into chunks and storing embeddings.
 * Re-indexes all documents on application startup since the vector store is in-memory.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingService {

    private final PdfProcessor pdfProcessor;
    private final ChunkingService chunkingService;
    private final VectorStoreService vectorStoreService;
    private final DocumentRepository documentRepository;

    /**
     * Processes a single document through the full embedding pipeline:
     * PDF → Text → Chunks → Embeddings → Vector Store
     *
     * @param documentId the document ID
     * @param filePath   the filesystem path to the PDF
     */
    public void processDocument(long documentId, String filePath) {
        log.info("Starting embedding pipeline for document {}", documentId);

        try {
            // Extract text from PDF
            PdfProcessor.ExtractionResult extraction = pdfProcessor.extractText(filePath);

            // Split into chunks
            List<ChunkingService.TextChunk> chunks = chunkingService.chunkText(
                    extraction.text(), documentId
            );

            // Generate embeddings and store
            vectorStoreService.addChunks(chunks);

            log.info("Embedding pipeline completed for document {}. {} chunks processed.",
                    documentId, chunks.size());

        } catch (IOException e) {
            log.error("Failed to process document {} for embeddings: {}", documentId, e.getMessage(), e);
        }
    }

    /**
     * Removes all embeddings for a document from the vector store.
     *
     * @param documentId the document ID whose embeddings to remove
     */
    public void removeDocumentEmbeddings(long documentId) {
        vectorStoreService.removeByDocumentId(documentId);
    }

    /**
     * Re-indexes all stored documents on application startup.
     * Necessary because InMemoryEmbeddingStore does not persist across restarts.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void reloadAllDocuments() {
        List<Document> documents = documentRepository.findAll();

        if (documents.isEmpty()) {
            log.info("No documents to re-index on startup");
            return;
        }

        log.info("Re-indexing {} documents on startup...", documents.size());

        for (Document doc : documents) {
            try {
                processDocument(doc.getId(), doc.getFilePath());
            } catch (Exception e) {
                log.error("Failed to re-index document {}: {}", doc.getId(), e.getMessage());
            }
        }

        log.info("Document re-indexing complete");
    }
}
