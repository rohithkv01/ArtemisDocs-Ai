package com.artemisdocs.backend.repository;

import com.artemisdocs.backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Document entities.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}
