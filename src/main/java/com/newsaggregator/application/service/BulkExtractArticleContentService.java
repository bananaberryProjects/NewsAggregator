package com.newsaggregator.application.service;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.port.in.BulkExtractArticleContentUseCase;
import com.newsaggregator.domain.port.out.ArticleContentExtractor;
import com.newsaggregator.domain.port.out.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Application Service für die Bulk-Extraktion von Artikel-Inhalten.
 *
 * <p>Implementiert den Use Case:
 * {@link BulkExtractArticleContentUseCase}
 * </p>
 * <p>Dieser Service ermöglicht das nachträgliche Extrahieren von HTML-Content
 * für bereits existierende Artikel, die noch keinen gespeicherten Content haben.
 * Verarbeitung erfolgt mit Pausen zwischen Requests, um Rate-Limiting zu vermeiden.</p>
 */
@Service
@Transactional
public class BulkExtractArticleContentService implements BulkExtractArticleContentUseCase {

    private static final Logger logger = LoggerFactory.getLogger(BulkExtractArticleContentService.class);

    private final ArticleRepository articleRepository;
    private final ArticleContentExtractor contentExtractor;

    public BulkExtractArticleContentService(ArticleRepository articleRepository,
                                             ArticleContentExtractor contentExtractor) {
        this.articleRepository = articleRepository;
        this.contentExtractor = contentExtractor;
    }

    @Override
    public ExtractionResult extractContentForArticlesWithoutContent(int limit, int delayMs) {
        logger.info("Starte Bulk-Content-Extraktion für max {} Artikel mit {}ms Pause", limit, delayMs);

        // Artikel ohne Content laden
        List<Article> articlesWithoutContent = articleRepository.findByContentHtmlIsNull(limit);

        if (articlesWithoutContent.isEmpty()) {
            logger.info("Keine Artikel ohne Content gefunden");
            return ExtractionResult.empty();
        }

        logger.info("{} Artikel ohne Content gefunden, beginne Extraktion", articlesWithoutContent.size());

        int successCount = 0;
        int failedCount = 0;
        List<ExtractionError> errors = new ArrayList<>();

        for (int i = 0; i < articlesWithoutContent.size(); i++) {
            Article article = articlesWithoutContent.get(i);
            int currentNumber = i + 1;

            logger.debug("Verarbeite Artikel {}/{}: '{}' (ID: {})",
                    currentNumber, articlesWithoutContent.size(),
                    article.getTitle(), article.getId());

            try {
                // Content extrahieren
                Article processedArticle = extractContentForArticle(article);

                if (processedArticle.hasExtractedContent()) {
                    successCount++;
                    logger.debug("Content erfolgreich extrahiert für '{}': {} Zeichen",
                            article.getTitle(),
                            processedArticle.getExtractedContent().length());
                } else {
                    failedCount++;
                    // Artikel als fehlgeschlagen markieren
                    Article failedArticle = article.withExtractionFailed();
                    articleRepository.save(failedArticle);
                    errors.add(new ExtractionError(
                            article.getId() != null ? article.getId().getValue() : null,
                            article.getTitle(),
                            article.getLink(),
                            "Content konnte nicht extrahiert werden (leere Antwort)"
                    ));
                    logger.warn("Content-Extraktion lieferte kein Ergebnis für '{}'", article.getLink());
                }

            } catch (Exception e) {
                failedCount++;
                // Artikel als fehlgeschlagen markieren
                try {
                    Article failedArticle = article.withExtractionFailed();
                    articleRepository.save(failedArticle);
                } catch (Exception saveEx) {
                    logger.error("Konnte Artikel nicht als fehlgeschlagen markieren: {}", saveEx.getMessage());
                }
                errors.add(new ExtractionError(
                        article.getId() != null ? article.getId().getValue() : null,
                        article.getTitle(),
                        article.getLink(),
                        e.getMessage()
                ));
                logger.error("Fehler bei Content-Extraktion für '{}': {}",
                        article.getLink(), e.getMessage());
            }

            // Pause zwischen Requests (außer beim letzten Artikel)
            if (i < articlesWithoutContent.size() - 1 && delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Bulk-Extraktion unterbrochen nach {} Artikeln", currentNumber);
                    break;
                }
            }

            // Fortschritt loggen alle 10 Artikel
            if (currentNumber % 10 == 0) {
                logger.info("Fortschritt: {}/{} Artikel verarbeitet ({} erfolgreich, {} fehlgeschlagen)",
                        currentNumber, articlesWithoutContent.size(), successCount, failedCount);
            }
        }

        logger.info("Bulk-Content-Extraktion abgeschlossen: {} verarbeitet, {} erfolgreich, {} fehlgeschlagen",
                articlesWithoutContent.size(), successCount, failedCount);

        return ExtractionResult.success(articlesWithoutContent.size(), successCount, failedCount, errors);
    }

    @Override
    @Transactional(readOnly = true)
    public long countArticlesWithoutContent() {
        return articleRepository.countByContentHtmlIsNull();
    }

    /**
     * Extrahiert Content für einen einzelnen Artikel.
     *
     * @param article Der Artikel ohne Content
     * @return Der Artikel mit extrahiertem Content (falls erfolgreich)
     */
    private Article extractContentForArticle(Article article) {
        try {
            if (!contentExtractor.canExtract(article.getLink())) {
                logger.debug("Content-Extraktion nicht möglich für URL: {}", article.getLink());
                return article;
            }

            String extractedContent = contentExtractor.extractContent(article.getLink());

            if (extractedContent != null && !extractedContent.isBlank()) {
                // Artikel mit Content speichern
                Article articleWithContent = article.withExtractedContent(extractedContent);
                return articleRepository.save(articleWithContent);
            }

        } catch (ArticleContentExtractor.ContentExtractionException e) {
            logger.warn("Content-Extraktion fehlgeschlagen für '{}': {}",
                    article.getLink(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unerwarteter Fehler bei Content-Extraktion für '{}': {}",
                    article.getLink(), e.getMessage());
        }

        // Bei Fehler: Artikel ohne Content zurückgeben
        return article;
    }
}
