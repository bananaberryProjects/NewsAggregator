package com.newsaggregator.application.service;

import com.newsaggregator.application.mapper.ArticleMapper;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.port.out.ArticleContentExtractor;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.domain.port.out.FeedRepository;
import com.newsaggregator.domain.port.out.RssFeedReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für die Content-Extraktion im FeedFetchingService.
 */
@ExtendWith(MockitoExtension.class)
class FeedFetchingServiceContentExtractionTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private RssFeedReader rssFeedReader;

    @Mock
    private ArticleContentExtractor contentExtractor;

    @Mock
    private ArticleMapper articleMapper;

    private FeedFetchingService service;

    @BeforeEach
    void setUp() {
        service = new FeedFetchingService(
                feedRepository,
                articleRepository,
                rssFeedReader,
                contentExtractor,
                articleMapper
        );
    }

    @Test
    void fetchFeed_ShouldExtractContent_ForNewArticle() throws Exception {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Description");
        FeedId feedId = feed.getId();

        Article article = Article.createNew(
                "Test Article",
                "Description",
                "https://example.com/article",
                LocalDateTime.now(),
                feed
        );

        String extractedContent = "<p>Full article content here</p>";

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssFeedReader.readFeed(feed)).thenReturn(List.of(article));
        when(articleRepository.existsByLink(article.getLink())).thenReturn(false);
        when(contentExtractor.canExtract(article.getLink())).thenReturn(true);
        when(contentExtractor.extractContent(article.getLink())).thenReturn(extractedContent);
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.fetchFeed(feedId);

        // Then
        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(articleCaptor.capture());

        Article savedArticle = articleCaptor.getValue();
        assertEquals(extractedContent, savedArticle.getExtractedContent());
        assertTrue(savedArticle.hasExtractedContent());
    }

    @Test
    void fetchFeed_ShouldNotExtractContent_WhenExtractorCannotExtract() throws Exception {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Description");
        FeedId feedId = feed.getId();

        Article article = Article.createNew(
                "Test Article",
                "Description",
                "https://example.com/article",
                LocalDateTime.now(),
                feed
        );

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssFeedReader.readFeed(feed)).thenReturn(List.of(article));
        when(articleRepository.existsByLink(article.getLink())).thenReturn(false);
        when(contentExtractor.canExtract(article.getLink())).thenReturn(false);
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.fetchFeed(feedId);

        // Then
        verify(contentExtractor, never()).extractContent(any());

        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(articleCaptor.capture());

        Article savedArticle = articleCaptor.getValue();
        assertFalse(savedArticle.hasExtractedContent());
    }

    @Test
    void fetchFeed_ShouldSaveArticle_WhenContentExtractionFails() throws Exception {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Description");
        FeedId feedId = feed.getId();

        Article article = Article.createNew(
                "Test Article",
                "Description",
                "https://example.com/article",
                LocalDateTime.now(),
                feed
        );

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssFeedReader.readFeed(feed)).thenReturn(List.of(article));
        when(articleRepository.existsByLink(article.getLink())).thenReturn(false);
        when(contentExtractor.canExtract(article.getLink())).thenReturn(true);
        when(contentExtractor.extractContent(article.getLink()))
                .thenThrow(new ArticleContentExtractor.ContentExtractionException(
                        article.getLink(), "Network error"));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.fetchFeed(feedId);

        // Then
        ArgumentCaptor<Article> articleCaptor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(articleCaptor.capture());

        Article savedArticle = articleCaptor.getValue();
        assertFalse(savedArticle.hasExtractedContent());
    }

    @Test
    void fetchFeed_ShouldNotExtractContent_ForExistingArticle() throws Exception {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Description");
        FeedId feedId = feed.getId();

        Article article = Article.createNew(
                "Test Article",
                "Description",
                "https://example.com/article",
                LocalDateTime.now(),
                feed
        );

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssFeedReader.readFeed(feed)).thenReturn(List.of(article));
        when(articleRepository.existsByLink(article.getLink())).thenReturn(true);

        // When
        service.fetchFeed(feedId);

        // Then
        verify(contentExtractor, never()).canExtract(any());
        verify(contentExtractor, never()).extractContent(any());
        verify(articleRepository, never()).save(any());
    }

    @Test
    void fetchFeed_ShouldExtractContent_ForMultipleNewArticles() throws Exception {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Description");
        FeedId feedId = feed.getId();

        Article article1 = Article.createNew("Article 1", "Desc", "https://example.com/1", LocalDateTime.now(), feed);
        Article article2 = Article.createNew("Article 2", "Desc", "https://example.com/2", LocalDateTime.now(), feed);

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssFeedReader.readFeed(feed)).thenReturn(List.of(article1, article2));
        when(articleRepository.existsByLink(article1.getLink())).thenReturn(false);
        when(articleRepository.existsByLink(article2.getLink())).thenReturn(false);
        when(contentExtractor.canExtract(anyString())).thenReturn(true);
        when(contentExtractor.extractContent(article1.getLink())).thenReturn("<p>Content 1</p>");
        when(contentExtractor.extractContent(article2.getLink())).thenReturn("<p>Content 2</p>");
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.fetchFeed(feedId);

        // Then
        verify(contentExtractor, times(2)).extractContent(anyString());
        verify(articleRepository, times(2)).save(any(Article.class));
    }

    @Test
    void fetchFeed_ShouldHandleEmptyArticleList() throws Exception {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Description");
        FeedId feedId = feed.getId();

        when(feedRepository.findById(feedId)).thenReturn(Optional.of(feed));
        when(rssFeedReader.readFeed(feed)).thenReturn(Collections.emptyList());

        // When
        service.fetchFeed(feedId);

        // Then
        verify(contentExtractor, never()).canExtract(any());
        verify(articleRepository, never()).save(any());
    }
}
