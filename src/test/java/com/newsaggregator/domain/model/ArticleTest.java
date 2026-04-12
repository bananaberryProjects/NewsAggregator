package com.newsaggregator.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die Article Domain-Entity.
 */
class ArticleTest {

    @Test
    void createNew_ShouldCreateArticle_WithCorrectValues() {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");
        LocalDateTime now = LocalDateTime.now();

        // When
        Article article = Article.createNew("Test Title", "Description", "https://example.com/article", now, feed);

        // Then
        assertNotNull(article);
        assertNull(article.getId()); // Neue Artikel haben keine ID
        assertEquals("Test Title", article.getTitle());
        assertEquals("Description", article.getDescription());
        assertEquals("https://example.com/article", article.getLink());
        assertEquals(now, article.getPublishedAt());
        assertNotNull(article.getCreatedAt());
        assertEquals(feed, article.getFeed());
    }

    @Test
    void createNew_ShouldThrowException_WhenTitleIsEmpty() {
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");

        assertThrows(IllegalArgumentException.class, () -> {
            Article.createNew("", "Desc", "https://example.com/article", null, feed);
        });
    }

    @Test
    void createNew_ShouldThrowException_WhenLinkIsEmpty() {
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");

        assertThrows(IllegalArgumentException.class, () -> {
            Article.createNew("Title", "Desc", "", null, feed);
        });
    }

    @Test
    void createNew_ShouldThrowException_WhenFeedIsNull() {
        assertThrows(NullPointerException.class, () -> {
            Article.createNew("Title", "Desc", "https://example.com/article", null, null);
        });
    }

    @Test
    void createNew_ShouldUseCurrentTime_WhenPublishedAtIsNull() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        LocalDateTime before = LocalDateTime.now();

        // When
        Article article = Article.createNew("Title", "Desc", "https://example.com/article", null, feed);

        // Then
        LocalDateTime after = LocalDateTime.now();
        assertNotNull(article.getPublishedAt());
        assertFalse(article.getPublishedAt().isBefore(before));
        assertFalse(article.getPublishedAt().isAfter(after));
    }

    @Test
    void getSummary_ShouldReturnDescription_WhenUnder200Chars() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        Article article = Article.createNew("Title", "Short desc", "https://example.com/article", null, feed);

        // Then
        assertEquals("Short desc", article.getSummary());
    }

    @Test
    void getSummary_ShouldTruncateDescription_WhenOver200Chars() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        String longDesc = "A".repeat(250);
        Article article = Article.createNew("Title", longDesc, "https://example.com/article", null, feed);

        // When
        String summary = article.getSummary();

        // Then
        assertEquals(203, summary.length()); // 200 + "..."
        assertTrue(summary.endsWith("..."));
    }

    @Test
    void getSummary_ShouldReturnTitle_WhenDescriptionIsEmpty() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        Article article = Article.createNew("My Title", "", "https://example.com/article", null, feed);

        // Then
        assertEquals("My Title", article.getSummary());
    }

    @Test
    void isPublishedToday_ShouldReturnTrue_WhenPublishedToday() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        Article article = Article.createNew("Title", "Desc", "https://example.com/article",
                LocalDateTime.now(), feed);

        // Then
        assertTrue(article.isPublishedToday());
    }

    @Test
    void isPublishedToday_ShouldReturnFalse_WhenPublishedYesterday() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        Article article = Article.createNew("Title", "Desc", "https://example.com/article",
                LocalDateTime.now().minusDays(1), feed);

        // Then
        assertFalse(article.isPublishedToday());
    }

    @Test
    void isPublishedAfter_ShouldReturnTrue_WhenAfterDate() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        Article article = Article.createNew("Title", "Desc", "https://example.com/article",
                LocalDateTime.now(), feed);

        // Then
        assertTrue(article.isPublishedAfter(yesterday));
    }

    @Test
    void equals_ShouldReturnTrue_ForSameLink() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        Article article1 = Article.createNew("Title1", "Desc1", "https://example.com/article", null, feed);
        Article article2 = Article.createNew("Title2", "Desc2", "https://example.com/article", null, feed);

        // Then
        assertEquals(article1, article2);
        assertEquals(article1.hashCode(), article2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalse_ForDifferentLink() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        Article article1 = Article.createNew("Title", "Desc", "https://example.com/article1", null, feed);
        Article article2 = Article.createNew("Title", "Desc", "https://example.com/article2", null, feed);

        // Then
        assertNotEquals(article1, article2);
    }
}
