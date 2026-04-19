package com.newsaggregator.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.ArticleRepository;

/**
 * Unit-Test für SummaryService.
 *
 * <p>Testet die Zusammenfassungsgenerierung.
 * Da Ollama API-Aufrufe schwer zu mocken sind, fokussieren wir uns auf
 * die Repository-Interaktion und Fallback-Verhalten.</p>
 */
@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    RestTemplate restTemplate;

    private SummaryService summaryService;

    @BeforeEach
    void setUp() {
        // Use the default constructor which creates its own RestTemplate
        // The Ollama calls will fail in tests, triggering the fallback
        summaryService = new SummaryService(articleRepository);
    }

    @Test
    void generateSummary_WithNoArticles_ShouldReturnNoArticlesMessage() {
        // Given
        when(articleRepository.findByPublishedAtAfter(LocalDate.now().atStartOfDay()))
            .thenReturn(List.of());

        // When
        String summary = summaryService.generateSummary();

        // Then
        assertThat(summary).isEqualTo("Keine neuen Artikel fuer heute verfuegbar.");
    }

    @Test
    void generateSummary_WithSingleArticle_ShouldReturnFallbackSummary() {
        // Given
        Feed feed = Feed.createNew("Tech Feed", "http://example.com/feed", "Tech");
        Article article = Article.of(ArticleId.of(1L), "AI News",
            "AI update", "http://example.com/1", null,
            LocalDateTime.now().minusHours(1), feed, LocalDateTime.now());

        when(articleRepository.findByPublishedAtAfter(LocalDate.now().atStartOfDay()))
            .thenReturn(List.of(article));

        // When - Ollama call will fail (no server), triggering fallback
        String summary = summaryService.generateSummary();

        // Then - Verify fallback format
        assertThat(summary).contains("Heute gibt es 1 neue Artikel");
        assertThat(summary).contains("AI News");
    }

    @Test
    void generateSummary_WithMultipleArticles_ShouldReturnFallbackSummaryWithAllTitles() {
        // Given
        Feed feed = Feed.createNew("Tech Feed", "http://example.com/feed", "Tech");
        Article article1 = Article.of(ArticleId.of(1L), "Tech News 1",
            "Description 1", "http://example.com/1", null,
            LocalDateTime.now().minusHours(2), feed, LocalDateTime.now());
        Article article2 = Article.of(ArticleId.of(2L), "Tech News 2",
            "Description 2", "http://example.com/2", null,
            LocalDateTime.now().minusHours(1), feed, LocalDateTime.now());

        when(articleRepository.findByPublishedAtAfter(LocalDate.now().atStartOfDay()))
            .thenReturn(List.of(article1, article2));

        // When
        String summary = summaryService.generateSummary();

        // Then
        assertThat(summary).contains("Heute gibt es 2 neue Artikel");
        assertThat(summary).contains("Tech News 1");
        assertThat(summary).contains("Tech News 2");
    }

    @Test
    void generateSummary_WithManyArticles_ShouldLimitToThreeInFallback() {
        // Given
        Feed feed = Feed.createNew("Tech Feed", "http://example.com/feed", "Tech");
        List<Article> articles = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            articles.add(Article.of(ArticleId.of((long) i), "Article " + i,
                "Description " + i, "http://example.com/" + i, null,
                LocalDateTime.now().minusHours(i), feed, LocalDateTime.now()));
        }

        when(articleRepository.findByPublishedAtAfter(LocalDate.now().atStartOfDay()))
            .thenReturn(articles);

        // When
        String summary = summaryService.generateSummary();

        // Then - Fallback limits to 3 articles
        assertThat(summary).contains("Heute gibt es 5 neue Artikel");
        assertThat(summary).contains("Article 1");
        assertThat(summary).contains("Article 2");
        assertThat(summary).contains("Article 3");
        // Article 4 and 5 should not be in the summary (only first 3)
    }

    @Test
    void generateSummary_WithArticleHavingNullDescription_ShouldHandleGracefully() {
        // Given
        Feed feed = Feed.createNew("Tech Feed", "http://example.com/feed", "Tech");
        Article article = Article.of(ArticleId.of(1L), "News Without Description",
            null, "http://example.com/1", null,
            LocalDateTime.now().minusHours(1), feed, LocalDateTime.now());

        when(articleRepository.findByPublishedAtAfter(LocalDate.now().atStartOfDay()))
            .thenReturn(List.of(article));

        // When
        String summary = summaryService.generateSummary();

        // Then - Should not throw exception, use fallback
        assertThat(summary).contains("Heute gibt es 1 neue Artikel");
        assertThat(summary).contains("News Without Description");
    }

    @Test
    void generateSummary_WithArticleHavingLongDescription_ShouldNotCrash() {
        // Given
        Feed feed = Feed.createNew("Tech Feed", "http://example.com/feed", "Tech");
        String longDescription = "A".repeat(500);
        Article article = Article.of(ArticleId.of(1L), "Long Description Article",
            longDescription, "http://example.com/1", null,
            LocalDateTime.now().minusHours(1), feed, LocalDateTime.now());

        when(articleRepository.findByPublishedAtAfter(LocalDate.now().atStartOfDay()))
            .thenReturn(List.of(article));

        // When
        String summary = summaryService.generateSummary();

        // Then - Should truncate description and not crash
        assertThat(summary).contains("Heute gibt es 1 neue Artikel");
        assertThat(summary).contains("Long Description Article");
    }

    @Test
    void generateSummary_WithMultipleFeeds_ShouldIncludeArticlesFromAllFeeds() {
        // Given
        Feed feed1 = Feed.createNew("Tech Feed", "http://example.com/tech", "Tech");
        Feed feed2 = Feed.createNew("World News", "http://example.com/world", "World");

        Article article1 = Article.of(ArticleId.of(1L), "Tech Update",
            "Tech news", "http://example.com/1", null,
            LocalDateTime.now().minusHours(2), feed1, LocalDateTime.now());
        Article article2 = Article.of(ArticleId.of(2L), "World Update",
            "World news", "http://example.com/2", null,
            LocalDateTime.now().minusHours(1), feed2, LocalDateTime.now());

        when(articleRepository.findByPublishedAtAfter(LocalDate.now().atStartOfDay()))
            .thenReturn(List.of(article1, article2));

        // When
        String summary = summaryService.generateSummary();

        // Then
        assertThat(summary).contains("Heute gibt es 2 neue Artikel");
        assertThat(summary).contains("Tech Update");
        assertThat(summary).contains("World Update");
    }

    @Test
    void generateSummary_WithOldArticles_ShouldOnlyIncludeTodayArticles() {
        // Given
        Feed feed = Feed.createNew("Tech Feed", "http://example.com/feed", "Tech");
        Article todayArticle = Article.of(ArticleId.of(1L), "Today News",
            "Today's news", "http://example.com/1", null,
            LocalDateTime.now(), feed, LocalDateTime.now());

        when(articleRepository.findByPublishedAtAfter(LocalDate.now().atStartOfDay()))
            .thenReturn(List.of(todayArticle));

        // When
        String summary = summaryService.generateSummary();

        // Then
        assertThat(summary).contains("Today News");
    }
}
