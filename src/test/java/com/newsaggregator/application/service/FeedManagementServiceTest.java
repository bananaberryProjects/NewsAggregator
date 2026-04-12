package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.AddFeedCommand;
import com.newsaggregator.application.dto.FeedDto;
import com.newsaggregator.application.mapper.FeedMapper;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.FeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Test für FeedManagementService.
 *
 * <p>Verwendet Mockito, um die Repository-Abhängigkeit zu mocken.
 * Testet nur die Application-Logik, nicht die Persistenz.</p>
 */
@ExtendWith(MockitoExtension.class)
class FeedManagementServiceTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ArticleJpaRepository articleJpaRepository;

    private FeedMapper feedMapper;
    private FeedManagementService service;

    @BeforeEach
    void setUp() {
        feedMapper = new FeedMapper(articleJpaRepository);
        service = new FeedManagementService(feedRepository, feedMapper);
    }

    @Test
    void addFeed_ShouldCreateAndSaveFeed() {
        // Given
        when(feedRepository.existsByUrl("https://example.com/feed")).thenReturn(false);
        when(feedRepository.save(any(Feed.class))).thenAnswer(invocation -> {
            Feed feed = invocation.getArgument(0);
            return Feed.of(
                    com.newsaggregator.domain.model.FeedId.of(1L),
                    feed.getName(),
                    feed.getUrl(),
                    feed.getDescription(),
                    feed.getCreatedAt(),
                    feed.getLastFetched(),
                    feed.getStatus()
            );
        });

        // When
        Feed result = service.addFeed("Test Feed", "https://example.com/feed", "Description");

        // Then
        assertNotNull(result);
        assertEquals("Test Feed", result.getName());
        assertEquals("https://example.com/feed", result.getUrl());
        verify(feedRepository).save(any(Feed.class));
    }

    @Test
    void addFeed_WithCommand_ShouldCreateAndReturnDto() {
        // Given
        AddFeedCommand command = new AddFeedCommand("Test", "https://example.com/feed", "Desc");

        when(feedRepository.existsByUrl("https://example.com/feed")).thenReturn(false);
        when(feedRepository.save(any(Feed.class))).thenAnswer(invocation -> {
            Feed feed = invocation.getArgument(0);
            return Feed.of(
                    com.newsaggregator.domain.model.FeedId.of(1L),
                    feed.getName(),
                    feed.getUrl(),
                    feed.getDescription(),
                    feed.getCreatedAt(),
                    feed.getLastFetched(),
                    feed.getStatus()
            );
        });

        // When
        FeedDto result = service.addFeed(command);

        // Then
        assertNotNull(result);
        assertEquals("Test", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void addFeed_ShouldThrowException_WhenUrlExists() {
        // Given
        when(feedRepository.existsByUrl("https://example.com/feed")).thenReturn(true);

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.addFeed("Test", "https://example.com/feed", "Desc");
        });

        verify(feedRepository, never()).save(any());
    }

    @Test
    void getAllFeeds_ShouldReturnAllFeedsAsDomainModel() {
        // Given
        Feed feed1 = Feed.createNew("Feed 1", "https://example.com/feed1", "Desc 1");
        Feed feed2 = Feed.createNew("Feed 2", "https://example.com/feed2", "Desc 2");

        when(feedRepository.findAll()).thenReturn(List.of(feed1, feed2));

        // When
        List<Feed> result = service.getAllFeeds();

        // Then
        assertEquals(2, result.size());
        assertEquals("Feed 1", result.get(0).getName());
        assertEquals("Feed 2", result.get(1).getName());
    }

    @Test
    void getFeedById_ShouldReturnFeed_WhenExists() {
        // Given
        Feed feed = Feed.of(
                com.newsaggregator.domain.model.FeedId.of(1L),
                "Test",
                "https://example.com/feed",
                "Desc",
                java.time.LocalDateTime.now(),
                null,
                com.newsaggregator.domain.model.FeedStatus.ACTIVE
        );

        when(feedRepository.findById(com.newsaggregator.domain.model.FeedId.of(1L)))
                .thenReturn(Optional.of(feed));

        // When
        Feed result = service.getFeedById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Test", result.getName());
    }

    @Test
    void getFeedById_ShouldThrowException_WhenNotExists() {
        // Given
        when(feedRepository.findById(com.newsaggregator.domain.model.FeedId.of(1L)))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.getFeedById(1L);
        });
    }

    @Test
    void deleteFeed_ShouldCallRepository() {
        // When
        service.deleteFeed(1L);

        // Then
        verify(feedRepository).deleteById(com.newsaggregator.domain.model.FeedId.of(1L));
    }
}
