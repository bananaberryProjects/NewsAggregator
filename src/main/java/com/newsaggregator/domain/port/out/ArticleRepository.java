package com.newsaggregator.domain.port.out;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Outgoing Port für Article-Persistenz.
 *
 * Dieses Interface definiert, was die Domain von der Persistenzschicht erwartet.
 * Die tatsächliche Implementierung liegt in der Infrastruktur.
 */
public interface ArticleRepository {

    /**
     * Speichert einen Artikel.
     *
     * @param article Der zu speichernde Artikel
     * @return Der gespeicherte Artikel (mit ID, falls neu)
     */
    Article save(Article article);

    /**
     * Speichert mehrere Artikel auf einmal.
     *
     * @param articles Die zu speichernden Artikel
     * @return Die gespeicherten Artikel
     */
    List<Article> saveAll(List<Article> articles);

    /**
     * Sucht einen Artikel anhand seiner ID.
     *
     * @param id Die Article-ID
     * @return Optional mit dem Artikel, falls gefunden
     */
    Optional<Article> findById(ArticleId id);

    /**
     * Gibt alle Artikel zurück.
     *
     * @return Liste aller Artikel
     */
    List<Article> findAll();

    /**
     * Sucht nach Artikeln anhand eines Suchbegriffs.
     *
     * @param query Der Suchbegriff
     * @return Liste der passenden Artikel
     */
    List<Article> searchByQuery(String query);

    /**
     * Gibt alle Artikel eines bestimmten Feeds zurück.
     *
     * @param feedId Die ID des Feeds
     * @return Liste der Artikel
     */
    List<Article> findByFeedId(Long feedId);

    /**
     * Findet alle Artikel nach einem bestimmten Veröffentlichungsdatum.
     *
     * @param date Das Datum, nach dem gesucht wird
     * @return Liste der Artikel
     */
    List<Article> findByPublishedAtAfter(LocalDateTime date);

    /**
     * Prüft, ob ein Artikel mit diesem Link existiert.
     *
     * @param link Der zu prüfende Link
     * @return true, falls ein Artikel mit diesem Link existiert
     */
    boolean existsByLink(String link);

    /**
     * Findet alle Artikel ohne extrahierten Content (contentHtml ist null).
     *
     * @param limit Maximale Anzahl zurückgegebener Artikel
     * @return Liste der Artikel ohne Content
     */
    List<Article> findByContentHtmlIsNull(int limit);

    /**
     * Zählt alle Artikel ohne extrahierten Content.
     *
     * @return Anzahl der Artikel ohne Content
     */
    long countByContentHtmlIsNull();

    /**
     * Löscht alle Artikel, die älter als das angegebene Datum sind.
     *
     * @param cutoffDate Das Grenzdatum - Artikel vor diesem Datum werden gelöscht
     * @return Die Anzahl der gelöschten Artikel
     */
    int deleteByPublishedAtBefore(LocalDateTime cutoffDate);
}
