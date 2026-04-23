package com.newsaggregator.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CryptoPrice {
    private final UUID id;
    private final String coinId;
    private final String symbol;
    private final String name;
    private final BigDecimal priceUsd;
    private final BigDecimal priceEur;
    private final BigDecimal priceChange24h;
    private final BigDecimal priceChangePercentage24h;
    private final Long marketCapUsd;
    private final Long volume24hUsd;
    private final LocalDateTime lastUpdated;

    public CryptoPrice(UUID id, String coinId, String symbol, String name,
                       BigDecimal priceUsd, BigDecimal priceEur,
                       BigDecimal priceChange24h, BigDecimal priceChangePercentage24h,
                       Long marketCapUsd, Long volume24hUsd,
                       LocalDateTime lastUpdated) {
        this.id = id;
        this.coinId = coinId;
        this.symbol = symbol;
        this.name = name;
        this.priceUsd = priceUsd;
        this.priceEur = priceEur;
        this.priceChange24h = priceChange24h;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.marketCapUsd = marketCapUsd;
        this.volume24hUsd = volume24hUsd;
        this.lastUpdated = lastUpdated;
    }

    public static CryptoPrice create(String coinId, String symbol, String name,
                                      BigDecimal priceUsd, BigDecimal priceEur,
                                      BigDecimal priceChange24h, BigDecimal priceChangePercentage24h,
                                      Long marketCapUsd, Long volume24hUsd) {
        return new CryptoPrice(UUID.randomUUID(), coinId, symbol, name,
                priceUsd, priceEur, priceChange24h, priceChangePercentage24h,
                marketCapUsd, volume24hUsd, LocalDateTime.now());
    }

    // Getters
    public UUID getId() { return id; }
    public String getCoinId() { return coinId; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public BigDecimal getPriceUsd() { return priceUsd; }
    public BigDecimal getPriceEur() { return priceEur; }
    public BigDecimal getPriceChange24h() { return priceChange24h; }
    public BigDecimal getPriceChangePercentage24h() { return priceChangePercentage24h; }
    public Long getMarketCapUsd() { return marketCapUsd; }
    public Long getVolume24hUsd() { return volume24hUsd; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
