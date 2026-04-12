package com.newsaggregator.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für das FeedId Value Object.
 */
class FeedIdTest {

    @Test
    void of_ShouldCreateFeedId_FromLong() {
        // When
        FeedId feedId = FeedId.of(1L);

        // Then
        assertNotNull(feedId);
        assertEquals(1L, feedId.getValue());
    }

    @Test
    void of_ShouldCreateFeedId_FromString() {
        // When
        FeedId feedId = FeedId.of("123");

        // Then
        assertNotNull(feedId);
        assertEquals(123L, feedId.getValue());
    }

    @Test
    void of_ShouldThrowException_WhenValueIsNull_FromLong() {
        assertThrows(IllegalArgumentException.class, () -> {
            FeedId.of((Long) null);
        });
    }

    @Test
    void of_ShouldThrowException_WhenValueIsNull_FromString() {
        assertThrows(IllegalArgumentException.class, () -> {
            FeedId.of((String) null);
        });
    }

    @Test
    void of_ShouldThrowException_WhenValueIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            FeedId.of("");
        });
    }

    @Test
    void of_ShouldThrowException_WhenValueIsNotANumber() {
        assertThrows(NumberFormatException.class, () -> {
            FeedId.of("abc");
        });
    }

    @Test
    void equals_ShouldReturnTrue_ForSameValue() {
        // Given
        FeedId id1 = FeedId.of(1L);
        FeedId id2 = FeedId.of(1L);

        // Then
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalse_ForDifferentValue() {
        // Given
        FeedId id1 = FeedId.of(1L);
        FeedId id2 = FeedId.of(2L);

        // Then
        assertNotEquals(id1, id2);
    }

    @Test
    void toString_ShouldReturnValueAsString() {
        // Given
        FeedId id = FeedId.of(42L);

        // Then
        assertEquals("FeedId(value=42)", id.toString());
    }
}
