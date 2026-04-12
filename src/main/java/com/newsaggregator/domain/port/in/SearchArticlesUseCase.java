package com.newsaggregator.domain.port.in;

import com.newsaggregator.domain.model.Article;

import java.util.List;

/**
 * Incoming Port für die Artikel-Suche.
 */
public interface SearchArticlesUseCase {

    /**
     * Sucht nach Artikeln anhand eines Suchbegriffs.
     * Sucht in Titel und Beschreibung.
     *
     * @param query Der Suchbegriff
     * @return Liste der passenden Artikel
     */
    List<Article> searchArticles(String query);

    /**
     * Gibt alle Artikel eines bestimmten Feeds zurück.
     *
     * @param feedId Die ID des Feeds
     * @return Liste der Artikel dieses Feeds
     */
    List<Article> getArticlesByFeed(Long feedId);
}
