package com.newsaggregator.infrastructure.adapter.web;

import java.math.BigDecimal;
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
import com.newsaggregator.application.dto.StockDto;
import com.newsaggregator.application.dto.StockListDto;
import com.newsaggregator.application.service.StockService;

/**
 * Unit-Test für StockController.
 *
 * <p>Testet die REST-Endpunkte für Börsenkursdaten.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private StockService stockService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        StockController controller = new StockController(stockService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getStocks_WithData_ShouldReturnOkWithStocks() throws Exception {
        // Given
        StockDto dax = new StockDto("DAX", "DAX", new BigDecimal("18500.50"), new BigDecimal("120.30"), new BigDecimal("0.65"));
        StockDto sp500 = new StockDto("S&P500", "S&P 500", new BigDecimal("5200.25"), new BigDecimal("-15.40"), new BigDecimal("-0.29"));
        StockDto btc = new StockDto("BTC", "Bitcoin", new BigDecimal("64200.00"), new BigDecimal("1500.00"), new BigDecimal("2.40"));
        StockListDto stockList = new StockListDto(List.of(dax, sp500, btc));

        when(stockService.fetchStockData()).thenReturn(stockList);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/stocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        StockListDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                StockListDto.class);
        assertThat(responseBody.getStocks()).hasSize(3);

        // Verify first stock (DAX)
        assertThat(responseBody.getStocks().get(0).getSymbol()).isEqualTo("DAX");
        assertThat(responseBody.getStocks().get(0).getName()).isEqualTo("DAX");
        assertThat(responseBody.getStocks().get(0).getValue()).isEqualByComparingTo(new BigDecimal("18500.50"));
        assertThat(responseBody.getStocks().get(0).getChange()).isEqualByComparingTo(new BigDecimal("120.30"));
        assertThat(responseBody.getStocks().get(0).getChangePercent()).isEqualByComparingTo(new BigDecimal("0.65"));

        // Verify second stock (S&P 500)
        assertThat(responseBody.getStocks().get(1).getSymbol()).isEqualTo("S&P500");
        assertThat(responseBody.getStocks().get(1).getName()).isEqualTo("S&P 500");
        assertThat(responseBody.getStocks().get(1).getValue()).isEqualByComparingTo(new BigDecimal("5200.25"));

        // Verify third stock (BTC)
        assertThat(responseBody.getStocks().get(2).getSymbol()).isEqualTo("BTC");
        assertThat(responseBody.getStocks().get(2).getName()).isEqualTo("Bitcoin");
    }

    @Test
    void getStocks_WithEmptyList_ShouldReturnNoContent() throws Exception {
        // Given
        StockListDto emptyStockList = new StockListDto(List.of());
        when(stockService.fetchStockData()).thenReturn(emptyStockList);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/stocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(204);
        assertThat(result.getResponse().getContentAsString()).isEmpty();
    }

    @Test
    void getStocks_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(stockService.fetchStockData()).thenThrow(new RuntimeException("Yahoo Finance API unavailable"));

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/stocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(500);
        assertThat(result.getResponse().getContentAsString()).isEmpty();
    }

    @Test
    void getStocks_WithSingleStock_ShouldReturnOkWithOneStock() throws Exception {
        // Given
        StockDto singleStock = new StockDto("DAX", "DAX", new BigDecimal("18500.50"), new BigDecimal("0"), new BigDecimal("0"));
        StockListDto stockList = new StockListDto(List.of(singleStock));

        when(stockService.fetchStockData()).thenReturn(stockList);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/stocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        StockListDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                StockListDto.class);
        assertThat(responseBody.getStocks()).hasSize(1);
        assertThat(responseBody.getStocks().get(0).getSymbol()).isEqualTo("DAX");
    }

    @Test
    void getStocks_WithNegativeChange_ShouldReturnCorrectNegativeValues() throws Exception {
        // Given
        StockDto decliningStock = new StockDto(
                "BTC",
                "Bitcoin",
                new BigDecimal("60000.00"),
                new BigDecimal("-2000.00"),
                new BigDecimal("-3.23")
        );
        StockListDto stockList = new StockListDto(List.of(decliningStock));

        when(stockService.fetchStockData()).thenReturn(stockList);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/stocks")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        StockListDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                StockListDto.class);
        assertThat(responseBody.getStocks()).hasSize(1);
        assertThat(responseBody.getStocks().get(0).getChange()).isNegative();
        assertThat(responseBody.getStocks().get(0).getChangePercent()).isNegative();
        assertThat(responseBody.getStocks().get(0).getChange()).isEqualByComparingTo(new BigDecimal("-2000.00"));
        assertThat(responseBody.getStocks().get(0).getChangePercent()).isEqualByComparingTo(new BigDecimal("-3.23"));
    }
}
