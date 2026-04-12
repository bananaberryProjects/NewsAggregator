package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.mapper.ArticleMapper;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.domain.port.out.FeedRepository;
import com.newsaggregator.domain.port.out.RssFeedReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Test für FeedFetchingService.
 *
 * <p>Testet das Abrufen von Feeds und das Speichern neuer Artikel.</p>
 */
@ExtendWith(MockitoExtension.class)
class FeedFetchingServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private RssFeedReader rssFeedReader;

    private ArticleMapper articleMapper;
    private FeedFetchingService service;

    @BeforeEach
    void setUp() {
        articleMapper = new ArticleMapper();
        service = new FeedFetchingService(feedRepository, articleRepository, rssFeedReader, articleMapper);
    }

    @Test
    void fetchFeed_ShouldSaveNewArticles() throws RssFeedReader.RssReadException {
        // Given
        FeedId feedId = FeedId.of(1L);
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");

        Article article1 = Article.createNew("Article 1", "Desc 1", "https://example.com/article1", null, feed);
        Article article2 = Article.createNew("Article 2", "Desc 2", "https://example.com/article2", null, feed);

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssFeedReader.readFeed(feed)).thenReturn(List.of(article1, article2));
        when(articleRepository.existsByLink("https://example.com/article1")).thenReturn(false);
        when(articleRepository.existsByLink("https://example.com/article2")).thenReturn(false);
        when(feedRepository.save(any(Feed.class))).thenReturn(feed);

        // When
        service.fetchFeed(feedId);

        // Then
        verify(articleRepository).save(article1);
        verify(articleRepository).save(article2);
        verify(feedRepository, times(1)).save(any(Feed.class));
    }

    @Test
    void fetchFeed_ShouldSkipDuplicateArticles() throws RssFeedReader.RssReadException {
        // Given
        FeedId feedId = FeedId.of(1L);
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");

        Article article1 = Article.createNew("Article 1", "Desc 1", "https://example.com/article1", null, feed);
        Article article2 = Article.createNew("Article 2", "Desc 2", "https://example.com/article2", null, feed);

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssFeedReader.readFeed(feed)).thenReturn(List.of(article1, article2));
        when(articleRepository.existsByLink("https://example.com/article1")).thenReturn(true); // Bereits vorhanden
        when(articleRepository.existsByLink("https://example.com/article2")).thenReturn(false);
        when(feedRepository.save(any(Feed.class))).thenReturn(feed);

        // When
        service.fetchFeed(feedId);

        // Then
        verify(articleRepository, never()).save(article1);
        verify(articleRepository).save(article2);
    }

    @Test
    void fetchFeed_ShouldThrowException_WhenFeedNotFound() {
        // Given
        FeedId feedId = FeedId.of(1L);
        when(feedRepository.findById(feedId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.fetchFeed(feedId);
        });
    }

    @Test
    void fetchFeed_ShouldThrowException_WhenFeedDisabled() {
        // Given
        FeedId feedId = FeedId.of(1L);
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");
        feed.disable();

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));

        // When / Then
        assertThrows(IllegalStateException.class, () -> {
            service.fetchFeed(feedId);
        });
    }

    @Test
    void fetchFeed_ShouldMarkFeedAsError_WhenRssReadFails() throws RssFeedReader.RssReadException {
        // Given
        FeedId feedId = FeedId.of(1L);
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssFeedReader.readFeed(feed)).thenThrow(new RssFeedReader.RssReadException("Connection failed"));
        when(feedRepository.save(any(Feed.class))).thenReturn(feed);

        // When / Then
        assertThrows(RuntimeException.class, () -> {
            service.fetchFeed(feedId);
        });

        verify(feedRepository).save(argThat(f -> f.getStatus() == com.newsaggregator.domain.model.FeedStatus.ERROR));
    }

    @Test
    void fetchFeed_WithLongId_ShouldReturnArticles() throws RssFeedReader.RssReadException {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");
        Feed savedFeed = Feed.of(FeedId.of(1L), "Test Feed", "https://example.com/feed", "Test",
                java.time.LocalDateTime.now(), null, com.newsaggregator.domain.model.FeedStatus.ACTIVE);

        Article article = Article.createNew("Article", "Desc", "https://example.com/article", null, feed);

        when(feedRepository.findById(FeedId.of(1L))).thenReturn(Optional.of(savedFeed));
        when(rssFeedReader.readFeed(savedFeed)).thenReturn(List.of(article));
        when(articleRepository.existsByLink("https://example.com/article")).thenReturn(false);
        when(feedRepository.save(any(Feed.class))).thenReturn(savedFeed);
        when(articleRepository.findByFeedId(1L)).thenReturn(List.of(article));

        // When
        List<ArticleDto> result = service.fetchFeed(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals("Article", result.get(0).getTitle());
    }

    @Test
    void fetchAllActiveFeeds_ShouldFetchAllActiveFeeds() throws RssFeedReader.RssReadException {
        // Given
        Feed feed1 = Feed.createNew("Feed 1", "https://example.com/feed1", "Test");
        Feed feed2 = Feed.createNew("Feed 2", "https://example.com/feed2", "Test");
        feed2.disable(); // Dieser sollte übersprungen werden

        Feed savedFeed1 = Feed.of(FeedId.of(1L), "Feed 1", "https://example.com/feed1", "Test",
                java.time.LocalDateTime.now(), null, com.newsaggregator.domain.model.FeedStatus.ACTIVE);

        when(feedRepository.findAll()).thenReturn(List.of(feed1, feed2));
        when(feedRepository.findById(any())).thenReturn(Optional.of(savedFeed1));
        when(rssFeedReader.readFeed(any())).thenReturn(List.of());
        when(feedRepository.save(any())).thenReturn(savedFeed1);

        // When
        service.fetchAllActiveFeeds();

        // Then
        verify(rssFeedReader).readFeed(any()); // Nur feed1 wird abgerufen
    }
}
