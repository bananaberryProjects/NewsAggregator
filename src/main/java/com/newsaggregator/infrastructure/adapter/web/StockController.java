package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.StockListDto;
import com.newsaggregator.application.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller für Börsenkursdaten.
 *
 * <p>Proxy für Yahoo Finance API. Ruft die Daten serverseitig ab
 * und liefert sie an das Frontend. Vermeidet CORS-Probleme.</p>
 */
@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private static final Logger logger = LoggerFactory.getLogger(StockController.class);

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * Gibt aktuelle Börsenkursdaten zurück.
     *
     * <p>Datenquelle: Yahoo Finance API (serverseitiger Aufruf)</p>
     */
    @GetMapping
    public ResponseEntity<StockListDto> getStocks() {
        logger.debug("GET /api/stocks aufgerufen");

        try {
            StockListDto stocks = stockService.fetchStockData();

            if (stocks.getStocks().isEmpty()) {
                logger.warn("Keine Börsenkursdaten verfügbar");
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen der Börsenkurse: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
