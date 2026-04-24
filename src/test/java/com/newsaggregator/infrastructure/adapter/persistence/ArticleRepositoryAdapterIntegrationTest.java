package com.newsaggregator.infrastructure.adapter.persistence;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.FeedJpaRepository;

/**
 * Integrationstest für ArticleRepositoryAdapter.
 *
 * <p>Testet die Integration zwischen Article-Domain und JPA-Repository.</p>
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class ArticleRepositoryAdapterIntegrationTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleJpaRepository articleJpaRepository;

    @Autowired
    private FeedJpaRepository feedJpaRepository;

    private FeedJpaEntity createFeedInDb() {
        FeedJpaEntity feed = new FeedJpaEntity();
        feed.setName("Test Feed");
        feed.setUrl("https://example.com/feed-" + System.currentTimeMillis());
        feed.setCreatedAt(java.time.LocalDateTime.now());
        feed.setStatus(FeedJpaEntity.FeedStatus.ACTIVE);
        return feedJpaRepository.save(feed);
    }

    @Test
    void save_ShouldPersistNewArticle() {
        // Given
        FeedJpaEntity feedEntity = createFeedInDb();
        Feed feed = com.newsaggregator.domain.model.Feed.of(
                com.newsaggregator.domain.model.FeedId.of(feedEntity.getId()),
                feedEntity.getName(),
                feedEntity.getUrl(),
                null,
                feedEntity.getCreatedAt(),
                null,
                com.newsaggregator.domain.model.FeedStatus.ACTIVE
        );

        Article article = Article.createNew("Test Article", "Description",
                "https://example.com/article", java.time.LocalDateTime.now(), feed);

        // When
        Article saved = articleRepository.save(article);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Test Article", saved.getTitle());

        Optional<ArticleJpaEntity> inDb = articleJpaRepository.findById(saved.getId().getValue());
        assertTrue(inDb.isPresent());
        assertEquals("Test Article", inDb.get().getTitle());
    }

    @Test
    void findById_ShouldReturnArticle_WhenExists() {
        // Given
        FeedJpaEntity feedEntity = createFeedInDb();
        ArticleJpaEntity articleEntity = new ArticleJpaEntity();
        articleEntity.setTitle("Test Article");
        articleEntity.setLink("https://example.com/article");
        articleEntity.setCreatedAt(java.time.LocalDateTime.now());
        articleEntity.setFeed(feedEntity);
        ArticleJpaEntity saved = articleJpaRepository.save(articleEntity);

        // When
        Optional<Article> found = articleRepository.findById(ArticleId.of(saved.getId()));

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Article", found.get().getTitle());
    }

    @Test
    void existsByLink_ShouldReturnTrue_WhenArticleExists() {
        // Given
        FeedJpaEntity feedEntity = createFeedInDb();
        ArticleJpaEntity article = new ArticleJpaEntity();
        article.setTitle("Test");
        article.setLink("https://example.com/existing-article");
        article.setCreatedAt(java.time.LocalDateTime.now());
        article.setFeed(feedEntity);
        articleJpaRepository.save(article);

        // When
        boolean exists = articleRepository.existsByLink("https://example.com/existing-article");

        // Then
        assertTrue(exists);
    }

    @Test
    void searchByQuery_ShouldReturnMatchingArticles() {
        // Given
        FeedJpaEntity feedEntity = createFeedInDb();
        ArticleJpaEntity article1 = new ArticleJpaEntity();
        article1.setTitle("Spring Boot Tutorial");
        article1.setLink("https://example.com/1");
        article1.setCreatedAt(java.time.LocalDateTime.now());
        article1.setFeed(feedEntity);
        articleJpaRepository.save(article1);

        ArticleJpaEntity article2 = new ArticleJpaEntity();
        article2.setTitle("Java Basics");
        article2.setLink("https://example.com/2");
        article2.setCreatedAt(java.time.LocalDateTime.now());
        article2.setFeed(feedEntity);
        articleJpaRepository.save(article2);

        // When
        List<Article> results = articleRepository.searchByQuery("Spring");

        // Then
        assertEquals(1, results.size());
        assertEquals("Spring Boot Tutorial", results.get(0).getTitle());
    }

    @Test
    void findByFeedId_ShouldReturnArticlesForFeed() {
        // Given
        FeedJpaEntity feed1 = createFeedInDb();
        FeedJpaEntity feed2 = createFeedInDb();
        feed2.setUrl("https://example.com/other-" + System.currentTimeMillis());
        feed2 = feedJpaRepository.save(feed2);

        ArticleJpaEntity article = new ArticleJpaEntity();
        article.setTitle("Article for Feed 1");
        article.setLink("https://example.com/article");
        article.setCreatedAt(java.time.LocalDateTime.now());
        article.setFeed(feed1);
        articleJpaRepository.save(article);

        // When
        List<Article> results = articleRepository.findByFeedId(feed1.getId());

        // Then
        assertEquals(1, results.size());
        assertEquals("Article for Feed 1", results.get(0).getTitle());
    }

    @Test
    void saveAll_ShouldSaveMultipleArticles() {
        // Given
        FeedJpaEntity feedEntity = createFeedInDb();
        Feed feed = com.newsaggregator.domain.model.Feed.of(
                com.newsaggregator.domain.model.FeedId.of(feedEntity.getId()),
                feedEntity.getName(),
                feedEntity.getUrl(),
                null,
                feedEntity.getCreatedAt(),
                null,
                com.newsaggregator.domain.model.FeedStatus.ACTIVE
        );

        Article article1 = Article.createNew("Article 1", "Desc 1",
                "https://example.com/1", java.time.LocalDateTime.now(), feed);
        Article article2 = Article.createNew("Article 2", "Desc 2",
                "https://example.com/2", java.time.LocalDateTime.now(), feed);

        // When
        List<Article> saved = articleRepository.saveAll(List.of(article1, article2));

        // Then
        assertEquals(2, saved.size());
        assertNotNull(saved.get(0).getId());
        assertNotNull(saved.get(1).getId());
    }
}
