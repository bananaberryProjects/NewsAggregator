package com.newsaggregator.application.dto;

import java.math.BigDecimal;

/**
 * DTO für Börsenkursdaten.
 */
public class StockDto {

    private final String symbol;
    private final String name;
    private final BigDecimal value;
    private final BigDecimal change;
    private final BigDecimal changePercent;

    public StockDto(String symbol, String name, BigDecimal value, BigDecimal change, BigDecimal changePercent) {
        this.symbol = symbol;
        this.name = name;
        this.value = value;
        this.change = change;
        this.changePercent = changePercent;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public BigDecimal getChange() {
        return change;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }
}
