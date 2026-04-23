package com.newsaggregator.application.service;

import com.newsaggregator.domain.port.in.CleanupOldArticlesUseCase;
import com.newsaggregator.domain.port.out.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service für das Bereinigen alter Artikel.
 *
 * <p>Implementiert den {@link CleanupOldArticlesUseCase} und koordiniert
 * das Löschen von Artikeln, die älter als ein konfigurierbarer Zeitraum sind.
 * Die Standardanzahl der Tage kann über die Konfiguration angepasst werden.</p>
 */
@Service
public class ArticleCleanupService implements CleanupOldArticlesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ArticleCleanupService.class);

    private final ArticleRepository articleRepository;
    private final int cleanupDays;

    /**
     * Erstellt einen neuen ArticleCleanupService.
     *
     * @param articleRepository Das Repository für Artikel
     * @param cleanupDays Die Anzahl der Tage, nach deren Ablauf Artikel gelöscht werden (Standard: 30)
     */
    public ArticleCleanupService(ArticleRepository articleRepository,
                                  @Value("${cleanup.articles.days:30}") int cleanupDays) {
        this.articleRepository = articleRepository;
        this.cleanupDays = cleanupDays;
    }

    @Override
    public int deleteArticlesOlderThan(LocalDateTime cutoffDate) {
        log.info("Deleting articles older than {}", cutoffDate);

        int deletedCount = articleRepository.deleteByPublishedAtBefore(cutoffDate);

        if (deletedCount > 0) {
            log.info("Successfully deleted {} articles older than {}", deletedCount, cutoffDate);
        } else {
            log.debug("No articles found to delete before {}", cutoffDate);
        }

        return deletedCount;
    }

    @Override
    public int cleanupOldArticles() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(cleanupDays);
        log.info("Starting scheduled cleanup of articles older than {} days (before {})", cleanupDays, cutoffDate);

        int deletedCount = deleteArticlesOlderThan(cutoffDate);

        log.info("Scheduled cleanup completed. Deleted {} articles older than {} days", deletedCount, cleanupDays);
        return deletedCount;
    }
}
