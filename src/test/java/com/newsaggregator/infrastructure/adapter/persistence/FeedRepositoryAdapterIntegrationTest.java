package com.newsaggregator.infrastructure.adapter.persistence;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.port.out.FeedRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.repository.FeedJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstest für FeedRepositoryAdapter.
 *
 * <p>Testet die Integration zwischen Domain-Port und JPA-Repository.
 * Verwendet eine echte H2-Datenbank.</p>
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class FeedRepositoryAdapterIntegrationTest {

    @Autowired
    private FeedRepository feedRepository; // Der Domain-Port

    @Autowired
    private FeedJpaRepository jpaRepository; // Für Setup und Cleanup

    @Test
    void save_ShouldPersistNewFeed() {
        // Given
        Feed feed = Feed.createNew("Test Feed", "https://example.com/feed", "Test Description");

        // When
        Feed saved = feedRepository.save(feed);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Test Feed", saved.getName());

        // Verify in database
        @SuppressWarnings("null")
        Optional<FeedJpaEntity> inDb = jpaRepository.findById(saved.getId().getValue());
        assertTrue(inDb.isPresent());
        assertEquals("Test Feed", inDb.get().getName());
    }

    @Test
    void findById_ShouldReturnFeed_WhenExists() {
        // Given
        Feed feed = Feed.createNew("Test", "https://example.com/feed1", "Test");
        Feed saved = feedRepository.save(feed);

        // When
        Optional<Feed> found = feedRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // When
        Optional<Feed> found = feedRepository.findById(FeedId.of(999L));

        // Then
        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllFeeds() {
        // Given
        feedRepository.save(Feed.createNew("Feed 1", "https://example.com/feed1", "Desc 1"));
        feedRepository.save(Feed.createNew("Feed 2", "https://example.com/feed2", "Desc 2"));

        // When
        List<Feed> feeds = feedRepository.findAll();

        // Then
        assertTrue(feeds.size() >= 2);
    }

    @Test
    void existsByUrl_ShouldReturnTrue_WhenFeedExists() {
        // Given
        feedRepository.save(Feed.createNew("Test", "https://example.com/existing", "Test"));

        // When
        boolean exists = feedRepository.existsByUrl("https://example.com/existing");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByUrl_ShouldReturnFalse_WhenFeedNotExists() {
        // When
        boolean exists = feedRepository.existsByUrl("https://example.com/not-existing");

        // Then
        assertFalse(exists);
    }

    @Test
    void deleteById_ShouldRemoveFeed() {
        // Given
        Feed saved = feedRepository.save(Feed.createNew("Test", "https://example.com/to-delete", "Test"));
        FeedId id = saved.getId();

        // When
        feedRepository.deleteById(id);

        // Then
        assertTrue(feedRepository.findById(id).isEmpty());
    }

    @Test
    void findByUrl_ShouldReturnFeed_WhenExists() {
        // Given
        feedRepository.save(Feed.createNew("Test", "https://example.com/by-url", "Test"));

        // When
        Optional<Feed> found = feedRepository.findByUrl("https://example.com/by-url");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getName());
    }
}
