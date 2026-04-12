package com.newsaggregator.domain.port.in;

import com.newsaggregator.domain.model.FeedId;

/**
 * Incoming Port für das manuelle Abrufen eines Feeds.
 */
public interface FetchFeedUseCase {

    /**
     * Ruft einen Feed manuell ab und verarbeitet neue Artikel.
     *
     * @param feedId Die ID des abzurufenden Feeds
     * @throws IllegalArgumentException wenn der Feed nicht existiert
     * @throws IllegalStateException    wenn der Feed deaktiviert ist
     */
    void fetchFeed(FeedId feedId);

    /**
     * Ruft alle aktiven Feeds ab.
     * Wird typischerweise von einem Scheduler aufgerufen.
     */
    void fetchAllActiveFeeds();
}
