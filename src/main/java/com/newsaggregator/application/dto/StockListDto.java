package com.newsaggregator.application.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO für eine Liste von Börsenkursen.
 */
public class StockListDto {

    private List<StockDto> stocks;

    // No-arg constructor for Jackson deserialization
    public StockListDto() {
        this.stocks = new ArrayList<>();
    }

    public StockListDto(List<StockDto> stocks) {
        this.stocks = stocks;
    }

    public List<StockDto> getStocks() {
        return stocks;
    }

    // Setter required for Jackson deserialization
    public void setStocks(List<StockDto> stocks) {
        this.stocks = stocks;
    }
}
