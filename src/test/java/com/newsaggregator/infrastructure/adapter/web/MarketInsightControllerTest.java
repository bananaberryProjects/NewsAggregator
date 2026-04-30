package com.newsaggregator.infrastructure.adapter.web;

import java.time.LocalDateTime;
import java.util.List;

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
import com.newsaggregator.application.dto.MarketCryptoDto;
import com.newsaggregator.application.dto.MarketInsightDto;
import com.newsaggregator.application.dto.MarketStockDto;
import com.newsaggregator.application.service.MarketInsightService;

/**
 * Unit-Test für MarketInsightController.
 *
 * <p>Testet den REST-Endpunkt für Markteinschätzungen.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class MarketInsightControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MarketInsightService marketInsightService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MarketInsightController controller = new MarketInsightController(marketInsightService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void getInsight_WithDefaultParameters_ShouldReturnMarketInsight() throws Exception {
        // Given
        MarketInsightDto insight = createMarketInsightDto();

        when(marketInsightService.generateInsight(
                List.of("DAX", "S&P500", "NASDAQ"),
                List.of("bitcoin", "ethereum")
        )).thenReturn(insight);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/market/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        MarketInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketInsightDto.class);
        assertThat(responseBody.getInsight()).isEqualTo("Markt zeigt positive Entwicklung");
        assertThat(responseBody.getMarketSentiment()).isEqualTo("bullish");
        assertThat(responseBody.getStocks()).hasSize(2);
        assertThat(responseBody.getCryptos()).hasSize(2);
    }

    @Test
    void getInsight_WithCustomParameters_ShouldReturnFilteredInsight() throws Exception {
        // Given
        MarketInsightDto insight = new MarketInsightDto(
                "Nur DAX und Bitcoin",
                "neutral",
                LocalDateTime.now(),
                List.of(),
                List.of()
        );

        when(marketInsightService.generateInsight(
                List.of("DAX"),
                List.of("bitcoin")
        )).thenReturn(insight);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/market/insight")
                .param("stockSymbols", "DAX")
                .param("cryptoIds", "bitcoin")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        MarketInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketInsightDto.class);
        assertThat(responseBody.getInsight()).isEqualTo("Nur DAX und Bitcoin");
    }

    @Test
    void getInsight_WithBullishSentiment_ShouldReturnBullish() throws Exception {
        // Given
        MarketInsightDto insight = new MarketInsightDto(
                "Markt ist stark",
                "bullish",
                LocalDateTime.now(),
                List.of(),
                List.of()
        );

        when(marketInsightService.generateInsight(
                List.of("DAX", "S&P500", "NASDAQ"),
                List.of("bitcoin", "ethereum")
        )).thenReturn(insight);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/market/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        MarketInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketInsightDto.class);
        assertThat(responseBody.getMarketSentiment()).isEqualTo("bullish");
    }

    @Test
    void getInsight_WithBearishSentiment_ShouldReturnBearish() throws Exception {
        // Given
        MarketInsightDto insight = new MarketInsightDto(
                "Markt ist schwach",
                "bearish",
                LocalDateTime.now(),
                List.of(),
                List.of()
        );

        when(marketInsightService.generateInsight(
                List.of("DAX", "S&P500", "NASDAQ"),
                List.of("bitcoin", "ethereum")
        )).thenReturn(insight);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/market/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        MarketInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketInsightDto.class);
        assertThat(responseBody.getMarketSentiment()).isEqualTo("bearish");
    }

    @Test
    void getInsight_WithOnlyCryptoRequested_ShouldReturnOnlyCryptoData() throws Exception {
        // Given - Request only specific crypto
        MarketInsightDto insight = new MarketInsightDto(
                "Bitcoin Daten",
                "bullish",
                LocalDateTime.now(),
                List.of(),
                List.of(new MarketCryptoDto("bitcoin", "BTC", "Bitcoin", 64000.0, 2.5))
        );

        when(marketInsightService.generateInsight(
                List.of("DAX", "S&P500", "NASDAQ"),
                List.of("bitcoin")
        )).thenReturn(insight);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/market/insight")
                .param("cryptoIds", "bitcoin")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        MarketInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketInsightDto.class);
        assertThat(responseBody.getCryptos()).hasSize(1);
        assertThat(responseBody.getCryptos().get(0).getSymbol()).isEqualTo("BTC");
    }

    @Test
    void getInsight_WithOnlyStocksRequested_ShouldReturnOnlyStockData() throws Exception {
        // Given - Request specific stocks with empty crypto
        MarketInsightDto insight = new MarketInsightDto(
                "Nur Aktien-Daten",
                "bullish",
                LocalDateTime.now(),
                List.of(new MarketStockDto("DAX", "DAX", 18500.0, 100.0, 0.5, "€")),
                List.of()
        );

        when(marketInsightService.generateInsight(
                List.of("DAX"),
                List.of("bitcoin", "ethereum")
        )).thenReturn(insight);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/market/insight")
                .param("stockSymbols", "DAX")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        MarketInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketInsightDto.class);
        assertThat(responseBody.getStocks()).hasSize(1);
        assertThat(responseBody.getStocks().get(0).getSymbol()).isEqualTo("DAX");
    }

    @Test
    void getInsight_ResponseShouldContainCorrectStockStructure() throws Exception {
        // Given
        MarketInsightDto insight = new MarketInsightDto(
                "Test",
                "neutral",
                LocalDateTime.now(),
                List.of(new MarketStockDto("DAX", "DAX", 18500.50, 100.25, 0.54, "€")),
                List.of()
        );

        when(marketInsightService.generateInsight(
                List.of("DAX", "S&P500", "NASDAQ"),
                List.of("bitcoin", "ethereum")
        )).thenReturn(insight);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/market/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("symbol");
        assertThat(json).contains("name");
        assertThat(json).contains("value");
        assertThat(json).contains("change");
        assertThat(json).contains("changePercent");
        assertThat(json).contains("currency");
    }

    @Test
    void getInsight_ResponseShouldContainCorrectCryptoStructure() throws Exception {
        // Given
        MarketInsightDto insight = new MarketInsightDto(
                "Test",
                "neutral",
                LocalDateTime.now(),
                List.of(),
                List.of(new MarketCryptoDto("bitcoin", "BTC", "Bitcoin", 64000.0, 2.5))
        );

        when(marketInsightService.generateInsight(
                List.of("DAX", "S&P500", "NASDAQ"),
                List.of("bitcoin", "ethereum")
        )).thenReturn(insight);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/market/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("coinId");
        assertThat(json).contains("symbol");
        assertThat(json).contains("name");
        assertThat(json).contains("priceUsd");
        assertThat(json).contains("priceChangePercentage24h");
    }

    @Test
    void getInsight_ShouldIncludeTimestamp() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        MarketInsightDto insight = new MarketInsightDto(
                "Test",
                "neutral",
                now,
                List.of(),
                List.of()
        );

        when(marketInsightService.generateInsight(
                List.of("DAX", "S&P500", "NASDAQ"),
                List.of("bitcoin", "ethereum")
        )).thenReturn(insight);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/market/insight")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        MarketInsightDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), MarketInsightDto.class);
        assertThat(responseBody.getUpdatedAt()).isEqualTo(now);
    }

    private MarketInsightDto createMarketInsightDto() {
        return new MarketInsightDto(
                "Markt zeigt positive Entwicklung",
                "bullish",
                LocalDateTime.now(),
                List.of(
                        new MarketStockDto("DAX", "DAX", 18500.0, 100.0, 0.5, "€"),
                        new MarketStockDto("S&P500", "S&P 500", 5200.0, 20.0, 0.4, "$")
                ),
                List.of(
                        new MarketCryptoDto("bitcoin", "BTC", "Bitcoin", 64000.0, 2.5),
                        new MarketCryptoDto("ethereum", "ETH", "Ethereum", 3400.0, -1.2)
                )
        );
    }
}
