package com.newsaggregator.infrastructure.adapter.scheduler;

import com.newsaggregator.domain.port.in.CleanupOldArticlesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled Task für die automatische Bereinigung alter Artikel.
 *
 * <p>Diese Komponente führt in regelmäßigen Abständen die Bereinigung
 * von Artikeln durch, die älter als der konfigurierte Zeitraum sind.
 * Die Ausführung kann über einen Cron-Ausdruck konfiguriert werden.</p>
 */
@Component
public class ArticleCleanupScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(ArticleCleanupScheduledTask.class);

    private final CleanupOldArticlesUseCase cleanupUseCase;
    private final boolean enabled;

    /**
     * Erstellt einen neuen ArticleCleanupScheduledTask.
     *
     * @param cleanupUseCase Der Use Case für die Artikelbereinigung
     * @param enabled Gibt an, ob der Task aktiviert ist (Standard: true)
     */
    public ArticleCleanupScheduledTask(CleanupOldArticlesUseCase cleanupUseCase,
                                        @Value("${cleanup.articles.enabled:true}") boolean enabled) {
        this.cleanupUseCase = cleanupUseCase;
        this.enabled = enabled;
    }

    /**
     * Führt die Bereinigung alter Artikel durch.
     * Standardmäßig läuft dieser Task jeden Tag um 2:00 Uhr.
     * Der Cron-Ausdruck kann über die Konfiguration angepasst werden.
     */
    @Scheduled(cron = "${cleanup.articles.cron:0 0 2 * * ?}")
    public void runCleanup() {
        if (!enabled) {
            log.debug("Article cleanup is disabled");
            return;
        }

        log.info("Starting scheduled article cleanup");

        try {
            int deletedCount = cleanupUseCase.cleanupOldArticles();
            log.info("Scheduled article cleanup completed successfully. Deleted {} articles", deletedCount);
        } catch (Exception e) {
            log.error("Scheduled article cleanup failed", e);
        }
    }
}
