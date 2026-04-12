package com.newsaggregator.infrastructure.adapter.persistence.mapper;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Test für FeedPersistenceMapper.
 *
 * <p>Testet die Konvertierung zwischen Domain-Objekten und JPA-Entities.</p>
 */
class FeedPersistenceMapperTest {

    private final FeedPersistenceMapper mapper = new FeedPersistenceMapper();

    @Test
    void toDomain_ShouldConvertEntityToDomain() {
        // Given
        FeedJpaEntity entity = new FeedJpaEntity();
        entity.setId(1L);
        entity.setName("Test Feed");
        entity.setUrl("https://example.com/feed");
        entity.setDescription("Test Description");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setLastFetched(LocalDateTime.now());
        entity.setStatus(FeedJpaEntity.FeedStatus.ACTIVE);

        // When
        Feed domain = mapper.toDomain(entity);

        // Then
        assertNotNull(domain);
        assertEquals(1L, domain.getId().getValue());
        assertEquals("Test Feed", domain.getName());
        assertEquals("https://example.com/feed", domain.getUrl());
        assertEquals(FeedStatus.ACTIVE, domain.getStatus());
    }

    @Test
    void toDomain_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toJpaEntity_ShouldConvertDomainToEntity() {
        // Given
        Feed domain = Feed.of(
                FeedId.of(1L),
                "Test Feed",
                "https://example.com/feed",
                "Description",
                LocalDateTime.now(),
                LocalDateTime.now(),
                FeedStatus.ACTIVE
        );

        // When
        FeedJpaEntity entity = mapper.toJpaEntity(domain);

        // Then
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("Test Feed", entity.getName());
        assertEquals(FeedJpaEntity.FeedStatus.ACTIVE, entity.getStatus());
    }

    @Test
    void toJpaEntity_ShouldNotSetId_ForNewFeed() {
        // Given
        Feed domain = Feed.createNew("Test Feed", "https://example.com/feed", "Description");

        // When
        FeedJpaEntity entity = mapper.toJpaEntity(domain);

        // Then
        assertNull(entity.getId());
        assertEquals("Test Feed", entity.getName());
    }

    @Test
    void toJpaEntity_ShouldReturnNull_WhenDomainIsNull() {
        assertNull(mapper.toJpaEntity(null));
    }

    @Test
    void updateJpaEntity_ShouldUpdateExistingEntity() {
        // Given
        FeedJpaEntity entity = new FeedJpaEntity();
        entity.setId(1L);
        entity.setName("Old Name");
        entity.setUrl("https://example.com/old");
        entity.setCreatedAt(LocalDateTime.now());

        Feed domain = Feed.of(
                FeedId.of(1L),
                "New Name",
                "https://example.com/new",
                "New Description",
                LocalDateTime.now(),
                LocalDateTime.now(),
                FeedStatus.ERROR
        );

        // When
        mapper.updateJpaEntity(entity, domain);

        // Then
        assertEquals("New Name", entity.getName());
        assertEquals("https://example.com/new", entity.getUrl());
        assertEquals(FeedJpaEntity.FeedStatus.ERROR, entity.getStatus());
    }

    @Test
    void updateJpaEntity_ShouldDoNothing_WhenNull() {
        // Should not throw
        mapper.updateJpaEntity(null, Feed.createNew("Test", "https://example.com/feed", "Test"));
        mapper.updateJpaEntity(new FeedJpaEntity(), null);
    }

    @Test
    void mapStatus_ShouldConvertAllStatuses() {
        // Test all status mappings
        assertEquals(FeedStatus.ACTIVE, invokeMapStatus(FeedJpaEntity.FeedStatus.ACTIVE));
        assertEquals(FeedStatus.ERROR, invokeMapStatus(FeedJpaEntity.FeedStatus.ERROR));
        assertEquals(FeedStatus.DISABLED, invokeMapStatus(FeedJpaEntity.FeedStatus.DISABLED));
    }

    // Helper method to test status mapping (private method)
    private FeedStatus invokeMapStatus(FeedJpaEntity.FeedStatus status) {
        // Create entity with specific status
        FeedJpaEntity entity = new FeedJpaEntity();
        entity.setId(1L);
        entity.setName("Test");
        entity.setUrl("https://example.com/feed");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setStatus(status);

        Feed domain = mapper.toDomain(entity);
        return domain.getStatus();
    }
}
