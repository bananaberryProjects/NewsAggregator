package com.newsaggregator.infrastructure.adapter.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.application.service.CryptoPriceService;

/**
 * Unit-Test fuer CryptoPriceController.
 *
 * <p>Testet die REST-Endpunkte fuer Kryptowaehrungskurse.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class CryptoPriceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CryptoPriceService cryptoPriceService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        CryptoPriceController controller = new CryptoPriceController(cryptoPriceService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getCurrentPrices_WithData_ShouldReturnOkWithPrices() throws Exception {
        // Given
        Map<String, Object> pricesResponse = Map.of(
            "prices", List.of(
                Map.of("coinId", "bitcoin", "symbol", "BTC", "priceUsd", new BigDecimal("65000.00")),
                Map.of("coinId", "ethereum", "symbol", "ETH", "priceUsd", new BigDecimal("3500.00"))
            ),
            "updatedAt", "2026-04-23T10:00:00"
        );

        when(cryptoPriceService.getCurrentPrices()).thenReturn(pricesResponse);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/crypto/prices")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String content = result.getResponse().getContentAsString();
        Map<String, Object> responseBody = objectMapper.readValue(content, Map.class);
        assertThat(responseBody).containsKey("prices");
        assertThat(responseBody).containsKey("updatedAt");
    }

    @Test
    void getCurrentPrices_WithEmptyPrices_ShouldReturnOk() throws Exception {
        // Given
        Map<String, Object> emptyResponse = Map.of(
            "prices", List.of(),
            "updatedAt", "2026-04-23T10:00:00"
        );

        when(cryptoPriceService.getCurrentPrices()).thenReturn(emptyResponse);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/crypto/prices")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String content = result.getResponse().getContentAsString();
        Map<String, Object> responseBody = objectMapper.readValue(content, Map.class);
        @SuppressWarnings("unchecked")
        List<Object> prices = (List<Object>) responseBody.get("prices");
        assertThat(prices).isEmpty();
    }

    @Test
    void getPriceHistory_WithValidCoinId_ShouldReturnOkWithHistory() throws Exception {
        // Given
        String coinId = "bitcoin";
        int days = 7;
        Map<String, Object> historyResponse = Map.of(
            "coinId", "bitcoin",
            "symbol", "BTC",
            "name", "Bitcoin",
            "days", 7,
            "history", List.of(
                Map.of("timestamp", 1713744000000L, "price", new BigDecimal("64000.00")),
                Map.of("timestamp", 1713830400000L, "price", new BigDecimal("65000.00"))
            )
        );

        when(cryptoPriceService.getPriceHistory(coinId, days)).thenReturn(historyResponse);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/crypto/history/{coinId}", coinId)
                .param("days", String.valueOf(days))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String content = result.getResponse().getContentAsString();
        Map<String, Object> responseBody = objectMapper.readValue(content, Map.class);
        assertThat(responseBody.get("coinId")).isEqualTo("bitcoin");
        assertThat(responseBody.get("symbol")).isEqualTo("BTC");
        assertThat(responseBody.get("days")).isEqualTo(7);
    }

    @Test
    void getPriceHistory_WithDefaultDays_ShouldReturnOk() throws Exception {
        // Given
        String coinId = "ethereum";
        Map<String, Object> historyResponse = Map.of(
            "coinId", "ethereum",
            "symbol", "ETH",
            "name", "Ethereum",
            "days", 7,
            "history", List.of()
        );

        when(cryptoPriceService.getPriceHistory(coinId, 7)).thenReturn(historyResponse);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/crypto/history/{coinId}", coinId)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String content = result.getResponse().getContentAsString();
        Map<String, Object> responseBody = objectMapper.readValue(content, Map.class);
        assertThat(responseBody.get("coinId")).isEqualTo("ethereum");
    }

    @Test
    void getPriceHistory_WithCustomDays_ShouldReturnOk() throws Exception {
        // Given
        String coinId = "solana";
        int days = 30;
        Map<String, Object> historyResponse = Map.of(
            "coinId", "solana",
            "symbol", "SOL",
            "name", "Solana",
            "days", 30,
            "history", List.of(Map.of("timestamp", 1713744000000L, "price", new BigDecimal("150.00")))
        );

        when(cryptoPriceService.getPriceHistory(coinId, days)).thenReturn(historyResponse);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/crypto/history/{coinId}", coinId)
                .param("days", String.valueOf(days))
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String content = result.getResponse().getContentAsString();
        Map<String, Object> responseBody = objectMapper.readValue(content, Map.class);
        assertThat(responseBody.get("days")).isEqualTo(30);
    }

}
