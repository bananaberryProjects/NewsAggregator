package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.mapper.ArticleMapper;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Test für ArticleSearchService.
 *
 * <p>Testet das Suchen und Abrufen von Artikeln.</p>
 */
@ExtendWith(MockitoExtension.class)
class ArticleSearchServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    private ArticleMapper articleMapper;
    private ArticleSearchService service;

    @BeforeEach
    void setUp() {
        articleMapper = new ArticleMapper();
        service = new ArticleSearchService(articleRepository, articleMapper);
    }

    @Test
    void searchArticles_ShouldReturnMatchingArticles() {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");
        Article article1 = Article.createNew("Spring Boot Tutorial", "Learn Spring", "https://example.com/1", null, feed);
        Article article2 = Article.createNew("Java Guide", "Java basics", "https://example.com/2", null, feed);

        when(articleRepository.searchByQuery("Spring")).thenReturn(List.of(article1, article2));

        // When
        List<Article> result = service.searchArticles("Spring");

        // Then
        assertEquals(2, result.size());
        assertEquals("Spring Boot Tutorial", result.get(0).getTitle());
    }

    @Test
    void searchArticles_ShouldReturnEmptyList_WhenQueryIsEmpty() {
        // When
        List<Article> result = service.searchArticles("");

        // Then
        assertTrue(result.isEmpty());
        verifyNoInteractions(articleRepository);
    }

    @Test
    void searchArticles_ShouldReturnEmptyList_WhenQueryIsNull() {
        // When
        List<Article> result = service.searchArticles(null);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void searchArticlesDto_ShouldReturnDtos() {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");
        Article article = Article.createNew("Title", "Desc", "https://example.com/article", null, feed);

        when(articleRepository.searchByQuery("test")).thenReturn(List.of(article));

        // When
        List<ArticleDto> result = service.searchArticlesDto("test");

        // Then
        assertEquals(1, result.size());
        assertEquals("Title", result.get(0).getTitle());
    }

    @Test
    void getArticlesByFeed_ShouldReturnArticlesForFeed() {
        // Given
        Long feedId = 1L;
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");
        Article article = Article.createNew("Article", "Desc", "https://example.com/article", null, feed);

        when(articleRepository.findByFeedId(feedId)).thenReturn(List.of(article));

        // When
        List<Article> result = service.getArticlesByFeed(feedId);

        // Then
        assertEquals(1, result.size());
        assertEquals("Article", result.get(0).getTitle());
    }

    @Test
    void getArticlesByFeedDto_ShouldReturnDtos() {
        // Given
        Long feedId = 1L;
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");
        Article article = Article.createNew("Article", "Desc", "https://example.com/article", null, feed);

        when(articleRepository.findByFeedId(feedId)).thenReturn(List.of(article));

        // When
        List<ArticleDto> result = service.getArticlesByFeedDto(feedId);

        // Then
        assertEquals(1, result.size());
        assertEquals("Article", result.get(0).getTitle());
    }

    @Test
    void getAllArticles_ShouldReturnAllArticles() {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");
        Article article1 = Article.createNew("Article 1", "Desc 1", "https://example.com/1", null, feed);
        Article article2 = Article.createNew("Article 2", "Desc 2", "https://example.com/2", null, feed);

        when(articleRepository.findAll()).thenReturn(List.of(article1, article2));

        // When
        List<ArticleDto> result = service.getAllArticles();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void getArticleById_ShouldReturnArticle_WhenExists() {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test");
        Article article = Article.of(
                com.newsaggregator.domain.model.ArticleId.of(1L),
                "Article", "Desc", "https://example.com/article",
                java.time.LocalDateTime.now(), feed, java.time.LocalDateTime.now()
        );

        when(articleRepository.findById(com.newsaggregator.domain.model.ArticleId.of(1L)))
                .thenReturn(Optional.of(article));

        // When
        ArticleDto result = service.getArticleById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Article", result.getTitle());
    }

    @Test
    void getArticleById_ShouldThrowException_WhenNotExists() {
        // Given
        when(articleRepository.findById(com.newsaggregator.domain.model.ArticleId.of(1L)))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.getArticleById(1L);
        });
    }
}
