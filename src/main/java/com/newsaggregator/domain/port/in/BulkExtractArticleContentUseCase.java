package com.newsaggregator.domain.port.in;

import java.util.List;

/**
 * Incoming Port für die Bulk-Extraktion von Artikel-Inhalten.
 *
 * <p>Dieser Use Case ermöglicht das nachträgliche Extrahieren von HTML-Content
 * für bereits existierende Artikel, die noch keinen gespeicherten Content haben.</p>
 */
public interface BulkExtractArticleContentUseCase {

    /**
     * Extrahiert Content für Artikel ohne gespeicherten Content.
     *
     * @param limit Maximale Anzahl zu verarbeitender Artikel (z.B. 50)
     * @param delayMs Pause zwischen Extraktionen in Millisekunden (z.B. 2000)
     * @return Ergebnis der Extraktion mit Statistiken
     */
    ExtractionResult extractContentForArticlesWithoutContent(int limit, int delayMs);

    /**
     * Zählt die Anzahl der Artikel ohne Content.
     *
     * @return Anzahl der Artikel ohne extrahierten Content
     */
    long countArticlesWithoutContent();

    /**
     * Record für das Ergebnis der Bulk-Extraktion.
     */
    record ExtractionResult(
            int processedCount,
            int successCount,
            int failedCount,
            int skippedCount,
            List<ExtractionError> errors
    ) {
        /**
         * Erzeugt ein erfolgreiches Ergebnis.
         */
        public static ExtractionResult success(int processed, int success, int failed, List<ExtractionError> errors) {
            return new ExtractionResult(processed, success, failed, 0, errors);
        }

        /**
         * Erzeugt ein leeres Ergebnis (keine Artikel zu verarbeiten).
         */
        public static ExtractionResult empty() {
            return new ExtractionResult(0, 0, 0, 0, List.of());
        }
    }

    /**
     * Record für einen einzelnen Extraktionsfehler.
     */
    record ExtractionError(
            Long articleId,
            String articleTitle,
            String articleLink,
            String errorMessage
    ) {
    }
}
