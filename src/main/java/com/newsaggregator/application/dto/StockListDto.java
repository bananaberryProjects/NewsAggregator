package com.newsaggregator.application.dto;

import java.util.List;

/**
 * DTO für eine Liste von Börsenkursen.
 */
public class StockListDto {

    private final List<StockDto> stocks;

    public StockListDto(List<StockDto> stocks) {
        this.stocks = stocks;
    }

    public List<StockDto> getStocks() {
        return stocks;
    }
}
