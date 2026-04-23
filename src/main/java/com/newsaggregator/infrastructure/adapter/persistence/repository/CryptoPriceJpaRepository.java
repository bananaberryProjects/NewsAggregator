package com.newsaggregator.infrastructure.adapter.persistence.repository;

import com.newsaggregator.infrastructure.adapter.persistence.entity.CryptoPriceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CryptoPriceJpaRepository extends JpaRepository<CryptoPriceJpaEntity, UUID> {
    Optional<CryptoPriceJpaEntity> findByCoinId(String coinId);
}
