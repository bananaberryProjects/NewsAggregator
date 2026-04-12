package com.newsaggregator.infrastructure.adapter.persistence.mapper;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper für die Konvertierung zwischen Feed (Domain) und FeedJpaEntity.
 */
@Component
public class FeedPersistenceMapper {

    /**
     * Wandelt eine JPA Entity in ein Domain-Objekt um.
     */
    public Feed toDomain(FeedJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Feed.of(
                FeedId.of(entity.getId()),
                entity.getName(),
                entity.getUrl(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getLastFetched(),
                mapStatus(entity.getStatus())
        );
    }

    /**
     * Wandelt ein Domain-Objekt in eine JPA Entity um.
     * Bei neuen Feeds (ohne ID) wird die ID nicht gesetzt.
     */
    public FeedJpaEntity toJpaEntity(Feed domain) {
        if (domain == null) {
            return null;
        }

        FeedJpaEntity entity = new FeedJpaEntity();

        // ID nur setzen, wenn vorhanden (nicht bei neuen Feeds)
        if (domain.getId() != null) {
            entity.setId(domain.getId().getValue());
        }

        entity.setName(domain.getName());
        entity.setUrl(domain.getUrl());
        entity.setDescription(domain.getDescription());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setLastFetched(domain.getLastFetched());
        entity.setStatus(mapStatus(domain.getStatus()));

        return entity;
    }

    /**
     * Aktualisiert eine bestehende JPA Entity mit Werten aus dem Domain-Objekt.
     */
    public void updateJpaEntity(FeedJpaEntity entity, Feed domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setName(domain.getName());
        entity.setUrl(domain.getUrl());
        entity.setDescription(domain.getDescription());
        entity.setLastFetched(domain.getLastFetched());
        entity.setStatus(mapStatus(domain.getStatus()));
        // createdAt und ID werden nicht aktualisiert
    }

    // ==================== Hilfsmethoden ====================

    private FeedStatus mapStatus(FeedJpaEntity.FeedStatus status) {
        if (status == null) {
            return FeedStatus.ACTIVE;
        }
        return switch (status) {
            case ACTIVE -> FeedStatus.ACTIVE;
            case ERROR -> FeedStatus.ERROR;
            case DISABLED -> FeedStatus.DISABLED;
        };
    }

    private FeedJpaEntity.FeedStatus mapStatus(FeedStatus status) {
        if (status == null) {
            return FeedJpaEntity.FeedStatus.ACTIVE;
        }
        return switch (status) {
            case ACTIVE -> FeedJpaEntity.FeedStatus.ACTIVE;
            case ERROR -> FeedJpaEntity.FeedStatus.ERROR;
            case DISABLED -> FeedJpaEntity.FeedStatus.DISABLED;
        };
    }
}
