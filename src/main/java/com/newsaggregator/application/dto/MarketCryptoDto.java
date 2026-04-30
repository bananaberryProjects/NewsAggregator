package com.newsaggregator.application.dto;

/**
 * DTO für Krypto-Preise im Market-Widget.
 */
public class MarketCryptoDto {

    private String coinId;
    private String symbol;
    private String name;
    private double priceUsd;
    private double priceChangePercentage24h;

    public MarketCryptoDto() {
    }

    public MarketCryptoDto(String coinId, String symbol, String name, double priceUsd, double priceChangePercentage24h) {
        this.coinId = coinId;
        this.symbol = symbol;
        this.name = name;
        this.priceUsd = priceUsd;
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPriceUsd() {
        return priceUsd;
    }

    public void setPriceUsd(double priceUsd) {
        this.priceUsd = priceUsd;
    }

    public double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public void setPriceChangePercentage24h(double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }
}
