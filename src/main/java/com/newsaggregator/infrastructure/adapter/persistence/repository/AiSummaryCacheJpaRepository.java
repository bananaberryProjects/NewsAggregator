package com.newsaggregator.infrastructure.adapter.persistence.repository;

import com.newsaggregator.infrastructure.adapter.persistence.entity.AiSummaryCacheJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Spring Data JPA Repository fuer AiSummaryCacheJpaEntity.
 */
@Repository
public interface AiSummaryCacheJpaRepository extends JpaRepository<AiSummaryCacheJpaEntity, Long> {

    Optional<AiSummaryCacheJpaEntity> findByForDate(LocalDate forDate);

    void deleteByForDate(LocalDate forDate);
}
