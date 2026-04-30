package com.newsaggregator.infrastructure.adapter.persistence.repository;

import com.newsaggregator.infrastructure.adapter.persistence.entity.WeatherInsightCacheJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository fuer KI-Wetter-Insight-Cache.
 */
@Repository
public interface WeatherInsightCacheJpaRepository extends JpaRepository<WeatherInsightCacheJpaEntity, Long> {

    Optional<WeatherInsightCacheJpaEntity> findByLocationKey(String locationKey);

    void deleteByLocationKey(String locationKey);
}
