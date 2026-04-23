package com.newsaggregator.infrastructure.adapter.scheduler;

import com.newsaggregator.domain.port.in.CleanupOldArticlesUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit Tests für den {@link ArticleCleanupScheduledTask}.
 */
@ExtendWith(MockitoExtension.class)
class ArticleCleanupScheduledTaskTest {

    @Mock
    private CleanupOldArticlesUseCase cleanupUseCase;

    @Test
    void runCleanup_whenEnabled_shouldCallCleanupUseCase() {
        // given
        ArticleCleanupScheduledTask task = new ArticleCleanupScheduledTask(cleanupUseCase, true);
        when(cleanupUseCase.cleanupOldArticles()).thenReturn(42);

        // when
        task.runCleanup();

        // then
        verify(cleanupUseCase).cleanupOldArticles();
    }

    @Test
    void runCleanup_whenEnabled_shouldHandleZeroDeleted() {
        // given
        ArticleCleanupScheduledTask task = new ArticleCleanupScheduledTask(cleanupUseCase, true);
        when(cleanupUseCase.cleanupOldArticles()).thenReturn(0);

        // when
        task.runCleanup();

        // then
        verify(cleanupUseCase).cleanupOldArticles();
    }

    @Test
    void runCleanup_whenDisabled_shouldNotCallCleanupUseCase() {
        // given
        ArticleCleanupScheduledTask task = new ArticleCleanupScheduledTask(cleanupUseCase, false);

        // when
        task.runCleanup();

        // then
        verifyNoInteractions(cleanupUseCase);
    }

    @Test
    void runCleanup_whenExceptionThrown_shouldHandleGracefully() {
        // given
        ArticleCleanupScheduledTask task = new ArticleCleanupScheduledTask(cleanupUseCase, true);
        when(cleanupUseCase.cleanupOldArticles()).thenThrow(new RuntimeException("Database error"));

        // when - should not throw
        task.runCleanup();

        // then
        verify(cleanupUseCase).cleanupOldArticles();
    }

    @Test
    void constructor_shouldAcceptEnabledTrue() {
        // given & when
        ArticleCleanupScheduledTask task = new ArticleCleanupScheduledTask(cleanupUseCase, true);

        // then - no exception
        task.runCleanup();
    }

    @Test
    void constructor_shouldAcceptEnabledFalse() {
        // given & when
        ArticleCleanupScheduledTask task = new ArticleCleanupScheduledTask(cleanupUseCase, false);

        // then - no exception
        task.runCleanup();
    }
}
