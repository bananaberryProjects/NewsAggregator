package com.newsaggregator.infrastructure.adapter.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.newsaggregator.domain.model.CryptoPrice;
import com.newsaggregator.domain.port.in.CryptoPriceRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.CryptoPriceJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.CryptoPriceJpaMapper;
import com.newsaggregator.infrastructure.adapter.persistence.repository.CryptoPriceJpaRepository;

public class CryptoPriceRepositoryAdapter implements CryptoPriceRepository {

    private final CryptoPriceJpaRepository jpaRepository;

    public CryptoPriceRepositoryAdapter(CryptoPriceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CryptoPrice save(CryptoPrice price) {
        // Try to find existing entity to update, otherwise create new
        CryptoPriceJpaEntity entity = jpaRepository.findByCoinId(price.getCoinId())
                .orElseGet(CryptoPriceJpaEntity::new);

        entity.setId(entity.getId() != null ? entity.getId() : price.getId());
        entity.setCoinId(price.getCoinId());
        entity.setSymbol(price.getSymbol());
        entity.setName(price.getName());
        entity.setPriceUsd(price.getPriceUsd());
        entity.setPriceEur(price.getPriceEur());
        entity.setPriceChange24h(price.getPriceChange24h());
        entity.setPriceChangePercentage24h(price.getPriceChangePercentage24h());
        entity.setMarketCapUsd(price.getMarketCapUsd());
        entity.setVolume24hUsd(price.getVolume24hUsd());
        entity.setLastUpdated(price.getLastUpdated());
        entity.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : price.getLastUpdated());

        jpaRepository.save(entity);
        return price;
    }

    @Override
    public List<CryptoPrice> findAllCurrent() {
        return jpaRepository.findAll()
                .stream()
                .map(CryptoPriceJpaMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CryptoPrice> findByCoinId(String coinId) {
        return jpaRepository.findByCoinId(coinId)
                .map(CryptoPriceJpaMapper::toDomain);
    }
}
