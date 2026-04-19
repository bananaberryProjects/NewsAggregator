package com.newsaggregator.infrastructure.adapter.persistence.adapter;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.ArticlePersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.FeedJpaRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter für ArticleRepository.
 *
 * <p>Implementiert den Domain-Port {@link ArticleRepository} und
 * delegiert die Operationen an das Spring Data JPA Repository.</p>
 */
@Component
@Transactional
public class ArticleRepositoryAdapter implements ArticleRepository {

    private final ArticleJpaRepository jpaRepository;
    private final FeedJpaRepository feedJpaRepository;
    private final ArticlePersistenceMapper mapper;

    public ArticleRepositoryAdapter(ArticleJpaRepository jpaRepository,
                                     FeedJpaRepository feedJpaRepository,
                                     ArticlePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.feedJpaRepository = feedJpaRepository;
        this.mapper = mapper;
    }

    @SuppressWarnings("null")
    @Override
    public Article save(Article article) {
        // Feed aus der Datenbank laden für die Relation
        FeedJpaEntity feedEntity = feedJpaRepository.findById(article.getFeed().getId().getValue())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Feed nicht gefunden: " + article.getFeed().getId()));

        ArticleJpaEntity entity;

        if (article.getId() != null && jpaRepository.existsById(article.getId().getValue())) {
            // Existierenden Artikel aktualisieren
            Optional<ArticleJpaEntity> existing = jpaRepository.findById(article.getId().getValue());
            if (existing.isPresent()) {
                entity = existing.get();
                mapper.updateJpaEntity(entity, article);
            } else {
                entity = mapper.toJpaEntity(article, feedEntity);
            }
        } else {
            // Neuen Artikel erstellen
            entity = mapper.toJpaEntity(article, feedEntity);
        }

        ArticleJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Article> saveAll(List<Article> articles) {
        return articles.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

   @SuppressWarnings("null")
 @Override
    @Transactional(readOnly = true)
    public Optional<Article> findById(ArticleId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> findAll() {
        return jpaRepository.findAllWithFeedAndCategories().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> searchByQuery(String query) {
        return jpaRepository.searchByQuery(query).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> findByFeedId(Long feedId) {
        return jpaRepository.findByFeedId(feedId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLink(String link) {
        return jpaRepository.existsByLink(link);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> findByPublishedAtAfter(LocalDateTime date) {
        return jpaRepository.findByPublishedAtAfter(date).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
