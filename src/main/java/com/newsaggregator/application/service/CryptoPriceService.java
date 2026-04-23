package com.newsaggregator.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.domain.model.CryptoPrice;
import com.newsaggregator.domain.port.in.CryptoPriceRepository;

@Service
public class CryptoPriceService {

    private static final Logger log = LoggerFactory.getLogger(CryptoPriceService.class);
    private final CryptoPriceRepository cryptoPriceRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> COIN_MAP = Map.of(
        "bitcoin", "BTC",
        "ethereum", "ETH",
        "solana", "SOL",
        "ripple", "XRP"
    );

    private static final Map<String, String> COIN_NAMES = Map.of(
        "bitcoin", "Bitcoin",
        "ethereum", "Ethereum",
        "solana", "Solana", 
        "ripple", "XRP"
    );

    public CryptoPriceService(CryptoPriceRepository cryptoPriceRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.cryptoPriceRepository = cryptoPriceRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getCurrentPrices() {
        List<CryptoPrice> prices = cryptoPriceRepository.findAllCurrent();
        return Map.of(
            "prices", prices,
            "updatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }

    public Map<String, Object> getPriceHistory(String coinId, int days) {
        String url = String.format(
            "https://api.coingecko.com/api/v3/coins/%s/market_chart?vs_currency=usd&days=%d",
            coinId, days
        );
        
        List<Map<String, Object>> history = new ArrayList<>();
        
        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode prices = root.get("prices");
            
            if (prices != null && prices.isArray()) {
                for (JsonNode point : prices) {
                    long timestamp = point.get(0).asLong();
                    BigDecimal price = point.get(1).decimalValue();
                    
                    history.add(Map.of(
                        "timestamp", timestamp,
                        "price", price
                    ));
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch price history for {}", coinId, e);
        }

        return Map.of(
            "coinId", coinId,
            "symbol", COIN_MAP.getOrDefault(coinId, coinId.toUpperCase()),
            "name", COIN_NAMES.getOrDefault(coinId, coinId),
            "days", days,
            "history", history
        );
    }
}
