package com.newsaggregator.application.dto;

/**
 * DTO für Börsenkursdaten im Market-Widget.
 */
public class MarketStockDto {

    private String symbol;
    private String name;
    private double value;
    private double change;
    private double changePercent;
    private String currency;

    public MarketStockDto() {
    }

    public MarketStockDto(String symbol, String name, double value, double change, double changePercent, String currency) {
        this.symbol = symbol;
        this.name = name;
        this.value = value;
        this.change = change;
        this.changePercent = changePercent;
        this.currency = currency;
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

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
