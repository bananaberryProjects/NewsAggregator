package com.newsaggregator.application.mapper;

import com.newsaggregator.application.dto.FeedDto;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;
import org.springframework.stereotype.Component;

/**
 * Mapper für die Konvertierung zwischen Feed (Domain) und FeedDto.
 */
@Component
public class FeedMapper {

    private final ArticleJpaRepository articleJpaRepository;

    public FeedMapper(ArticleJpaRepository articleJpaRepository) {
        this.articleJpaRepository = articleJpaRepository;
    }

    /**
     * Wandelt ein Domain-Objekt in ein DTO um.
     */
    public FeedDto toDto(Feed feed) {
        if (feed == null) {
            return null;
        }

        // Artikelanzahl direkt aus DB abfragen (effizienter als Lazy Loading)
        int articleCount = 0;
        if (feed.getId() != null) {
            articleCount = (int) articleJpaRepository.countByFeedId(feed.getId().getValue());
        }

        return FeedDto.builder()
                .id(feed.getId() != null ? feed.getId().getValue() : null)
                .name(feed.getName())
                .url(feed.getUrl())
                .description(feed.getDescription())
                .lastFetched(feed.getLastFetched())
                .createdAt(feed.getCreatedAt())
                .status(feed.getStatus() != null ? feed.getStatus().name() : null)
                .articleCount(articleCount)
                .build();
    }
}
