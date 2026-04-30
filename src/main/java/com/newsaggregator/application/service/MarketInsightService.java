package com.newsaggregator.application.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.application.dto.MarketCryptoDto;
import com.newsaggregator.application.dto.MarketInsightDto;
import com.newsaggregator.application.dto.MarketStockDto;
import com.newsaggregator.application.dto.StockDto;
import com.newsaggregator.domain.model.CryptoPrice;
import com.newsaggregator.domain.port.in.CryptoPriceRepository;

@Service
public class MarketInsightService {

    private static final Logger log = LoggerFactory.getLogger(MarketInsightService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final StockService stockService;
    private final CryptoPriceRepository cryptoPriceRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Value("${ollama.base-url:http://host.docker.internal:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:gpt-oss:120b-cloud}")
    private String ollamaModel;

    private record CacheEntry(MarketInsightDto data, LocalDateTime cachedAt) {}

    public MarketInsightService(StockService stockService, CryptoPriceRepository cryptoPriceRepository) {
        this.stockService = stockService;
        this.cryptoPriceRepository = cryptoPriceRepository;
        this.restTemplate = createRestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(300000);
        return new RestTemplate(factory);
    }

    public MarketInsightDto generateInsight(List<String> stockSymbols, List<String> cryptoIds) {
        String cacheKey = buildCacheKey(stockSymbols, cryptoIds);
        CacheEntry cached = cache.get(cacheKey);

        if (cached != null && cached.cachedAt.plus(CACHE_TTL).isAfter(LocalDateTime.now())) {
            log.debug("Cache hit fuer Markteinschaetzung");
            return cached.data;
        }

        List<StockDto> allStocks = stockService.fetchStockData().getStocks();
        List<StockDto> filteredStocks = allStocks.stream()
                .filter(s -> stockSymbols.contains(s.getSymbol()))
                .toList();

        List<CryptoPrice> allCryptos = cryptoPriceRepository.findAllCurrent();
        List<CryptoPrice> filteredCryptos = allCryptos.stream()
                .filter(c -> cryptoIds.contains(c.getCoinId()))
                .toList();

        String insight = generateAiInsight(filteredStocks, filteredCryptos);
        String sentiment = determineSentiment(filteredStocks, filteredCryptos);

        MarketInsightDto dto = new MarketInsightDto();
        dto.setInsight(insight);
        dto.setMarketSentiment(sentiment);
        dto.setUpdatedAt(LocalDateTime.now());
        dto.setStocks(filteredStocks.stream().map(this::toStockDto).toList());
        dto.setCryptos(filteredCryptos.stream().map(this::toCryptoDto).toList());

        cache.put(cacheKey, new CacheEntry(dto, LocalDateTime.now()));
        return dto;
    }

    private String buildCacheKey(List<String> stockSymbols, List<String> cryptoIds) {
        return String.join(",", stockSymbols) + "|" + String.join(",", cryptoIds);
    }

    private MarketStockDto toStockDto(StockDto s) {
        return new MarketStockDto(
                s.getSymbol(),
                s.getName(),
                s.getValue().doubleValue(),
                s.getChange().doubleValue(),
                s.getChangePercent().doubleValue(),
                getCurrency(s.getSymbol())
        );
    }

    private String getCurrency(String symbol) {
        return switch (symbol) {
            case "DAX" -> "€";
            case "S&P500", "NASDAQ" -> "$";
            default -> "";
        };
    }

    private MarketCryptoDto toCryptoDto(CryptoPrice c) {
        return new MarketCryptoDto(
                c.getCoinId(),
                c.getSymbol(),
                c.getName(),
                c.getPriceUsd() != null ? c.getPriceUsd().doubleValue() : 0,
                c.getPriceChangePercentage24h() != null ? c.getPriceChangePercentage24h().doubleValue() : 0
        );
    }

    private String generateAiInsight(List<StockDto> stocks, List<CryptoPrice> cryptos) {
        if (stocks.isEmpty() && cryptos.isEmpty()) {
            return "Keine Marktdaten verfuegbar.";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Du bist ein Finanzexperte. Fasse die aktuelle Marktlage in einem kurzen, prägnanten Satz zusammen (maximal 15 Wörter). ");

        if (!stocks.isEmpty()) {
            prompt.append("Aktien: ");
            for (StockDto s : stocks) {
                prompt.append(String.format("%s %+.1f%%, ", s.getName(), s.getChangePercent()));
            }
        }

        if (!cryptos.isEmpty()) {
            prompt.append("Krypto: ");
            for (CryptoPrice c : cryptos) {
                double change = c.getPriceChangePercentage24h() != null ? c.getPriceChangePercentage24h().doubleValue() : 0;
                prompt.append(String.format("%s %+.1f%%, ", c.getName(), change));
            }
        }

        prompt.append("\nAntworte NUR mit einem kurzen deutschen Satz. Keine Aufzaehlung, keine Zahlen wiederholen.\n");

        try {
            String url = ollamaBaseUrl + "/api/generate";

            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0.7);
            options.put("num_predict", 1024);

            Map<String, Object> body = new HashMap<>();
            body.put("model", ollamaModel);
            body.put("prompt", prompt.toString());
            body.put("stream", false);
            body.put("options", options);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String raw = restTemplate.postForObject(url, entity, String.class);
            if (raw == null) {
                return "KI-Einschaetzung nicht verfuegbar.";
            }

            JsonNode root = objectMapper.readTree(raw);
            String response = root.path("response").asText().trim();
            if (response.isEmpty()) {
                return "KI-Einschaetzung nicht verfuegbar.";
            }

            return response;

        } catch (Exception e) {
            log.error("Fehler bei AI-Insight fuer Markt: {}", e.getMessage());
            return "KI-Einschaetzung nicht verfuegbar.";
        }
    }

    private String determineSentiment(List<StockDto> stocks, List<CryptoPrice> cryptos) {
        if (stocks.isEmpty() && cryptos.isEmpty()) {
            return "neutral";
        }

        double sum = 0;
        int count = 0;

        for (StockDto s : stocks) {
            sum += s.getChangePercent().doubleValue();
            count++;
        }

        for (CryptoPrice c : cryptos) {
            double change = c.getPriceChangePercentage24h() != null ? c.getPriceChangePercentage24h().doubleValue() : 0;
            sum += change;
            count++;
        }

        double avg = sum / count;

        if (avg >= 1.0) return "bullish";
        if (avg <= -1.0) return "bearish";
        return "neutral";
    }
}
