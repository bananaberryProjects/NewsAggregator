package com.newsaggregator.domain.port.in;

import com.newsaggregator.domain.model.Feed;

/**
 * Incoming Port für das Hinzufügen eines neuen Feeds.
 */
public interface AddFeedUseCase {

    /**
     * Fügt einen neuen Feed hinzu.
     *
     * @param name        Der Name des Feeds
     * @param url         Die URL des Feeds
     * @param description Optionale Beschreibung
     * @return Der erstellte Feed
     * @throws IllegalArgumentException wenn ein Feed mit dieser URL bereits existiert
     * @throws IllegalArgumentException wenn Name oder URL ungültig sind
     */
    Feed addFeed(String name, String url, String description);
}
