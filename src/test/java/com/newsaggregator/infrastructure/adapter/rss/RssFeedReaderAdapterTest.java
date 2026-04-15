package com.newsaggregator.infrastructure.adapter.rss;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.RssFeedReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Test für RssFeedReaderAdapter.
 *
 * <p>Testet die RSS-Reading Logik.</p>
 */
@ExtendWith(MockitoExtension.class)
class RssFeedReaderAdapterTest {

    @InjectMocks
    private RssFeedReaderAdapter rssFeedReader;

    @Test
    void readFeed_ShouldThrowException_WhenFeedIsNull() {
        assertThrows(RssFeedReader.RssReadException.class, () -> {
            rssFeedReader.readFeed(null);
        });
    }

    @Test
    void readFeed_ShouldThrowException_WhenFeedUrlNotReachable() {
        Feed feed = Feed.createNew("Test", "https://invalid-domain-12345.com/feed.xml", "Test");
        assertThrows(RssFeedReader.RssReadException.class, () -> {
            rssFeedReader.readFeed(feed);
        });
    }

    @Test
    void readFeed_ShouldReturnArticles_FromRealRssFeed() throws RssFeedReader.RssReadException {
        Feed feed = Feed.createNew("Heise", "https://www.heise.de/rss/heise-atom.xml", "Technik News");
        List<Article> articles = rssFeedReader.readFeed(feed);
        
        assertNotNull(articles);
        assertFalse(articles.isEmpty());
        
        for (Article article : articles) {
            assertNotNull(article.getTitle());
            assertNotNull(article.getLink());
            assertTrue(article.getLink().startsWith("http"));
        }
    }

    @Test
    void readFeed_ShouldExtractFeedName() throws RssFeedReader.RssReadException {
        String feedName = "Heise";
        Feed feed = Feed.createNew(feedName, "https://www.heise.de/rss/heise-atom.xml", "Technik News");
        List<Article> articles = rssFeedReader.readFeed(feed);
        
        assertNotNull(articles);
        if (!articles.isEmpty()) {
            assertNotNull(articles.get(0).getFeed());
            assertEquals(feedName, articles.get(0).getFeed().getName());
        }
    }

    @Test
    void readFeed_ShouldSetPublishedAt() throws RssFeedReader.RssReadException {
        Feed feed = Feed.createNew("Heise", "https://www.heise.de/rss/heise-atom.xml", "Technik News");
        List<Article> articles = rssFeedReader.readFeed(feed);
        
        assertNotNull(articles);
        if (!articles.isEmpty()) {
            assertNotNull(articles.get(0).getPublishedAt());
        }
    }
}
