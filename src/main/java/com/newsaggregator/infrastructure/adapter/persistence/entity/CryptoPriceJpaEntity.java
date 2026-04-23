package com.newsaggregator.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crypto_prices")
public class CryptoPriceJpaEntity {

    @Id
    private UUID id;

    @Column(name = "coin_id", nullable = false, unique = true)
    private String coinId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_usd", nullable = false, precision = 18, scale = 8)
    private BigDecimal priceUsd;

    @Column(name = "price_eur", precision = 18, scale = 8)
    private BigDecimal priceEur;

    @Column(name = "price_change_24h", precision = 18, scale = 8)
    private BigDecimal priceChange24h;

    @Column(name = "price_change_percentage_24h", precision = 10, scale = 4)
    private BigDecimal priceChangePercentage24h;

    @Column(name = "market_cap_usd")
    private Long marketCapUsd;

    @Column(name = "volume_24h_usd")
    private Long volume24hUsd;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCoinId() { return coinId; }
    public void setCoinId(String coinId) { this.coinId = coinId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPriceUsd() { return priceUsd; }
    public void setPriceUsd(BigDecimal priceUsd) { this.priceUsd = priceUsd; }
    public BigDecimal getPriceEur() { return priceEur; }
    public void setPriceEur(BigDecimal priceEur) { this.priceEur = priceEur; }
    public BigDecimal getPriceChange24h() { return priceChange24h; }
    public void setPriceChange24h(BigDecimal priceChange24h) { this.priceChange24h = priceChange24h; }
    public BigDecimal getPriceChangePercentage24h() { return priceChangePercentage24h; }
    public void setPriceChangePercentage24h(BigDecimal priceChangePercentage24h) { this.priceChangePercentage24h = priceChangePercentage24h; }
    public Long getMarketCapUsd() { return marketCapUsd; }
    public void setMarketCapUsd(Long marketCapUsd) { this.marketCapUsd = marketCapUsd; }
    public Long getVolume24hUsd() { return volume24hUsd; }
    public void setVolume24hUsd(Long volume24hUsd) { this.volume24hUsd = volume24hUsd; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
