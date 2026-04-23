package com.newsaggregator.infrastructure.adapter.scheduler;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.domain.model.CryptoPrice;
import com.newsaggregator.domain.port.in.CryptoPriceRepository;

public class CoinGeckoScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(CoinGeckoScheduledTask.class);
    private static final String COINGECKO_URL = 
        "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana,ripple" +
        "&vs_currencies=usd,eur" +
        "&include_24hr_change=true" +
        "&include_market_cap=true" +
        "&include_24hr_vol=true";

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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CryptoPriceRepository cryptoPriceRepository;

    public CoinGeckoScheduledTask(RestTemplate restTemplate, ObjectMapper objectMapper, CryptoPriceRepository cryptoPriceRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.cryptoPriceRepository = cryptoPriceRepository;
    }

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void fetchCurrentPrices() {
        try {
            log.info("Fetching crypto prices from CoinGecko...");
            String response = restTemplate.getForObject(COINGECKO_URL, String.class);
            JsonNode root = objectMapper.readTree(response);

            COIN_MAP.forEach((coinId, symbol) -> {
                JsonNode data = root.get(coinId);
                if (data != null) {
                    BigDecimal priceUsd = data.path("usd").decimalValue();
                    BigDecimal priceEur = data.path("eur").decimalValue();
                    BigDecimal change24h = data.path("usd_24h_change").decimalValue();
                    BigDecimal changePct = change24h.divide(priceUsd, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    Long marketCap = data.path("usd_market_cap").asLong(0);
                    Long volume24h = data.path("usd_24h_vol").asLong(0);

                    CryptoPrice price = CryptoPrice.create(
                            coinId, symbol, COIN_NAMES.get(coinId),
                            priceUsd, priceEur,
                            change24h, changePct,
                            marketCap, volume24h
                    );
                    cryptoPriceRepository.save(price);
                    log.info("Updated {} price: ${}", coinId, priceUsd);
                }
            });
        } catch (Exception e) {
            log.error("Failed to fetch crypto prices", e);
        }
    }
}
