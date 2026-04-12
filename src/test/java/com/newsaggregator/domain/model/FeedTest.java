package com.newsaggregator.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die Feed Domain-Entity.
 */
class FeedTest {

    @Test
    void createNew_ShouldCreateFeed_WithCorrectValues() {
        // When
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test Description");

        // Then
        assertNotNull(feed);
        assertNull(feed.getId()); // Neue Feeds haben keine ID
        assertEquals("Test Feed", feed.getName());
        assertEquals("https://example.com/feed", feed.getUrl());
        assertEquals("Test Description", feed.getDescription());
        assertNotNull(feed.getCreatedAt());
        assertNull(feed.getLastFetched());
        assertEquals(FeedStatus.ACTIVE, feed.getStatus());
    }

    @Test
    void createNew_ShouldThrowException_WhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            Feed.createNew("", "https://example.com/feed", "Test");
        });
    }

    @Test
    void createNew_ShouldThrowException_WhenUrlIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            Feed.createNew("Test", "invalid-url", "Test");
        });
    }

    @Test
    void createNew_ShouldThrowException_WhenUrlIsNotHttp() {
        assertThrows(IllegalArgumentException.class, () -> {
            Feed.createNew("Test", "ftp://example.com/feed", "Test");
        });
    }

    @Test
    void of_ShouldCreateFeed_WithExistingId() {
        // Given
        FeedId id = FeedId.of(1L);

        // When
        Feed feed = Feed.of(id, "Test", "https://example.com/feed", "Desc",
                java.time.LocalDateTime.now(), null, FeedStatus.ACTIVE);

        // Then
        assertNotNull(feed);
        assertEquals(id, feed.getId());
    }

    @Test
    void markAsFetched_ShouldSetLastFetchedAndStatus() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");

        // When
        feed.markAsFetched();

        // Then
        assertNotNull(feed.getLastFetched());
        assertEquals(FeedStatus.ACTIVE, feed.getStatus());
    }

    @Test
    void markAsError_ShouldSetStatusToError() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");

        // When
        feed.markAsError();

        // Then
        assertEquals(FeedStatus.ERROR, feed.getStatus());
    }

    @Test
    void disable_ShouldSetStatusToDisabled() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");

        // When
        feed.disable();

        // Then
        assertEquals(FeedStatus.DISABLED, feed.getStatus());
        assertFalse(feed.canBeFetched());
    }

    @Test
    void enable_ShouldReactivateDisabledFeed() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        feed.disable();

        // When
        feed.enable();

        // Then
        assertEquals(FeedStatus.ACTIVE, feed.getStatus());
        assertTrue(feed.canBeFetched());
    }

    @Test
    void canBeFetched_ShouldReturnFalse_WhenDisabled() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        feed.disable();

        // Then
        assertFalse(feed.canBeFetched());
    }

    @Test
    void canBeFetched_ShouldReturnTrue_WhenActive() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");

        // Then
        assertTrue(feed.canBeFetched());
    }

    @Test
    void canBeFetched_ShouldReturnTrue_WhenError() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        feed.markAsError();

        // Then
        assertTrue(feed.canBeFetched());
    }

    @Test
    void addArticle_ShouldAddArticleToFeed() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        Article article = Article.createNew("Title", "Desc", "https://example.com/article", null, feed);

        // When
        feed.addArticle(article);

        // Then
        assertTrue(feed.hasArticleWithLink("https://example.com/article"));
    }

    @Test
    void equals_ShouldReturnTrue_ForSameUrl() {
        // Given
        Feed feed1 = Feed.createNew("Test1", "https://example.com/feed", "Desc1");
        Feed feed2 = Feed.createNew("Test2", "https://example.com/feed", "Desc2");

        // Then
        assertEquals(feed1, feed2);
        assertEquals(feed1.hashCode(), feed2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalse_ForDifferentUrl() {
        // Given
        Feed feed1 = Feed.createNew("Test", "https://example.com/feed1", "Desc");
        Feed feed2 = Feed.createNew("Test", "https://example.com/feed2", "Desc");

        // Then
        assertNotEquals(feed1, feed2);
    }
}
