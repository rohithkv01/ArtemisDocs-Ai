package com.artemisdocs.backend.rag;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Service for extracting text content from PDF files using Apache PDFBox.
 * Returns extracted text along with page count metadata.
 */
@Service
@Slf4j
public class PdfProcessor {

    /**
     * Result record containing extracted text and page count.
     */
    public record ExtractionResult(String text, int pageCount) {}

    /**
     * Extracts all text from a PDF file.
     *
     * @param filePath absolute path to the PDF file
     * @return ExtractionResult with full text and page count
     * @throws IOException if the file cannot be read or parsed
     */
    public ExtractionResult extractText(String filePath) throws IOException {
        log.info("Extracting text from PDF: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("PDF file not found: " + filePath);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            int pageCount = document.getNumberOfPages();

            log.info("Extracted {} characters from {} pages", text.length(), pageCount);
            return new ExtractionResult(text, pageCount);
        }
    }
}
