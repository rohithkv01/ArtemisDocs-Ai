package com.artemisdocs.backend.controller;

import com.artemisdocs.backend.dto.UploadResponse;
import com.artemisdocs.backend.entity.Document;
import com.artemisdocs.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST controller for document management operations.
 * Handles PDF upload, listing, and deletion.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "http://localhost:*")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Uploads a PDF document.
     * POST /api/documents/upload
     *
     * @param file the PDF file to upload
     * @return UploadResponse with document metadata
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadDocument(@RequestParam("file") MultipartFile file)
            throws IOException {
        log.info("Upload request received: {}", file.getOriginalFilename());
        UploadResponse response = documentService.uploadDocument(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all uploaded documents.
     * GET /api/documents
     *
     * @return list of all documents
     */
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Retrieves a single document by ID.
     * GET /api/documents/{id}
     *
     * @param id the document ID
     * @return the document
     */
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    /**
     * Deletes a document and its associated data.
     * DELETE /api/documents/{id}
     *
     * @param id the document ID to delete
     * @return success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
    }
}
