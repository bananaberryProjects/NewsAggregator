package com.newsaggregator.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für kombinierte Markteinschätzung (Börse + Krypto) mit KI-Einblick.
 */
public class MarketInsightDto {

    private String insight;
    private String marketSentiment; // bullish | bearish | neutral
    private LocalDateTime updatedAt;
    private List<MarketStockDto> stocks;
    private List<MarketCryptoDto> cryptos;

    public MarketInsightDto() {
    }

    public MarketInsightDto(String insight, String marketSentiment, LocalDateTime updatedAt,
                            List<MarketStockDto> stocks, List<MarketCryptoDto> cryptos) {
        this.insight = insight;
        this.marketSentiment = marketSentiment;
        this.updatedAt = updatedAt;
        this.stocks = stocks;
        this.cryptos = cryptos;
    }

    public String getInsight() {
        return insight;
    }

    public void setInsight(String insight) {
        this.insight = insight;
    }

    public String getMarketSentiment() {
        return marketSentiment;
    }

    public void setMarketSentiment(String marketSentiment) {
        this.marketSentiment = marketSentiment;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MarketStockDto> getStocks() {
        return stocks;
    }

    public void setStocks(List<MarketStockDto> stocks) {
        this.stocks = stocks;
    }

    public List<MarketCryptoDto> getCryptos() {
        return cryptos;
    }

    public void setCryptos(List<MarketCryptoDto> cryptos) {
        this.cryptos = cryptos;
    }
}
