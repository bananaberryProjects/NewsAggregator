package com.newsaggregator.domain.port.in;

import java.time.LocalDateTime;

/**
 * Input Port für das Bereinigen alter Artikel.
 *
 * <p>Dieser Use Case definiert die Operation zum Löschen von Artikeln,
 * die älter als ein konfigurierbares Datum sind.</p>
 */
public interface CleanupOldArticlesUseCase {

    /**
     * Löscht alle Artikel, die älter als das angegebene Datum sind.
     *
     * @param cutoffDate Das Grenzdatum - Artikel vor diesem Datum werden gelöscht
     * @return Die Anzahl der gelöschten Artikel
     */
    int deleteArticlesOlderThan(LocalDateTime cutoffDate);

    /**
     * Führt die Bereinigung mit dem konfigurierten Standardzeitraum durch.
     * Die Anzahl der Tage wird aus der Konfiguration gelesen.
     *
     * @return Die Anzahl der gelöschten Artikel
     */
    int cleanupOldArticles();
}
