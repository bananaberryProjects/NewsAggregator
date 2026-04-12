package com.newsaggregator.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Value Object für Article IDs.
 * Unveränderlich und typensicher.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public final class ArticleId {
    private final Long value;

    public static ArticleId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("ArticleId darf nicht null sein");
        }
        return new ArticleId(value);
    }

    public static ArticleId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ArticleId darf nicht leer sein");
        }
        return new ArticleId(Long.parseLong(value.trim()));
    }

}
