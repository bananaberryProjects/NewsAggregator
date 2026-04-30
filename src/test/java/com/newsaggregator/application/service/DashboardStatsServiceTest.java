package com.newsaggregator.application.service;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.application.dto.DashboardStatsDto;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.domain.port.out.FeedRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleReadStatus;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleReadStatusRepository;

@ExtendWith(MockitoExtension.class)
class DashboardStatsServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleReadStatusRepository readStatusRepository;

    @Mock
    private FeedRepository feedRepository;

    private DashboardStatsService dashboardStatsService;

    @BeforeEach
    void setUp() {
        dashboardStatsService = new DashboardStatsService(articleRepository, readStatusRepository, feedRepository);
    }

    @Test
    void getDashboardStats_ShouldReturnCorrectAggregates() {
        // Given
        Feed feed1 = Feed.of(FeedId.of(1L), "Tech News", "http://example.com/tech", "Tech",
            LocalDateTime.now().minusDays(1), null, FeedStatus.ACTIVE);
        Feed feed2 = Feed.of(FeedId.of(2L), "World News", "http://example.com/world", "World",
            LocalDateTime.now().minusDays(1), null, FeedStatus.ACTIVE);

        Article article1 = Article.of(ArticleId.of(1L), "Tech Title", "Desc", "http://example.com/1", null,
            LocalDateTime.now().minusDays(1), feed1, LocalDateTime.now());
        Article article2 = Article.of(ArticleId.of(2L), "World Title", "Desc", "http://example.com/2", null,
            LocalDateTime.now().minusDays(1), feed2, LocalDateTime.now());
        Article article3 = Article.of(ArticleId.of(3L), "Old Article", "Desc", "http://example.com/3", null,
            LocalDateTime.now().minusDays(5), feed1, LocalDateTime.now());

        ArticleReadStatus rs1 = new ArticleReadStatus(1L, "user-001");
        rs1.setRead(true);
        rs1.setReadAt(LocalDateTime.now());
        rs1.setFavorite(true);
        rs1.setFavoritedAt(LocalDateTime.now());

        ArticleReadStatus rs2 = new ArticleReadStatus(2L, "user-001");
        rs2.setRead(false);

        ArticleReadStatus rs3 = new ArticleReadStatus(3L, "user-001");
        rs3.setRead(true);
        rs3.setReadAt(LocalDateTime.now().minusDays(2));

        when(articleRepository.findAll()).thenReturn(List.of(article1, article2, article3));
        when(feedRepository.findAll()).thenReturn(List.of(feed1, feed2));
        when(readStatusRepository.findByUserId("user-001")).thenReturn(List.of(rs1, rs2, rs3));

        // When
        DashboardStatsDto stats = dashboardStatsService.getDashboardStats();

        // Then
        assertEquals(1, stats.getUnreadCount()); // article2
        assertEquals(2, stats.getTotalFeeds());
        assertEquals(1, stats.getFeedsWithNewArticles()); // feed2 hat unread article
        assertEquals(1, stats.getFavoriteCount());
        assertEquals(1, stats.getNewFavoritesToday());
        assertEquals(1, stats.getArticlesReadToday());
        assertNotNull(stats.getLastReadAt());
    }

    @Test
    void getDashboardStats_ReadStreak_CurrentDayRead() {
        // Given - 3-Tage-Streak (heute, gestern, vorgestern)
        Feed feed = Feed.createNew("F", "http://f", "F");
        Article article = Article.of(ArticleId.of(1L), "A", "D", "http://a", null,
            LocalDateTime.now(), feed, LocalDateTime.now());

        ArticleReadStatus rs1 = new ArticleReadStatus(1L, "user-001");
        rs1.setRead(true);
        rs1.setReadAt(LocalDateTime.now());

        ArticleReadStatus rs2 = new ArticleReadStatus(2L, "user-001");
        rs2.setRead(true);
        rs2.setReadAt(LocalDateTime.now().minusDays(1));

        ArticleReadStatus rs3 = new ArticleReadStatus(3L, "user-001");
        rs3.setRead(true);
        rs3.setReadAt(LocalDateTime.now().minusDays(2));

        when(articleRepository.findAll()).thenReturn(List.of(article));
        when(feedRepository.findAll()).thenReturn(List.of(feed));
        when(readStatusRepository.findByUserId("user-001")).thenReturn(List.of(rs1, rs2, rs3));

        // When
        DashboardStatsDto stats = dashboardStatsService.getDashboardStats();

        // Then
        assertEquals(3, stats.getReadStreakDays());
    }

    @Test
    void getDashboardStats_ReadStreak_TodayNotRead_ShouldReturnZero() {
        // Given - gestern gelesen, aber heute nicht
        Feed feed = Feed.createNew("F", "http://f", "F");
        Article article = Article.of(ArticleId.of(1L), "A", "D", "http://a", null,
            LocalDateTime.now(), feed, LocalDateTime.now());

        ArticleReadStatus rs = new ArticleReadStatus(1L, "user-001");
        rs.setRead(true);
        rs.setReadAt(LocalDateTime.now().minusDays(1));

        when(articleRepository.findAll()).thenReturn(List.of(article));
        when(feedRepository.findAll()).thenReturn(List.of(feed));
        when(readStatusRepository.findByUserId("user-001")).thenReturn(List.of(rs));

        // When
        DashboardStatsDto stats = dashboardStatsService.getDashboardStats();

        // Then
        assertEquals(1, stats.getReadStreakDays());
    }

    @Test
    void getDashboardStats_WithNoData_ShouldReturnZeroes() {
        // Given
        when(articleRepository.findAll()).thenReturn(List.of());
        when(feedRepository.findAll()).thenReturn(List.of());
        when(readStatusRepository.findByUserId("user-001")).thenReturn(List.of());

        // When
        DashboardStatsDto stats = dashboardStatsService.getDashboardStats();

        // Then
        assertEquals(0, stats.getUnreadCount());
        assertEquals(0, stats.getTotalFeeds());
        assertEquals(0, stats.getFeedsWithNewArticles());
        assertEquals(0, stats.getFavoriteCount());
        assertEquals(0, stats.getNewFavoritesToday());
        assertEquals(0, stats.getReadStreakDays());
        assertEquals(0, stats.getArticlesReadToday());
        assertNull(stats.getLastReadAt());
    }
}
