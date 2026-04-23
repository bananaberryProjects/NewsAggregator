package com.newsaggregator.infrastructure.adapter.persistence.mapper;

import com.newsaggregator.domain.model.CryptoPrice;
import com.newsaggregator.infrastructure.adapter.persistence.entity.CryptoPriceJpaEntity;

public class CryptoPriceJpaMapper {

    public static CryptoPriceJpaEntity toJpa(CryptoPrice domain) {
        CryptoPriceJpaEntity entity = new CryptoPriceJpaEntity();
        entity.setId(domain.getId());
        entity.setCoinId(domain.getCoinId());
        entity.setSymbol(domain.getSymbol());
        entity.setName(domain.getName());
        entity.setPriceUsd(domain.getPriceUsd());
        entity.setPriceEur(domain.getPriceEur());
        entity.setPriceChange24h(domain.getPriceChange24h());
        entity.setPriceChangePercentage24h(domain.getPriceChangePercentage24h());
        entity.setMarketCapUsd(domain.getMarketCapUsd());
        entity.setVolume24hUsd(domain.getVolume24hUsd());
        entity.setLastUpdated(domain.getLastUpdated());
        entity.setCreatedAt(domain.getLastUpdated());
        return entity;
    }

    public static CryptoPrice toDomain(CryptoPriceJpaEntity entity) {
        return new CryptoPrice(
                entity.getId(),
                entity.getCoinId(),
                entity.getSymbol(),
                entity.getName(),
                entity.getPriceUsd(),
                entity.getPriceEur(),
                entity.getPriceChange24h(),
                entity.getPriceChangePercentage24h(),
                entity.getMarketCapUsd(),
                entity.getVolume24hUsd(),
                entity.getLastUpdated()
        );
    }
}
