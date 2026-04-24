package com.newsaggregator.application.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleReadStatus;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleReadStatusRepository;

/**
 * Unit-Test für ArticleReadStatusService.
 *
 * <p>Testet die Geschäftslogik für Lesestatus und Favoriten.</p>
 */
@ExtendWith(MockitoExtension.class)
class ArticleReadStatusServiceTest {

    @Mock
    private ArticleReadStatusRepository repository;

    private ArticleReadStatusService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new ArticleReadStatusService();
        // Repository via Reflection setzen (da @Autowired)
        try {
            var field = ArticleReadStatusService.class.getDeclaredField("repository");
            field.setAccessible(true);
            field.set(service, repository);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("null")
    @Test
    void markAsRead_WhenStatusExists_ShouldUpdateAndSave() {
        // Given
        Long articleId = 1L;
        ArticleReadStatus existing = new ArticleReadStatus(articleId, "user-001");
        existing.setRead(false);

        when(repository.findByArticleIdAndUserId(articleId, "user-001"))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(ArticleReadStatus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ArticleReadStatus result = service.markAsRead(articleId);

        // Then
        assertTrue(result.isRead());
        verify(repository).save(existing);
    }

    @SuppressWarnings("null")
    @Test
    void markAsRead_WhenStatusNotExists_ShouldCreateNew() {
        // Given
        Long articleId = 1L;
        when(repository.findByArticleIdAndUserId(articleId, "user-001"))
                .thenReturn(Optional.empty());
        when(repository.save(any(ArticleReadStatus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ArticleReadStatus result = service.markAsRead(articleId);

        // Then
        assertTrue(result.isRead());
        assertEquals(articleId, result.getArticleId());
        assertEquals("user-001", result.getUserId());
        verify(repository).save(any(ArticleReadStatus.class));
    }

    @SuppressWarnings("null")
    @Test
    void markAsUnread_WhenStatusExists_ShouldUpdateAndSave() {
        // Given
        Long articleId = 1L;
        ArticleReadStatus existing = new ArticleReadStatus(articleId, "user-001");
        existing.setRead(true);

        when(repository.findByArticleIdAndUserId(articleId, "user-001"))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(ArticleReadStatus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ArticleReadStatus result = service.markAsUnread(articleId);

        // Then
        assertFalse(result.isRead());
        verify(repository).save(existing);
    }

    @SuppressWarnings("null")
    @Test
    void markAsUnread_WhenStatusNotExists_ShouldReturnNull() {
        // Given
        Long articleId = 1L;
        when(repository.findByArticleIdAndUserId(articleId, "user-001"))
                .thenReturn(Optional.empty());

        // When
        ArticleReadStatus result = service.markAsUnread(articleId);

        // Then
        assertNull(result);
        verify(repository, never()).save(any());
    }

    @SuppressWarnings("null")
    @Test
    void toggleFavorite_WhenStatusExists_ShouldToggleAndSave() {
        // Given
        Long articleId = 1L;
        ArticleReadStatus existing = new ArticleReadStatus(articleId, "user-001");
        existing.setFavorite(false);

        when(repository.findByArticleIdAndUserId(articleId, "user-001"))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(ArticleReadStatus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ArticleReadStatus result = service.toggleFavorite(articleId);

        // Then
        assertTrue(result.isFavorite());
        verify(repository).save(existing);
    }

    @SuppressWarnings("null")
    @Test
    void toggleFavorite_WhenStatusNotExists_ShouldCreateNewFavorite() {
        // Given
        Long articleId = 1L;
        when(repository.findByArticleIdAndUserId(articleId, "user-001"))
                .thenReturn(Optional.empty());
        when(repository.save(any(ArticleReadStatus.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ArticleReadStatus result = service.toggleFavorite(articleId);

        // Then
        assertTrue(result.isFavorite());
        assertEquals(articleId, result.getArticleId());
        verify(repository).save(any(ArticleReadStatus.class));
    }

    @Test
    void getAllByUser_ShouldReturnAllStatuses() {
        // Given
        ArticleReadStatus status1 = new ArticleReadStatus(1L, "user-001");
        ArticleReadStatus status2 = new ArticleReadStatus(2L, "user-001");
        when(repository.findByUserId("user-001")).thenReturn(List.of(status1, status2));

        // When
        List<ArticleReadStatus> result = service.getAllByUser();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void getReadArticles_ShouldReturnOnlyRead() {
        // Given
        ArticleReadStatus status1 = new ArticleReadStatus(1L, "user-001");
        status1.setRead(true);
        when(repository.findByUserIdAndIsRead("user-001", true)).thenReturn(List.of(status1));

        // When
        List<ArticleReadStatus> result = service.getReadArticles();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isRead());
    }

    @Test
    void getFavoriteArticles_ShouldReturnOnlyFavorites() {
        // Given
        ArticleReadStatus status1 = new ArticleReadStatus(1L, "user-001");
        status1.setFavorite(true);
        when(repository.findByUserIdAndIsFavorite("user-001", true)).thenReturn(List.of(status1));

        // When
        List<ArticleReadStatus> result = service.getFavoriteArticles();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).isFavorite());
    }

    @Test
    void isRead_WhenStatusExistsAndRead_ShouldReturnTrue() {
        // Given
        ArticleReadStatus status = new ArticleReadStatus(1L, "user-001");
        status.setRead(true);
        when(repository.findByArticleIdAndUserId(1L, "user-001"))
                .thenReturn(Optional.of(status));

        // When
        boolean result = service.isRead(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void isRead_WhenStatusNotExists_ShouldReturnFalse() {
        // Given
        when(repository.findByArticleIdAndUserId(1L, "user-001"))
                .thenReturn(Optional.empty());

        // When
        boolean result = service.isRead(1L);

        // Then
        assertFalse(result);
    }

    @Test
    void isFavorite_WhenStatusExistsAndFavorite_ShouldReturnTrue() {
        // Given
        ArticleReadStatus status = new ArticleReadStatus(1L, "user-001");
        status.setFavorite(true);
        when(repository.findByArticleIdAndUserId(1L, "user-001"))
                .thenReturn(Optional.of(status));

        // When
        boolean result = service.isFavorite(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void isFavorite_WhenStatusNotExists_ShouldReturnFalse() {
        // Given
        when(repository.findByArticleIdAndUserId(1L, "user-001"))
                .thenReturn(Optional.empty());

        // When
        boolean result = service.isFavorite(1L);

        // Then
        assertFalse(result);
    }
}
