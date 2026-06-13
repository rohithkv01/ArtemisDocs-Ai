package com.artemisdocs.backend.service;

import com.artemisdocs.backend.dto.UploadResponse;
import com.artemisdocs.backend.entity.Document;
import com.artemisdocs.backend.exception.GlobalExceptionHandler.EntityNotFoundException;
import com.artemisdocs.backend.rag.PdfProcessor;
import com.artemisdocs.backend.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Service handling document upload, retrieval, and deletion.
 * Manages file persistence, metadata storage, and triggers the embedding pipeline.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;
    private final PdfProcessor pdfProcessor;

    @Value("${upload.directory}")
    private String uploadDirectory;

    /**
     * Uploads a PDF document: validates, saves to filesystem, extracts text,
     * generates embeddings, and persists metadata.
     *
     * @param file the uploaded multipart file
     * @return UploadResponse with document metadata
     * @throws IOException if file processing fails
     */
    public UploadResponse uploadDocument(MultipartFile file) throws IOException {
        // Validate file type
        validatePdfFile(file);

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename to avoid collisions
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(storedFilename);

        // Save file to disk
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Saved file to: {}", filePath);

        // Extract text and get page count
        PdfProcessor.ExtractionResult extraction = pdfProcessor.extractText(filePath.toString());

        // Create and save document entity
        Document document = Document.builder()
                .filename(originalFilename)
                .filePath(filePath.toString())
                .pageCount(extraction.pageCount())
                .fileSize(file.getSize())
                .build();

        document = documentRepository.save(document);
        log.info("Document saved with ID: {}", document.getId());

        // Trigger async embedding generation
        embeddingService.processDocument(document.getId(), filePath.toString());

        return new UploadResponse(
                document.getId(),
                document.getFilename(),
                document.getUploadDate(),
                document.getPageCount(),
                document.getFileSize(),
                "Document uploaded and processed successfully"
        );
    }

    /**
     * Retrieves all uploaded documents.
     *
     * @return list of all documents
     */
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    /**
     * Retrieves a single document by ID.
     *
     * @param id the document ID
     * @return the document entity
     * @throws EntityNotFoundException if not found
     */
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with ID: " + id));
    }

    /**
     * Deletes a document: removes file, embeddings, and database record.
     *
     * @param id the document ID to delete
     */
    public void deleteDocument(Long id) {
        Document document = getDocumentById(id);

        // Delete file from filesystem
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", document.getFilePath(), e);
        }

        // Remove embeddings from vector store
        embeddingService.removeDocumentEmbeddings(id);

        // Delete from database
        documentRepository.delete(document);
        log.info("Document {} deleted successfully", id);
    }

    /**
     * Validates that the uploaded file is a PDF.
     */
    private void validatePdfFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Invalid file type. Only PDF files are accepted");
        }
    }
}
