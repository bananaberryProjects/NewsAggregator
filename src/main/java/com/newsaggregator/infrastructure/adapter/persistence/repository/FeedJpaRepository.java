package com.newsaggregator.infrastructure.adapter.persistence.repository;

import com.newsaggregator.infrastructure.adapter.persistence.entity.FeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository für FeedJpaEntity.
 */
@Repository
public interface FeedJpaRepository extends JpaRepository<FeedJpaEntity, Long> {

    /**
     * Sucht einen Feed anhand seiner URL.
     *
     * @param url Die URL des Feeds
     * @return Optional mit dem Feed
     */
    Optional<FeedJpaEntity> findByUrl(String url);

    /**
     * Prüft, ob ein Feed mit dieser URL existiert.
     *
     * @param url Die zu prüfende URL
     * @return true, falls existiert
     */
    boolean existsByUrl(String url);
}
