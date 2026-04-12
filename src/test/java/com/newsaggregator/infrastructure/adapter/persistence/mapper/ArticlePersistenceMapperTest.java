package com.newsaggregator.infrastructure.adapter.persistence.mapper;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Test für ArticlePersistenceMapper.
 *
 * <p>Testet die Konvertierung zwischen Article Domain und JPA Entity.</p>
 */
class ArticlePersistenceMapperTest {

    private final ArticlePersistenceMapper mapper = new ArticlePersistenceMapper();

    @Test
    void toDomain_ShouldConvertEntityToDomain() {
        // Given
        FeedJpaEntity feedEntity = new FeedJpaEntity();
        feedEntity.setId(1L);
        feedEntity.setName("Test Feed");
        feedEntity.setUrl("https://example.com/feed");
        feedEntity.setCreatedAt(LocalDateTime.now());
        feedEntity.setStatus(FeedJpaEntity.FeedStatus.ACTIVE);

        ArticleJpaEntity entity = new ArticleJpaEntity();
        entity.setId(1L);
        entity.setTitle("Test Article");
        entity.setDescription("Description");
        entity.setLink("https://example.com/article");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setPublishedAt(LocalDateTime.now());
        entity.setFeed(feedEntity);

        // When
        Article domain = mapper.toDomain(entity);

        // Then
        assertNotNull(domain);
        assertEquals("Test Article", domain.getTitle());
        assertEquals("https://example.com/article", domain.getLink());
        assertNotNull(domain.getFeed());
        assertEquals("Test Feed", domain.getFeed().getName());
    }

    @Test
    void toDomain_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toJpaEntity_ShouldConvertDomainToEntity() {
        // Given
        FeedJpaEntity feedEntity = new FeedJpaEntity();
        feedEntity.setId(1L);
        feedEntity.setName("Test Feed");
        feedEntity.setUrl("https://example.com/feed");
        feedEntity.setCreatedAt(LocalDateTime.now());
        feedEntity.setStatus(FeedJpaEntity.FeedStatus.ACTIVE);

        Feed feed = Feed.of(
                FeedId.of(1L), "Test Feed", "https://example.com/feed", null,
                LocalDateTime.now(), null, com.newsaggregator.domain.model.FeedStatus.ACTIVE
        );

        Article article = Article.createNew("Title", "Desc", "https://example.com/article",
                LocalDateTime.now(), feed);

        // When
        ArticleJpaEntity entity = mapper.toJpaEntity(article, feedEntity);

        // Then
        assertNotNull(entity);
        assertEquals("Title", entity.getTitle());
        assertEquals("https://example.com/article", entity.getLink());
        assertEquals(feedEntity, entity.getFeed());
    }

    @Test
    void toJpaEntity_ShouldReturnNull_WhenDomainIsNull() {
        assertNull(mapper.toJpaEntity(null, new FeedJpaEntity()));
    }

    @Test
    void updateJpaEntity_ShouldUpdateExistingEntity() {
        // Given
        ArticleJpaEntity entity = new ArticleJpaEntity();
        entity.setId(1L);
        entity.setTitle("Old Title");
        entity.setDescription("Old Desc");
        entity.setLink("https://example.com/old");

        Feed feed = Feed.createNew("Feed", "https://example.com/feed", "Test");
        Article article = Article.of(
                com.newsaggregator.domain.model.ArticleId.of(1L),
                "New Title", "New Desc", "https://example.com/new",
                LocalDateTime.now(), feed, LocalDateTime.now()
        );

        // When
        mapper.updateJpaEntity(entity, article);

        // Then
        assertEquals("New Title", entity.getTitle());
        assertEquals("New Desc", entity.getDescription());
        assertEquals("https://example.com/new", entity.getLink());
    }
}
