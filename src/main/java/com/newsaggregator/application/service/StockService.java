package com.newsaggregator.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.newsaggregator.application.dto.StockDto;
import com.newsaggregator.application.dto.StockListDto;

import lombok.RequiredArgsConstructor;

/**
 * Application Service für Börsenkursdaten.
 *
 * <p>Ruft Yahoo Finance API serverseitig auf und liefert
 * die Daten an das Frontend. Vermeidet CORS-Probleme.</p>
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final RestClient restClient;

    private static final List<Map<String, String>> SYMBOLS = List.of(
            Map.of("symbol", "^GDAXI", "displayName", "DAX", "shortName", "DAX"),
            Map.of("symbol", "^GSPC", "displayName", "S&P 500", "shortName", "S&P500"),
            Map.of("symbol", "^IXIC", "displayName", "NASDAQ", "shortName", "NASDAQ")
    );

    public StockService() {
        this(RestClient.builder()
                .baseUrl("https://query1.finance.yahoo.com")
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build());
    }

    /**
     * Ruft aktuelle Kursdaten für alle konfigurierten Symbole ab.
     */
    public StockListDto fetchStockData() {
        logger.debug("Rufe Börsenkursdaten von Yahoo Finance ab");

        List<StockDto> results = new ArrayList<>();

        for (Map<String, String> symbolConfig : SYMBOLS) {
            try {
                // Kurze Pause zwischen Requests
                Thread.sleep(Duration.ofMillis(100));
                StockDto stock = fetchSingleStock(symbolConfig);
                if (stock != null) {
                    results.add(stock);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.warn("Fehler beim Abrufen von {}: {}", symbolConfig.get("symbol"), e.getMessage());
            }
        }

        logger.info("{} Börsenkurse erfolgreich abgerufen", results.size());
        return new StockListDto(results);
    }

    private StockDto fetchSingleStock(Map<String, String> config) {
        String symbol = config.get("symbol");
        String displayName = config.get("displayName");
        String shortName = config.get("shortName");

        String url = "/v8/finance/chart/{symbol}?interval=1d&range=2d";

        try {
            YahooChartResponse response = restClient.get()
                    .uri(url, symbol)
                    .retrieve()
                    .body(YahooChartResponse.class);

            if (response == null || response.chart() == null) {
                logger.warn("Leere Antwort für {}", symbol);
                return null;
            }

            if (response.chart().error() != null) {
                logger.warn("Yahoo Finance Fehler für {}: {}",
                        symbol, response.chart().error().get("description"));
                return null;
            }

            if (response.chart().result() == null || response.chart().result().isEmpty()) {
                logger.warn("Keine Daten für {}", symbol);
                return null;
            }

            YahooChartResult result = response.chart().result().get(0);
            YahooMeta meta = result.meta();

            BigDecimal currentPrice = BigDecimal.valueOf(meta.regularMarketPrice());
            BigDecimal previousClose = BigDecimal.valueOf(meta.previousClose());

            BigDecimal change = currentPrice.subtract(previousClose);
            BigDecimal changePercent = previousClose.compareTo(BigDecimal.ZERO) != 0
                    ? change.divide(previousClose, 6, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            return new StockDto(
                    shortName,
                    displayName,
                    currentPrice,
                    change.setScale(2, RoundingMode.HALF_UP),
                    changePercent.setScale(2, RoundingMode.HALF_UP)
            );

        } catch (RestClientException e) {
            logger.error("REST Fehler beim Abrufen von {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    // Record classes für Yahoo Finance API Response
    public record YahooChartResponse(YahooChart chart) {}
    public record YahooChart(List<YahooChartResult> result, Map<String, String> error) {}
    public record YahooChartResult(YahooMeta meta) {}
    public record YahooMeta(double regularMarketPrice, double previousClose, String shortName, String symbol) {}
}
