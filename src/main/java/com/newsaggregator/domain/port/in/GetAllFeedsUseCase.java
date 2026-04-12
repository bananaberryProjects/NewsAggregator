package com.newsaggregator.domain.port.in;

import com.newsaggregator.domain.model.Feed;

import java.util.List;

/**
 * Incoming Port für das Abfragen aller Feeds.
 */
public interface GetAllFeedsUseCase {

    /**
     * Gibt alle Feeds zurück.
     *
     * @return Liste aller Feeds
     */
    List<Feed> getAllFeeds();

    /**
     * Gibt nur aktive Feeds zurück.
     *
     * @return Liste aller aktiven Feeds
     */
    List<Feed> getActiveFeeds();
}
