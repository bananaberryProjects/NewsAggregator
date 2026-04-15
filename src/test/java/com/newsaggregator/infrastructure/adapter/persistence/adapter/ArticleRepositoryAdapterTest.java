package com.newsaggregator.infrastructure.adapter.persistence.adapter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.ArticlePersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.FeedJpaRepository;

/**
 * Unit-Test für ArticleRepositoryAdapter.
 *
 * <p>Testet die Konvertierung zwischen Domain-Objekten und JPA Entities.</p>
 */
@ExtendWith(MockitoExtension.class)
class ArticleRepositoryAdapterTest {

    @Mock
    private ArticleJpaRepository jpaRepository;

    @Mock
    private FeedJpaRepository feedJpaRepository;

    @Mock
    private ArticlePersistenceMapper mapper;

    private ArticleRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ArticleRepositoryAdapter(jpaRepository, feedJpaRepository, mapper);
    }

    @Test
    void findById_ShouldReturnArticle_WhenExists() {
        // Given
        Long articleId = 1L;
        ArticleJpaEntity jpaEntity = createArticleJpaEntity(articleId, "Test Article");
        Article domainArticle = createDomainArticle(articleId, "Test Article");

        when(jpaRepository.findById(articleId)).thenReturn(Optional.of(jpaEntity));
        when(mapper.toDomain(jpaEntity)).thenReturn(domainArticle);

        // When
        Optional<Article> result = adapter.findById(ArticleId.of(articleId));

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Article", result.get().getTitle());
        verify(jpaRepository).findById(articleId);
        verify(mapper).toDomain(jpaEntity);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        Long articleId = 999L;
        when(jpaRepository.findById(articleId)).thenReturn(Optional.empty());

        // When
        Optional<Article> result = adapter.findById(ArticleId.of(articleId));

        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository).findById(articleId);
        verifyNoInteractions(mapper);
    }

    @Test
    void findAll_ShouldReturnAllArticles() {
        // Given
        ArticleJpaEntity entity1 = createArticleJpaEntity(1L, "Article 1");
        ArticleJpaEntity entity2 = createArticleJpaEntity(2L, "Article 2");
        Article domain1 = createDomainArticle(1L, "Article 1");
        Article domain2 = createDomainArticle(2L, "Article 2");

        when(jpaRepository.findAllWithFeedAndCategories()).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(domain1);
        when(mapper.toDomain(entity2)).thenReturn(domain2);

        // When
        List<Article> result = adapter.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("Article 1", result.get(0).getTitle());
        assertEquals("Article 2", result.get(1).getTitle());
        verify(jpaRepository).findAllWithFeedAndCategories();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoArticles() {
        // Given
        when(jpaRepository.findAllWithFeedAndCategories()).thenReturn(Collections.emptyList());

        // When
        List<Article> result = adapter.findAll();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldUpdateExistingArticle_WhenExists() {
        // Given
        Long articleId = 1L;
        FeedJpaEntity feedEntity = createFeedJpaEntity(1L, "Test Feed");
        Feed feed = Feed.of(FeedId.of(1L), "Test Feed", "https://example.com", null, LocalDateTime.now(), null, FeedStatus.ACTIVE);
        Article article = Article.of(
                ArticleId.of(articleId), "Updated Article", "Description", "https://example.com/article",
                null, LocalDateTime.now(), feed, LocalDateTime.now()
        );
        ArticleJpaEntity existingEntity = createArticleJpaEntity(articleId, "Old Article");
        ArticleJpaEntity savedEntity = createArticleJpaEntity(articleId, "Updated Article");
        Article savedArticle = createDomainArticle(articleId, "Updated Article");

        when(feedJpaRepository.findById(1L)).thenReturn(Optional.of(feedEntity));
        when(jpaRepository.existsById(articleId)).thenReturn(true);
        when(jpaRepository.findById(articleId)).thenReturn(Optional.of(existingEntity));
        doNothing().when(mapper).updateJpaEntity(existingEntity, article);
        when(jpaRepository.save(existingEntity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedArticle);

        // When
        Article result = adapter.save(article);

        // Then
        assertNotNull(result);
        assertEquals("Updated Article", result.getTitle());
        verify(mapper).updateJpaEntity(existingEntity, article);
    }

    @Test
    void save_ShouldThrowException_WhenFeedNotFound() {
        // Given
        Feed feed = Feed.of(FeedId.of(999L), "Test Feed", "https://example.com", null, LocalDateTime.now(), null, FeedStatus.ACTIVE);
        Article article = Article.createNew("New Article", "Description", "https://example.com/article", null, feed);

        when(feedJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> adapter.save(article));
        verify(feedJpaRepository).findById(999L);
    }

    @Test
    void saveAll_ShouldSaveMultipleArticles() {
        // Given
        FeedJpaEntity feedEntity = createFeedJpaEntity(1L, "Test Feed");
        Feed feed = Feed.of(FeedId.of(1L), "Test Feed", "https://example.com", null, LocalDateTime.now(), null, FeedStatus.ACTIVE);
        Article article1 = Article.createNew("Article 1", "Desc 1", "https://example.com/1", null, feed);
        Article article2 = Article.createNew("Article 2", "Desc 2", "https://example.com/2", null, feed);

        ArticleJpaEntity entity1 = createArticleJpaEntity(1L, "Article 1");
        ArticleJpaEntity entity2 = createArticleJpaEntity(2L, "Article 2");

        when(feedJpaRepository.findById(1L)).thenReturn(Optional.of(feedEntity));
        when(mapper.toJpaEntity(any(Article.class), any(FeedJpaEntity.class)))
                .thenReturn(entity1, entity2);
        when(jpaRepository.save(any(ArticleJpaEntity.class)))
                .thenReturn(entity1, entity2);
        when(mapper.toDomain(entity1)).thenReturn(article1);
        when(mapper.toDomain(entity2)).thenReturn(article2);

        // When
        List<Article> result = adapter.saveAll(List.of(article1, article2));

        // Then
        assertEquals(2, result.size());
        verify(jpaRepository, times(2)).save(any(ArticleJpaEntity.class));
        verify(mapper, times(2)).toJpaEntity(any(Article.class), any(FeedJpaEntity.class));
    }

    @Test
    void existsByLink_ShouldReturnTrue_WhenArticleExists() {
        // Given
        String link = "https://example.com/article";
        when(jpaRepository.existsByLink(link)).thenReturn(true);

        // When
        boolean result = adapter.existsByLink(link);

        // Then
        assertTrue(result);
        verify(jpaRepository).existsByLink(link);
    }

    @Test
    void existsByLink_ShouldReturnFalse_WhenArticleNotExists() {
        // Given
        String link = "https://example.com/not-existing";
        when(jpaRepository.existsByLink(link)).thenReturn(false);

        // When
        boolean result = adapter.existsByLink(link);

        // Then
        assertFalse(result);
    }

    @Test
    void searchByQuery_ShouldReturnMatchingArticles() {
        // Given
        String query = "Spring";
        ArticleJpaEntity entity = createArticleJpaEntity(1L, "Spring Boot Tutorial");
        Article domain = createDomainArticle(1L, "Spring Boot Tutorial");

        when(jpaRepository.searchByQuery(query)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        // When
        List<Article> result = adapter.searchByQuery(query);

        // Then
        assertEquals(1, result.size());
        assertEquals("Spring Boot Tutorial", result.get(0).getTitle());
    }

    @Test
    void findByFeedId_ShouldReturnArticlesForFeed() {
        // Given
        Long feedId = 1L;
        ArticleJpaEntity entity1 = createArticleJpaEntity(1L, "Article 1");
        ArticleJpaEntity entity2 = createArticleJpaEntity(2L, "Article 2");
        Article domain1 = createDomainArticle(1L, "Article 1");
        Article domain2 = createDomainArticle(2L, "Article 2");

        when(jpaRepository.findByFeedId(feedId)).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(domain1);
        when(mapper.toDomain(entity2)).thenReturn(domain2);

        // When
        List<Article> result = adapter.findByFeedId(feedId);

        // Then
        assertEquals(2, result.size());
        verify(jpaRepository).findByFeedId(feedId);
    }

    // Hilfsmethoden

    private ArticleJpaEntity createArticleJpaEntity(Long id, String title) {
        ArticleJpaEntity entity = new ArticleJpaEntity();
        entity.setId(id);
        entity.setTitle(title);
        entity.setDescription("Description");
        entity.setLink("https://example.com/article");
        entity.setCreatedAt(LocalDateTime.now());
        
        FeedJpaEntity feed = createFeedJpaEntity(1L, "Test Feed");
        entity.setFeed(feed);
        
        return entity;
    }

    private FeedJpaEntity createFeedJpaEntity(Long id, String name) {
        FeedJpaEntity feed = new FeedJpaEntity();
        feed.setId(id);
        feed.setName(name);
        feed.setUrl("https://example.com/feed");
        feed.setDescription("Test Description");
        return feed;
    }

    private Article createDomainArticle(Long id, String title) {
        Feed feed = Feed.of(FeedId.of(1L), "Test Feed", "https://example.com", null, LocalDateTime.now(), null, FeedStatus.ACTIVE);
        return Article.of(
                ArticleId.of(id),
                title,
                "Description",
                "https://example.com/article",
                null,
                LocalDateTime.now(),
                feed,
                LocalDateTime.now()
        );
    }
}