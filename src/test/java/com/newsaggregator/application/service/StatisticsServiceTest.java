package com.newsaggregator.application.service;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.application.dto.ReadingStatisticsDto;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleReadStatus;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleReadStatusRepository;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleReadStatusRepository readStatusRepository;

    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService(articleRepository, readStatusRepository);
    }

    @Test
    void getStatistics_ShouldReturnCorrectCounts() {
        // Given
        Feed feed = Feed.createNew("Test Feed", "http://example.com/feed", "Description");
        
        Article article1 = Article.of(ArticleId.of(1L), "Title 1", "Desc 1", "http://example.com/1", null,
            LocalDateTime.now().minusDays(1), feed, LocalDateTime.now());
        Article article2 = Article.of(ArticleId.of(2L), "Title 2", "Desc 2", "http://example.com/2", null,
            LocalDateTime.now().minusDays(2), feed, LocalDateTime.now());
        
        ArticleReadStatus status1 = new ArticleReadStatus("1", "user-001");
        status1.setRead(true);
        status1.setFavorite(true);
        
        ArticleReadStatus status2 = new ArticleReadStatus("2", "user-001");
        status2.setRead(false);
        status2.setFavorite(false);
        
        when(articleRepository.findAll()).thenReturn(List.of(article1, article2));
        when(readStatusRepository.findByUserId("user-001")).thenReturn(List.of(status1, status2));
        
        // When
        ReadingStatisticsDto stats = statisticsService.getStatistics();
        
        // Then
        assertEquals(2, stats.getTotalArticles());
        assertEquals(1, stats.getReadArticles());
        assertEquals(1, stats.getUnreadArticles());
        assertEquals(1, stats.getFavoriteArticles());
        assertEquals(50.0, stats.getReadPercentage(), 0.01);
    }

    @Test
    void getStatistics_WithNoArticles_ShouldReturnEmptyStats() {
        // Given
        when(articleRepository.findAll()).thenReturn(List.of());
        when(readStatusRepository.findByUserId("user-001")).thenReturn(List.of());
        
        // When
        ReadingStatisticsDto stats = statisticsService.getStatistics();
        
        // Then
        assertEquals(0, stats.getTotalArticles());
        assertEquals(0, stats.getReadArticles());
        assertEquals(0, stats.getUnreadArticles());
        assertEquals(0, stats.getFavoriteArticles());
        assertEquals(0.0, stats.getReadPercentage(), 0.01);
        // 30 Tage mit 0 Werten werden zurückgegeben
        assertEquals(30, stats.getArticlesPerDay().size());
        assertTrue(stats.getArticlesPerDay().stream().allMatch(d -> d.getArticleCount() == 0 && d.getReadCount() == 0));
    }

    @Test
    void getStatistics_ShouldCalculateArticlesPerFeed() {
        // Given
        Feed feed1 = Feed.createNew("Tech News", "http://example.com/tech", "Tech");
        Feed feed2 = Feed.createNew("World News", "http://example.com/world", "World");
        
        Article article1 = Article.of(ArticleId.of(1L), "Tech Title", "Desc", "http://example.com/1", null,
            LocalDateTime.now().minusDays(1), feed1, LocalDateTime.now());
        Article article2 = Article.of(ArticleId.of(2L), "Tech Title 2", "Desc", "http://example.com/2", null,
            LocalDateTime.now().minusDays(2), feed1, LocalDateTime.now());
        Article article3 = Article.of(ArticleId.of(3L), "World Title", "Desc", "http://example.com/3", null,
            LocalDateTime.now().minusDays(3), feed2, LocalDateTime.now());
        
        when(articleRepository.findAll()).thenReturn(List.of(article1, article2, article3));
        when(readStatusRepository.findByUserId("user-001")).thenReturn(List.of());
        
        // When
        ReadingStatisticsDto stats = statisticsService.getStatistics();
        
        // Then
        assertEquals(2, stats.getArticlesPerFeed().size());
        
        // Tech News should have 2 articles
        var techFeedStats = stats.getArticlesPerFeed().stream()
            .filter(f -> f.getFeedName().equals("Tech News"))
            .findFirst()
            .orElseThrow();
        assertEquals(2, techFeedStats.getTotalArticles());
        
        // World News should have 1 article
        var worldFeedStats = stats.getArticlesPerFeed().stream()
            .filter(f -> f.getFeedName().equals("World News"))
            .findFirst()
            .orElseThrow();
        assertEquals(1, worldFeedStats.getTotalArticles());
    }
}
