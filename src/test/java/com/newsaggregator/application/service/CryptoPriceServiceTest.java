package com.newsaggregator.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.domain.model.CryptoPrice;
import com.newsaggregator.domain.port.in.CryptoPriceRepository;

@ExtendWith(MockitoExtension.class)
class CryptoPriceServiceTest {

    @Mock
    private CryptoPriceRepository cryptoPriceRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private CryptoPriceService cryptoPriceService;

    @BeforeEach
    void setUp() {
        cryptoPriceService = new CryptoPriceService(cryptoPriceRepository, restTemplate, objectMapper);
    }

    @Test
    void getCurrentPrices_ShouldReturnPricesWithTimestamp() {
        // Given
        CryptoPrice bitcoin = CryptoPrice.create(
                "bitcoin", "BTC", "Bitcoin",
                new BigDecimal("65000.00"), new BigDecimal("60000.00"),
                new BigDecimal("500.00"), new BigDecimal("0.77"),
                1200000000000L, 30000000000L
        );
        CryptoPrice ethereum = CryptoPrice.create(
                "ethereum", "ETH", "Ethereum",
                new BigDecimal("3500.00"), new BigDecimal("3200.00"),
                new BigDecimal("-50.00"), new BigDecimal("-1.41"),
                400000000000L, 15000000000L
        );
        List<CryptoPrice> prices = List.of(bitcoin, ethereum);

        when(cryptoPriceRepository.findAllCurrent()).thenReturn(prices);

        // When
        Map<String, Object> result = cryptoPriceService.getCurrentPrices();

        // Then
        assertThat(result).containsKey("prices");
        assertThat(result).containsKey("updatedAt");
        assertThat(result.get("prices")).isEqualTo(prices);
        assertThat(result.get("updatedAt")).isNotNull();

        String updatedAt = (String) result.get("updatedAt");
        assertThat(LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME)).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void getCurrentPrices_WithEmptyList_ShouldReturnEmptyPrices() {
        // Given
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of());

        // When
        Map<String, Object> result = cryptoPriceService.getCurrentPrices();

        // Then
        assertThat(result.get("prices")).asList().isEmpty();
        assertThat(result).containsKey("updatedAt");
    }

    @Test
    void getPriceHistory_ShouldFetchAndParseDataFromCoinGecko() throws Exception {
        // Given
        String coinId = "bitcoin";
        int days = 7;
        String mockResponse = "{\"prices\":[[1713744000000,64000.00],[1713830400000,65000.00]]}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        Map<String, Object> result = cryptoPriceService.getPriceHistory(coinId, days);

        // Then
        assertThat(result.get("coinId")).isEqualTo(coinId);
        assertThat(result.get("symbol")).isEqualTo("BTC");
        assertThat(result.get("name")).isEqualTo("Bitcoin");
        assertThat(result.get("days")).isEqualTo(days);
        assertThat(result).containsKey("history");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> history = (List<Map<String, Object>>) result.get("history");
        assertThat(history).hasSize(2);
        assertThat(history.get(0).get("timestamp")).isEqualTo(1713744000000L);
        assertThat((BigDecimal) history.get(0).get("price")).isEqualByComparingTo(new BigDecimal("64000.00"));
        assertThat(history.get(1).get("timestamp")).isEqualTo(1713830400000L);
        assertThat((BigDecimal) history.get(1).get("price")).isEqualByComparingTo(new BigDecimal("65000.00"));

        String expectedUrl = String.format(
                "https://api.coingecko.com/api/v3/coins/%s/market_chart?vs_currency=usd&days=%d",
                coinId, days
        );
        verify(restTemplate).getForObject(expectedUrl, String.class);
    }

    @Test
    void getPriceHistory_ForEthereum_ShouldReturnCorrectSymbolAndName() throws Exception {
        // Given
        String coinId = "ethereum";
        int days = 30;
        String mockResponse = "{\"prices\":[[1713744000000,3500.00]]}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        Map<String, Object> result = cryptoPriceService.getPriceHistory(coinId, days);

        // Then
        assertThat(result.get("coinId")).isEqualTo(coinId);
        assertThat(result.get("symbol")).isEqualTo("ETH");
        assertThat(result.get("name")).isEqualTo("Ethereum");
    }

    @Test
    void getPriceHistory_ForUnknownCoin_ShouldUseDefaults() throws Exception {
        // Given
        String coinId = "unknowncoin";
        int days = 7;
        String mockResponse = "{\"prices\":[[1713744000000,1.00]]}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        Map<String, Object> result = cryptoPriceService.getPriceHistory(coinId, days);

        // Then
        assertThat(result.get("coinId")).isEqualTo(coinId);
        assertThat(result.get("symbol")).isEqualTo("UNKNOWNCOIN");
        assertThat(result.get("name")).isEqualTo(coinId);
    }

    @Test
    void getPriceHistory_WhenApiThrowsException_ShouldReturnEmptyHistory() throws Exception {
        // Given
        String coinId = "bitcoin";
        int days = 7;

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When
        Map<String, Object> result = cryptoPriceService.getPriceHistory(coinId, days);

        // Then
        assertThat(result.get("coinId")).isEqualTo(coinId);
        assertThat(result.get("symbol")).isEqualTo("BTC");
        assertThat(result.get("name")).isEqualTo("Bitcoin");
        assertThat(result.get("days")).isEqualTo(days);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> history = (List<Map<String, Object>>) result.get("history");
        assertThat(history).isEmpty();
    }

    @Test
    void getPriceHistory_WhenResponseHasNoPrices_ShouldReturnEmptyHistory() throws Exception {
        // Given
        String coinId = "bitcoin";
        int days = 7;
        String mockResponse = "{\"market_caps\":[],\"total_volumes\":[]}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        Map<String, Object> result = cryptoPriceService.getPriceHistory(coinId, days);

        // Then
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> history = (List<Map<String, Object>>) result.get("history");
        assertThat(history).isEmpty();
    }

    @Test
    void getPriceHistory_ForSolana_ShouldReturnCorrectData() throws Exception {
        // Given
        String coinId = "solana";
        int days = 7;
        String mockResponse = "{\"prices\":[[1713744000000,150.00]]}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        Map<String, Object> result = cryptoPriceService.getPriceHistory(coinId, days);

        // Then
        assertThat(result.get("coinId")).isEqualTo(coinId);
        assertThat(result.get("symbol")).isEqualTo("SOL");
        assertThat(result.get("name")).isEqualTo("Solana");
    }

    @Test
    void getPriceHistory_ForRipple_ShouldReturnCorrectData() throws Exception {
        // Given
        String coinId = "ripple";
        int days = 7;
        String mockResponse = "{\"prices\":[[1713744000000,0.60]]}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        Map<String, Object> result = cryptoPriceService.getPriceHistory(coinId, days);

        // Then
        assertThat(result.get("coinId")).isEqualTo(coinId);
        assertThat(result.get("symbol")).isEqualTo("XRP");
        assertThat(result.get("name")).isEqualTo("XRP");
    }
}
