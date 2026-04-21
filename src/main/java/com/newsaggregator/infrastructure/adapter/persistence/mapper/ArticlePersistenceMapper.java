package com.newsaggregator.infrastructure.adapter.persistence.mapper;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper für die Konvertierung zwischen Article (Domain) und ArticleJpaEntity.
 */
@Component
public class ArticlePersistenceMapper {

    /**
     * Wandelt eine JPA Entity in ein Domain-Objekt um.
     * Hinweis: Das Feed-Objekt im Article ist nur ein "Stub" mit ID und URL
     * (keine vollständige Feed-Daten, um Lazy Loading zu vermeiden).
     */
    public Article toDomain(ArticleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        // Für das Feed-Objekt erstellen wir einen minimalen Stub
        // (nur ID und URL, um zirkuläre Abhängigkeiten zu vermeiden)
        var feedStub = createFeedStub(entity.getFeed());

        return Article.of(
                ArticleId.of(entity.getId()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getLink(),
                entity.getImageUrl(),
                entity.getContentHtml(),
                entity.getContentExtractionFailed() != null ? entity.getContentExtractionFailed() : false,
                entity.getPublishedAt(),
                feedStub,
                entity.getCreatedAt()
        );
    }

    /**
     * Wandelt ein Domain-Objekt in eine JPA Entity um.
     * Bei neuen Artikeln (ohne ID) wird die ID nicht gesetzt.
     */
    public ArticleJpaEntity toJpaEntity(Article domain, FeedJpaEntity feedEntity) {
        if (domain == null) {
            return null;
        }

        ArticleJpaEntity entity = new ArticleJpaEntity();

        // ID nur setzen, wenn vorhanden
        if (domain.getId() != null) {
            entity.setId(domain.getId().getValue());
        }

        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setLink(domain.getLink());
        entity.setImageUrl(domain.getImageUrl());
        entity.setContentHtml(domain.getExtractedContent());
        entity.setPublishedAt(domain.getPublishedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setFeed(feedEntity);

        return entity;
    }

    /**
     * Aktualisiert eine bestehende JPA Entity mit Werten aus dem Domain-Objekt.
     */
    public void updateJpaEntity(ArticleJpaEntity entity, Article domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setLink(domain.getLink());
        entity.setImageUrl(domain.getImageUrl());
        entity.setContentHtml(domain.getExtractedContent());
        entity.setPublishedAt(domain.getPublishedAt());
        // Feed und createdAt werden nicht aktualisiert
    }

    // ==================== Hilfsmethoden ====================

    /**
     * Erstellt einen minimalen Feed-Stub für die Domain.
     * Verhindert zirkuläre Abhängigkeiten beim Laden von Artikeln.
     */
    private com.newsaggregator.domain.model.Feed createFeedStub(FeedJpaEntity feedEntity) {
        if (feedEntity == null) {
            return null;
        }

        // Minimaler Feed mit nur ID und URL (keine Articles, um Rekursion zu vermeiden)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        var feed = com.newsaggregator.domain.model.Feed.of(
                com.newsaggregator.domain.model.FeedId.of(feedEntity.getId()),
                feedEntity.getName(),
                feedEntity.getUrl(),
                feedEntity.getDescription(),
                feedEntity.getCreatedAt() != null ? feedEntity.getCreatedAt() : now,
                feedEntity.getLastFetched(),
                mapStatus(feedEntity.getStatus())
        );

        // Kategorien hinzufügen (Duplikate vermeiden durch Set)
        if (feedEntity.getCategories() != null) {
            var categoryIds = feedEntity.getCategories().stream()
                    .map(cat -> com.newsaggregator.domain.model.CategoryId.of(cat.getId().toString()))
                    .distinct()
                    .toList();
            feed.setCategories(categoryIds);
        }

        return feed;
    }

    private com.newsaggregator.domain.model.FeedStatus mapStatus(FeedJpaEntity.FeedStatus status) {
        if (status == null) {
            return com.newsaggregator.domain.model.FeedStatus.ACTIVE;
        }
        return switch (status) {
            case ACTIVE -> com.newsaggregator.domain.model.FeedStatus.ACTIVE;
            case ERROR -> com.newsaggregator.domain.model.FeedStatus.ERROR;
            case DISABLED -> com.newsaggregator.domain.model.FeedStatus.DISABLED;
        };
    }
}
