package com.newsaggregator.application.service;

import com.newsaggregator.domain.port.out.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für den {@link ArticleCleanupService}.
 */
@ExtendWith(MockitoExtension.class)
class ArticleCleanupServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    private ArticleCleanupService service;

    @BeforeEach
    void setUp() {
        // Default: 30 days
        service = new ArticleCleanupService(articleRepository, 30);
    }

    @Test
    void deleteArticlesOlderThan_shouldCallRepositoryAndReturnCount() {
        // given
        LocalDateTime cutoffDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        int expectedDeleted = 42;
        when(articleRepository.deleteByPublishedAtBefore(cutoffDate)).thenReturn(expectedDeleted);

        // when
        int result = service.deleteArticlesOlderThan(cutoffDate);

        // then
        assertThat(result).isEqualTo(expectedDeleted);
        verify(articleRepository).deleteByPublishedAtBefore(cutoffDate);
    }

    @Test
    void deleteArticlesOlderThan_shouldReturnZeroWhenNoArticlesFound() {
        // given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        when(articleRepository.deleteByPublishedAtBefore(any())).thenReturn(0);

        // when
        int result = service.deleteArticlesOlderThan(cutoffDate);

        // then
        assertThat(result).isZero();
    }

    @Test
    void cleanupOldArticles_shouldCalculateCutoffDateFromConfiguration() {
        // given
        int daysToKeep = 30;
        int expectedDeleted = 100;
        when(articleRepository.deleteByPublishedAtBefore(any(LocalDateTime.class))).thenReturn(expectedDeleted);

        // when
        int result = service.cleanupOldArticles();

        // then
        assertThat(result).isEqualTo(expectedDeleted);
        verify(articleRepository).deleteByPublishedAtBefore(argThat(date ->
            date.isBefore(LocalDateTime.now().minusDays(daysToKeep - 1)) &&
            date.isAfter(LocalDateTime.now().minusDays(daysToKeep + 1))
        ));
    }

    @Test
    void cleanupOldArticles_shouldUseCustomConfiguration() {
        // given
        int customDays = 7;
        service = new ArticleCleanupService(articleRepository, customDays);
        when(articleRepository.deleteByPublishedAtBefore(any(LocalDateTime.class))).thenReturn(50);

        // when
        int result = service.cleanupOldArticles();

        // then
        assertThat(result).isEqualTo(50);
        verify(articleRepository).deleteByPublishedAtBefore(argThat(date ->
            date.isBefore(LocalDateTime.now().minusDays(customDays - 1)) &&
            date.isAfter(LocalDateTime.now().minusDays(customDays + 1))
        ));
    }

    @Test
    void deleteArticlesOlderThan_shouldHandleLargeDeletions() {
        // given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(365);
        when(articleRepository.deleteByPublishedAtBefore(any())).thenReturn(10000);

        // when
        int result = service.deleteArticlesOlderThan(cutoffDate);

        // then
        assertThat(result).isEqualTo(10000);
    }

    @Test
    void cleanupOldArticles_shouldHandleZeroDaysConfiguration() {
        // given
        service = new ArticleCleanupService(articleRepository, 0);
        when(articleRepository.deleteByPublishedAtBefore(any(LocalDateTime.class))).thenReturn(5);

        // when
        int result = service.cleanupOldArticles();

        // then
        assertThat(result).isEqualTo(5);
        verify(articleRepository).deleteByPublishedAtBefore(argThat(date ->
            date.isBefore(LocalDateTime.now().plusMinutes(1)) &&
            date.isAfter(LocalDateTime.now().minusMinutes(1))
        ));
    }
}
