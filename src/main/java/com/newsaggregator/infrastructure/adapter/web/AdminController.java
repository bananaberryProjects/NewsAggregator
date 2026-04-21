package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.service.BulkExtractArticleContentService;
import com.newsaggregator.domain.port.in.BulkExtractArticleContentUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller für Admin-Operationen.
 *
 * <p>Dieser Controller bietet Endpunkte für administrative Aufgaben
 * wie Bulk-Content-Extraktion und System-Wartung.</p>
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final BulkExtractArticleContentService bulkExtractService;

    public AdminController(BulkExtractArticleContentService bulkExtractService) {
        this.bulkExtractService = bulkExtractService;
    }

    /**
     * Extrahiert Content für Artikel ohne gespeicherten Content.
     *
     * @param limit Maximale Anzahl zu verarbeitender Artikel (Default: 50)
     * @param delayMs Pause zwischen Extraktionen in ms (Default: 2000)
     * @return Ergebnis der Extraktion
     */
    @PostMapping("/articles/extract-content")
    public ResponseEntity<ExtractionResponse> extractContentForArticles(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "2000") int delayMs) {

        logger.info("Admin-Request: Bulk-Content-Extraktion für max {} Artikel mit {}ms Pause", limit, delayMs);

        // Limits validieren
        if (limit < 1 || limit > 100) {
            return ResponseEntity.badRequest()
                    .body(new ExtractionResponse(false, "Limit muss zwischen 1 und 100 liegen", null, 0, 0, 0, 0));
        }

        if (delayMs < 500 || delayMs > 10000) {
            return ResponseEntity.badRequest()
                    .body(new ExtractionResponse(false, "Delay muss zwischen 500 und 10000 ms liegen", null, 0, 0, 0, 0));
        }

        try {
            BulkExtractArticleContentUseCase.ExtractionResult result =
                    bulkExtractService.extractContentForArticlesWithoutContent(limit, delayMs);

            ExtractionResponse response = new ExtractionResponse(
                    true,
                    "Content-Extraktion abgeschlossen",
                    null,
                    result.processedCount(),
                    result.successCount(),
                    result.failedCount(),
                    result.skippedCount()
            );

            logger.info("Bulk-Content-Extraktion erfolgreich: {} verarbeitet, {} erfolgreich, {} fehlgeschlagen",
                    result.processedCount(), result.successCount(), result.failedCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Fehler bei Bulk-Content-Extraktion: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ExtractionResponse(false, "Interner Fehler: " + e.getMessage(), null, 0, 0, 0, 0));
        }
    }

    /**
     * Gibt Statistiken über Artikel ohne Content zurück.
     *
     * @return Anzahl der Artikel ohne Content
     */
    @GetMapping("/articles/without-content/count")
    public ResponseEntity<Map<String, Object>> getArticlesWithoutContentCount() {
        long count = bulkExtractService.countArticlesWithoutContent();

        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("hasArticlesWithoutContent", count > 0);

        return ResponseEntity.ok(response);
    }

    /**
     * Response-Record für Content-Extraktion.
     */
    public record ExtractionResponse(
            boolean success,
            String message,
            Object errors,
            int processedCount,
            int successCount,
            int failedCount,
            int skippedCount
    ) {
    }
}
