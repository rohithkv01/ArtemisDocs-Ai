package com.artemisdocs.backend.ai;

import com.artemisdocs.backend.rag.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Evaluates the confidence of an AI-generated answer based on multiple signals:
 * - Similarity scores from vector retrieval
 * - Number of relevant chunks found
 * - Presence of uncertainty phrases in the AI response
 *
 * Returns a composite confidence score between 0.0 and 1.0.
 */
@Service
@Slf4j
public class ConfidenceEvaluator {

    @Value("${rag.confidence-threshold}")
    private double confidenceThreshold;

    /** Phrases that indicate the AI is uncertain about its answer */
    private static final List<String> UNCERTAINTY_PHRASES = List.of(
            "i could not find",
            "not enough information",
            "i'm not sure",
            "i am not sure",
            "the document does not",
            "the document doesn't",
            "no relevant information",
            "cannot determine",
            "unable to find",
            "not mentioned in",
            "not specified in",
            "no information available",
            "insufficient information",
            "beyond the scope"
    );

    /**
     * Result record containing the confidence score and whether escalation is needed.
     */
    public record ConfidenceResult(double score, boolean shouldEscalate) {}

    /**
     * Evaluates the confidence of an AI response.
     *
     * @param answer          the AI-generated answer
     * @param retrievalResults the chunks retrieved from the vector store
     * @return ConfidenceResult with score and escalation flag
     */
    public ConfidenceResult evaluate(String answer, List<VectorStoreService.RetrievalResult> retrievalResults) {
        // Factor 1: Retrieval quality (40% weight)
        double retrievalScore = evaluateRetrievalQuality(retrievalResults);

        // Factor 2: Answer content analysis (40% weight)
        double contentScore = evaluateAnswerContent(answer);

        // Factor 3: Context coverage (20% weight)
        double coverageScore = evaluateCoverage(retrievalResults);

        // Weighted composite score
        double compositeScore = (retrievalScore * 0.4) + (contentScore * 0.4) + (coverageScore * 0.2);

        // Round to 4 decimal places
        compositeScore = Math.round(compositeScore * 10000.0) / 10000.0;

        boolean shouldEscalate = compositeScore < confidenceThreshold;

        log.info("Confidence evaluation: retrieval={}, content={}, coverage={}, composite={}, escalate={}",
                String.format("%.3f", retrievalScore),
                String.format("%.3f", contentScore),
                String.format("%.3f", coverageScore),
                String.format("%.4f", compositeScore),
                shouldEscalate);

        return new ConfidenceResult(compositeScore, shouldEscalate);
    }

    /**
     * Evaluates retrieval quality based on the best similarity score.
     */
    private double evaluateRetrievalQuality(List<VectorStoreService.RetrievalResult> results) {
        if (results.isEmpty()) {
            return 0.0;
        }
        // Use the highest similarity score as the retrieval quality indicator
        return results.stream()
                .mapToDouble(VectorStoreService.RetrievalResult::score)
                .max()
                .orElse(0.0);
    }

    /**
     * Evaluates answer content for uncertainty signals.
     * Returns 1.0 for confident answers, lower for uncertain ones.
     */
    private double evaluateAnswerContent(String answer) {
        if (answer == null || answer.isBlank()) {
            return 0.0;
        }

        String lowerAnswer = answer.toLowerCase();

        // Check for uncertainty phrases
        long uncertaintyCount = UNCERTAINTY_PHRASES.stream()
                .filter(lowerAnswer::contains)
                .count();

        if (uncertaintyCount >= 2) {
            return 0.2; // Very uncertain
        } else if (uncertaintyCount == 1) {
            return 0.5; // Somewhat uncertain
        }

        // Higher score for longer, substantive answers
        if (answer.length() > 200) {
            return 1.0;
        } else if (answer.length() > 100) {
            return 0.85;
        } else {
            return 0.7;
        }
    }

    /**
     * Evaluates context coverage based on the number of relevant chunks found.
     */
    private double evaluateCoverage(List<VectorStoreService.RetrievalResult> results) {
        if (results.isEmpty()) {
            return 0.0;
        }

        // More relevant chunks = better coverage
        int relevantChunks = (int) results.stream()
                .filter(r -> r.score() > 0.5)
                .count();

        return Math.min(1.0, relevantChunks / 3.0);
    }

    /**
     * Returns the configured confidence threshold.
     */
    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }
}
