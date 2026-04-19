package com.newsaggregator.application.dto;

import java.math.BigDecimal;

/**
 * DTO für Börsenkursdaten.
 */
public class StockDto {

    private String symbol;
    private String name;
    private BigDecimal value;
    private BigDecimal change;
    private BigDecimal changePercent;

    // No-arg constructor for Jackson deserialization
    public StockDto() {
    }

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

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }
}
