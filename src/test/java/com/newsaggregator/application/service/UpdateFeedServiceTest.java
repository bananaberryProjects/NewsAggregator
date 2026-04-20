package com.newsaggregator.application.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.domain.port.out.FeedRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-Test für UpdateFeedService.
 *
 * <p>Testet die Geschäftslogik für das Aktualisieren von Feeds.</p>
 */
@ExtendWith(MockitoExtension.class)
class UpdateFeedServiceTest {

    @Mock
    private FeedRepository feedRepository;

    private UpdateFeedService service;

    private static final Long FEED_ID = 1L;
    private static final Long OTHER_FEED_ID = 2L;

    @BeforeEach
    void setUp() {
        service = new UpdateFeedService(feedRepository);
    }

    private Feed createExistingFeed(Long id, String name, String url) {
        return Feed.of(
                FeedId.of(id),
                name,
                url,
                "Description",
                LocalDateTime.now(),
                null,
                FeedStatus.ACTIVE
        );
    }

    @Test
    void updateFeed_ShouldUpdateAndSave() {
        // Given
        Feed existingFeed = createExistingFeed(FEED_ID, "Tech News", "https://example.com/feed");

        when(feedRepository.findById(FeedId.of(FEED_ID)))
                .thenReturn(Optional.of(existingFeed));
        when(feedRepository.findByUrl("https://newurl.com/feed"))
                .thenReturn(Optional.empty());
        when(feedRepository.save(any(Feed.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Feed result = service.updateFeed(FEED_ID, "New Name", "https://newurl.com/feed", "New Description");

        // Then
        assertThat(result)
                .isNotNull()
                .extracting(Feed::getName, Feed::getUrl, Feed::getDescription)
                .containsExactly("New Name", "https://newurl.com/feed", "New Description");
        verify(feedRepository).save(any(Feed.class));
    }

    @Test
    void updateFeed_ShouldTrimNameAndUrl() {
        // Given
        Feed existingFeed = createExistingFeed(FEED_ID, "Tech News", "https://example.com/feed");

        when(feedRepository.findById(FeedId.of(FEED_ID)))
                .thenReturn(Optional.of(existingFeed));
        when(feedRepository.findByUrl("https://newurl.com/feed"))
                .thenReturn(Optional.empty());
        when(feedRepository.save(any(Feed.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Feed result = service.updateFeed(FEED_ID, "  New Name  ", "  https://newurl.com/feed  ", "Description");

        // Then
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getUrl()).isEqualTo("https://newurl.com/feed");
    }

    @Test
    void updateFeed_WithNullName_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> service.updateFeed(FEED_ID, null, "https://example.com", "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Feed-Name darf nicht leer sein");
    }

    @Test
    void updateFeed_WithEmptyName_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> service.updateFeed(FEED_ID, "   ", "https://example.com", "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Feed-Name darf nicht leer sein");
    }

    @Test
    void updateFeed_WithNullUrl_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> service.updateFeed(FEED_ID, "Name", null, "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Feed-URL darf nicht leer sein");
    }

    @Test
    void updateFeed_WithEmptyUrl_ShouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> service.updateFeed(FEED_ID, "Name", "   ", "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Feed-URL darf nicht leer sein");
    }

    @Test
    void updateFeed_WithNonExistingId_ShouldThrowException() {
        // Given
        when(feedRepository.findById(FeedId.of(FEED_ID)))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.updateFeed(FEED_ID, "Name", "https://example.com", "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Feed mit ID " + FEED_ID + " nicht gefunden");
    }

    @Test
    void updateFeed_WithDuplicateUrl_ShouldThrowException() {
        // Given
        Feed existingFeed = createExistingFeed(FEED_ID, "Tech News", "https://example.com/feed");
        Feed otherFeed = createExistingFeed(OTHER_FEED_ID, "Other News", "https://other.com/feed");

        when(feedRepository.findById(FeedId.of(FEED_ID)))
                .thenReturn(Optional.of(existingFeed));
        when(feedRepository.findByUrl("https://other.com/feed"))
                .thenReturn(Optional.of(otherFeed));

        // When/Then
        assertThatThrownBy(() -> service.updateFeed(FEED_ID, "Name", "https://other.com/feed", "Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ein Feed mit dieser URL existiert bereits");
    }

    @Test
    void updateFeed_WithSameUrl_ShouldAllowUpdate() {
        // Given - URL gehört zum gleichen Feed (Update ohne Änderung der URL)
        Feed existingFeed = createExistingFeed(FEED_ID, "Tech News", "https://example.com/feed");

        when(feedRepository.findById(FeedId.of(FEED_ID)))
                .thenReturn(Optional.of(existingFeed));
        when(feedRepository.findByUrl("https://example.com/feed"))
                .thenReturn(Optional.of(existingFeed));
        when(feedRepository.save(any(Feed.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Feed result = service.updateFeed(FEED_ID, "Updated Name", "https://example.com/feed", "Updated Description");

        // Then
        assertThat(result.getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateFeed_WithNullDescription_ShouldSetNull() {
        // Given
        Feed existingFeed = createExistingFeed(FEED_ID, "Tech News", "https://example.com/feed");

        when(feedRepository.findById(FeedId.of(FEED_ID)))
                .thenReturn(Optional.of(existingFeed));
        when(feedRepository.findByUrl("https://newurl.com/feed"))
                .thenReturn(Optional.empty());
        when(feedRepository.save(any(Feed.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Feed result = service.updateFeed(FEED_ID, "New Name", "https://newurl.com/feed", null);

        // Then
        assertThat(result.getDescription()).isNull();
    }

    @Test
    void updateFeed_WithEmptyDescription_ShouldTrimToEmpty() {
        // Given
        Feed existingFeed = createExistingFeed(FEED_ID, "Tech News", "https://example.com/feed");

        when(feedRepository.findById(FeedId.of(FEED_ID)))
                .thenReturn(Optional.of(existingFeed));
        when(feedRepository.findByUrl("https://newurl.com/feed"))
                .thenReturn(Optional.empty());
        when(feedRepository.save(any(Feed.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Feed result = service.updateFeed(FEED_ID, "New Name", "https://newurl.com/feed", "   ");

        // Then - empty after trim
        assertThat(result.getDescription()).isEmpty();
    }
}
