package com.newsaggregator.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Value Object für Feed IDs.
 * Unveränderlich und typensicher.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public final class FeedId {
    private final Long value;

    public static FeedId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("FeedId darf nicht null sein");
        }
        return new FeedId(value);
    }

    public static FeedId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("FeedId darf nicht leer sein");
        }
        return new FeedId(Long.parseLong(value.trim()));
    }

}
