package com.artemisdocs.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing an uploaded PDF document.
 * Stores metadata about the file; actual PDF is on the filesystem.
 */
@Entity
@Table(name = "documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Original filename of the uploaded PDF */
    @Column(nullable = false)
    private String filename;

    /** Timestamp when the document was uploaded */
    @Column(nullable = false)
    private LocalDateTime uploadDate;

    /** Filesystem path where the PDF is stored */
    @Column(nullable = false)
    private String filePath;

    /** Number of pages in the PDF */
    private Integer pageCount;

    /** File size in bytes */
    private Long fileSize;

    @PrePersist
    protected void onCreate() {
        this.uploadDate = LocalDateTime.now();
    }
}
