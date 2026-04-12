package com.newsaggregator.infrastructure.adapter.rss;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.RssFeedReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Test für RssFeedReaderAdapter.
 *
 * <p>Hinweis: Diese Tests erfordern einen echten RSS-Feed für volle Integration.
 * Für Unit-Tests ohne Netzwerkzugriff werden die Tests mit @Disabled markiert
 * oder verwenden Mock-RSS-Daten.</p>
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
    void readFeed_ShouldThrowException_WhenUrlIsInvalid() {
        // Given - use a URL that will fail to resolve/connect
        Feed feed = Feed.createNew("Test", "https://invalid-domain-12345.com/feed.xml", "Test");

        // When / Then
        assertThrows(RssFeedReader.RssReadException.class, () -> {
            rssFeedReader.readFeed(feed);
        });
    }

    /**
     * Integrationstest mit einem echten RSS-Feed.
     * Kann aufgrund von Netzwerkzugriff fehlschlagen.
     * 
     * @Disabled("Erfordert Netzwerkzugriff")
     */
    /*
    @Test
    void readFeed_ShouldReturnArticles_FromRealRssFeed() throws RssFeedReader.RssReadException {
        // Given - Verwende einen öffentlichen RSS-Feed
        Feed feed = Feed.createNew("Heise", "https://www.heise.de/rss/heise-atom.xml", "Technik News");

        // When
        List<Article> articles = rssFeedReader.readFeed(feed);

        // Then
        assertNotNull(articles);
        assertFalse(articles.isEmpty());
        
        // Jeder Artikel sollte einen Titel und Link haben
        for (Article article : articles) {
            assertNotNull(article.getTitle());
            assertNotNull(article.getLink());
            assertTrue(article.getLink().startsWith("http"));
        }
    }
    */
}
