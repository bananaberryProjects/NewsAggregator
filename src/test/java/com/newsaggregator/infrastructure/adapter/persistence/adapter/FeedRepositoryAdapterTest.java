package com.newsaggregator.infrastructure.adapter.persistence.adapter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.FeedPersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.repository.CategoryJpaRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.FeedJpaRepository;

/**
 * Unit-Test für FeedRepositoryAdapter.
 *
 * <p>Testet die Konvertierung zwischen Domain-Objekten und JPA Entities.</p>
 */
@ExtendWith(MockitoExtension.class)
class FeedRepositoryAdapterTest {

    @Mock
    private FeedJpaRepository feedJpaRepository;

    @Mock
    private CategoryJpaRepository categoryJpaRepository;

    @Mock
    private FeedPersistenceMapper mapper;

    private FeedRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FeedRepositoryAdapter(feedJpaRepository, categoryJpaRepository, mapper);
    }

    @Test
    void findById_ShouldReturnFeed_WhenExists() {
        // Given
        Long feedId = 1L;
        FeedJpaEntity jpaEntity = createFeedJpaEntity(feedId, "Tech Blog", "https://example.com/feed.xml");
        Feed domainFeed = createDomainFeed(feedId, "Tech Blog", "https://example.com/feed.xml");

        when(feedJpaRepository.findById(feedId)).thenReturn(Optional.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(domainFeed);

        // When
        Optional<Feed> result = adapter.findById(FeedId.of(feedId));

        // Then
        assertTrue(result.isPresent());
        assertEquals("Tech Blog", result.get().getName());
        assertEquals(feedId, result.get().getId().getValue());
        verify(feedJpaRepository).findById(feedId);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        Long feedId = 1L;
        when(feedJpaRepository.findById(feedId)).thenReturn(Optional.empty());

        // When
        Optional<Feed> result = adapter.findById(FeedId.of(feedId));

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllFeeds() {
        // Given
        Long id1 = 1L;
        Long id2 = 2L;
        FeedJpaEntity entity1 = createFeedJpaEntity(id1, "Tech Blog", "https://tech.example.com/feed.xml");
        FeedJpaEntity entity2 = createFeedJpaEntity(id2, "News Daily", "https://news.example.com/feed.xml");
        Feed domainFeed1 = createDomainFeed(id1, "Tech Blog", "https://tech.example.com/feed.xml");
        Feed domainFeed2 = createDomainFeed(id2, "News Daily", "https://news.example.com/feed.xml");

        when(feedJpaRepository.findAllWithCategories()).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(domainFeed1);
        when(mapper.toDomain(entity2)).thenReturn(domainFeed2);

        // When
        List<Feed> result = adapter.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("Tech Blog", result.get(0).getName());
        assertEquals("News Daily", result.get(1).getName());
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoFeeds() {
        // Given
        when(feedJpaRepository.findAllWithCategories()).thenReturn(Collections.emptyList());

        // When
        List<Feed> result = adapter.findAll();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldCreateNewFeed_WhenFeedHasNoId() {
        // Given
        Long feedId = 1L;
        Feed newFeed = Feed.createNew("Tech Blog", "https://example.com/feed.xml", "Description");
        FeedJpaEntity jpaEntity = createFeedJpaEntity(feedId, "Tech Blog", "https://example.com/feed.xml");
        Feed savedDomainFeed = createDomainFeed(feedId, "Tech Blog", "https://example.com/feed.xml");

        when(mapper.toJpaEntity(newFeed)).thenReturn(jpaEntity);
        when(feedJpaRepository.save(jpaEntity)).thenReturn(jpaEntity);
        when(mapper.toDomain(jpaEntity)).thenReturn(savedDomainFeed);

        // When
        Feed result = adapter.save(newFeed);

        // Then
        assertNotNull(result);
        assertEquals("Tech Blog", result.getName());
        assertEquals(feedId, result.getId().getValue());
    }

    @Test
    void save_ShouldUpdateExistingFeed_WhenFeedHasId() {
        // Given
        Long feedId = 1L;
        Feed existingFeed = createDomainFeed(feedId, "Tech Blog", "https://example.com/feed.xml");
        FeedJpaEntity existingEntity = createFeedJpaEntity(feedId, "Tech Blog", "https://example.com/feed.xml");
        FeedJpaEntity updatedEntity = createFeedJpaEntity(feedId, "Updated Tech Blog", "https://example.com/feed.xml");
        Feed savedDomainFeed = createDomainFeed(feedId, "Updated Tech Blog", "https://example.com/feed.xml");

        when(feedJpaRepository.existsById(feedId)).thenReturn(true);
        when(feedJpaRepository.findById(feedId)).thenReturn(Optional.of(existingEntity));
        when(feedJpaRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(mapper.toDomain(updatedEntity)).thenReturn(savedDomainFeed);

        // When
        Feed result = adapter.save(existingFeed);

        // Then
        assertNotNull(result);
        assertEquals("Updated Tech Blog", result.getName());
    }

    @Test
    void deleteById_ShouldDeleteFeed() {
        // Given
        Long feedId = 1L;

        // When
        adapter.deleteById(FeedId.of(feedId));

        // Then
        verify(feedJpaRepository).deleteById(feedId);
    }

    @Test
    void existsByUrl_ShouldReturnTrue_WhenFeedExists() {
        // Given
        String url = "https://example.com/feed.xml";
        when(feedJpaRepository.existsByUrl(url)).thenReturn(true);

        // When
        boolean result = adapter.existsByUrl(url);

        // Then
        assertTrue(result);
    }

    @Test
    void existsByUrl_ShouldReturnFalse_WhenFeedDoesNotExist() {
        // Given
        String url = "https://example.com/feed.xml";
        when(feedJpaRepository.existsByUrl(url)).thenReturn(false);

        // When
        boolean result = adapter.existsByUrl(url);

        // Then
        assertFalse(result);
    }

    @Test
    void findByUrl_ShouldReturnFeed_WhenExists() {
        // Given
        String url = "https://example.com/feed.xml";
        Long feedId = 1L;
        FeedJpaEntity jpaEntity = createFeedJpaEntity(feedId, "Tech Blog", url);
        Feed domainFeed = createDomainFeed(feedId, "Tech Blog", url);

        when(feedJpaRepository.findByUrl(url)).thenReturn(Optional.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(domainFeed);

        // When
        Optional<Feed> result = adapter.findByUrl(url);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Tech Blog", result.get().getName());
        assertEquals(url, result.get().getUrl());
    }

    // Hilfsmethoden

    private FeedJpaEntity createFeedJpaEntity(Long id, String name, String url) {
        FeedJpaEntity entity = new FeedJpaEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setUrl(url);
        entity.setDescription("Description for " + name);
        entity.setStatus(FeedJpaEntity.FeedStatus.ACTIVE);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setLastFetched(LocalDateTime.now());
        return entity;
    }

    private Feed createDomainFeed(Long id, String name, String url) {
        return Feed.of(
                FeedId.of(id),
                name,
                url,
                "Description for " + name,
                LocalDateTime.now(),
                LocalDateTime.now(),
                com.newsaggregator.domain.model.FeedStatus.ACTIVE
        );
    }
}