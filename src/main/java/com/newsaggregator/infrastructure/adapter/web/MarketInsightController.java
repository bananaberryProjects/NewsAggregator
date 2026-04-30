package com.newsaggregator.infrastructure.adapter.web;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newsaggregator.application.dto.MarketInsightDto;
import com.newsaggregator.application.service.MarketInsightService;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = {"http://localhost:5173", "http://mac-mini.local:5173"})
public class MarketInsightController {

    private static final Logger logger = LoggerFactory.getLogger(MarketInsightController.class);

    private final MarketInsightService marketInsightService;

    public MarketInsightController(MarketInsightService marketInsightService) {
        this.marketInsightService = marketInsightService;
    }

    @GetMapping("/insight")
    public ResponseEntity<MarketInsightDto> getInsight(
            @RequestParam(defaultValue = "DAX,S&P500,NASDAQ") String stockSymbols,
            @RequestParam(defaultValue = "bitcoin,ethereum") String cryptoIds) {

        logger.debug("GET /api/market/insight?s={}", stockSymbols);

        List<String> stocks = Arrays.asList(stockSymbols.split(","));
        List<String> cryptos = Arrays.asList(cryptoIds.split(","));

        try {
            MarketInsightDto dto = marketInsightService.generateInsight(stocks, cryptos);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen der Markteinschaetzung: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
