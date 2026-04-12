package com.newsaggregator.domain.model;

/**
 * Enum für den Status eines Feeds.
 */
public enum FeedStatus {
    ACTIVE("Aktiv"),
    ERROR("Fehler"),
    DISABLED("Deaktiviert");

    private final String displayName;

    FeedStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
