package com.newsaggregator.domain.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FeedKeywordFilterTest {

    @Test
    void isTitleBlocked_noKeywords_returnsFalse() {
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        assertFalse(feed.isTitleBlocked("Bitcoin Preis steigt"));
    }

    @Test
    void isTitleBlocked_matchingKeyword_returnsTrue() {
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        feed.setBlockedKeywords(List.of("bitcoin", "spam"));
        assertTrue(feed.isTitleBlocked("Bitcoin Preis steigt"));
    }

    @Test
    void isTitleBlocked_nonMatchingKeyword_returnsFalse() {
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        feed.setBlockedKeywords(List.of("bitcoin"));
        assertFalse(feed.isTitleBlocked("Ethereum Preis steigt"));
    }

    @Test
    void isTitleBlocked_nullTitle_returnsFalse() {
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        feed.setBlockedKeywords(List.of("bitcoin"));
        assertFalse(feed.isTitleBlocked(null));
    }

    @Test
    void addBlockedKeyword_deduplicates() {
        Feed feed = Feed.createNew("Test", "https://example.com/feed", "Test");
        feed.addBlockedKeyword(" bitcoin ");
        feed.addBlockedKeyword("Bitcoin");
        assertEquals(1, feed.getBlockedKeywords().size());
        assertEquals("bitcoin", feed.getBlockedKeywords().get(0));
    }
}
