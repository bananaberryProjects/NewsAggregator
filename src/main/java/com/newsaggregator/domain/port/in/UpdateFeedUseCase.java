package com.newsaggregator.domain.port.in;

import com.newsaggregator.domain.model.Feed;

/**
 * Incoming Port für das Aktualisieren eines bestehenden Feeds.
 */
public interface UpdateFeedUseCase {

    /**
     * Aktualisiert einen bestehenden Feed.
     *
     * @param id          Die ID des zu aktualisierenden Feeds
     * @param name        Der neue Name des Feeds
     * @param url         Die neue URL des Feeds
     * @param description Die neue Beschreibung des Feeds
     * @return Der aktualisierte Feed
     * @throws IllegalArgumentException wenn der Feed nicht gefunden wird
     * @throws IllegalArgumentException wenn Name oder URL ungültig sind
     * @throws IllegalArgumentException wenn ein anderer Feed mit dieser URL bereits existiert
     */
    Feed updateFeed(Long id, String name, String url, String description);
}
