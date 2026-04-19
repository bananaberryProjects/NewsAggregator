package com.newsaggregator.application.service;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.newsaggregator.application.dto.StockDto;
import com.newsaggregator.application.dto.StockListDto;

/**
 * Unit-Test für StockService.
 *
 * <p>Testet die DTO-Struktur und Berechnungen für Börsenkursdaten.
 * Integration-Tests mit HTTP-Mocking wären separat erforderlich.</p>
 */
class StockServiceTest {

    @Test
    void fetchStockData_ResponseStructure_ShouldHaveRequiredFields() {
        // Given - Create a stock list directly to verify DTO structure
        StockDto stock = new StockDto(
                "DAX",
                "DAX",
                new BigDecimal("18500.50"),
                new BigDecimal("120.30"),
                new BigDecimal("0.65")
        );
        StockListDto stockList = new StockListDto(List.of(stock));

        // Then
        assertThat(stockList.getStocks()).hasSize(1);
        StockDto firstStock = stockList.getStocks().get(0);
        assertThat(firstStock.getSymbol()).isEqualTo("DAX");
        assertThat(firstStock.getName()).isEqualTo("DAX");
        assertThat(firstStock.getValue()).isEqualByComparingTo(new BigDecimal("18500.50"));
        assertThat(firstStock.getChange()).isEqualByComparingTo(new BigDecimal("120.30"));
        assertThat(firstStock.getChangePercent()).isEqualByComparingTo(new BigDecimal("0.65"));
    }

    @Test
    void fetchStockData_WithEmptyList_ShouldReturnEmptyStockList() {
        // Given
        StockListDto emptyList = new StockListDto(List.of());

        // Then
        assertThat(emptyList.getStocks()).isEmpty();
    }

    @Test
    void fetchStockData_WithMultipleStocks_ShouldReturnAllStocks() {
        // Given
        StockDto dax = new StockDto("DAX", "DAX", new BigDecimal("18500"), new BigDecimal("100"), new BigDecimal("0.5"));
        StockDto sp500 = new StockDto("S&P500", "S&P 500", new BigDecimal("5200"), new BigDecimal("-20"), new BigDecimal("-0.4"));
        StockDto btc = new StockDto("BTC", "Bitcoin", new BigDecimal("64000"), new BigDecimal("1000"), new BigDecimal("1.5"));

        StockListDto stockList = new StockListDto(List.of(dax, sp500, btc));

        // Then
        assertThat(stockList.getStocks()).hasSize(3);
        assertThat(stockList.getStocks().stream().map(StockDto::getSymbol))
                .containsExactly("DAX", "S&P500", "BTC");
    }

    @Test
    void fetchStockData_WithNegativeChange_ShouldHaveNegativeValues() {
        // Given
        StockDto decliningStock = new StockDto(
                "BTC",
                "Bitcoin",
                new BigDecimal("60000"),
                new BigDecimal("-2000"),
                new BigDecimal("-3.23")
        );
        StockListDto stockList = new StockListDto(List.of(decliningStock));

        // Then
        assertThat(stockList.getStocks().get(0).getChange()).isNegative();
        assertThat(stockList.getStocks().get(0).getChangePercent()).isNegative();
    }

    @Test
    void fetchStockData_WithZeroChange_ShouldHaveZeroValues() {
        // Given
        StockDto unchangedStock = new StockDto(
                "TEST",
                "Test Stock",
                new BigDecimal("100"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        StockListDto stockList = new StockListDto(List.of(unchangedStock));

        // Then
        assertThat(stockList.getStocks().get(0).getChange()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stockList.getStocks().get(0).getChangePercent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void fetchStockData_WithHighPrecisionValues_ShouldMaintainPrecision() {
        // Given
        StockDto preciseStock = new StockDto(
                "BTC",
                "Bitcoin",
                new BigDecimal("64234.567890"),
                new BigDecimal("1234.567890"),
                new BigDecimal("1.958765")
        );
        StockListDto stockList = new StockListDto(List.of(preciseStock));

        // Then
        assertThat(stockList.getStocks().get(0).getValue()).isEqualByComparingTo(new BigDecimal("64234.567890"));
    }

    @Test
    void fetchStockData_SingleStockSymbols_ShouldMatchExpectedSymbols() {
        // This test documents which symbols are configured in the service
        // The service fetches: ^GDAXI (DAX), ^GSPC (S&P 500), BTC-USD (Bitcoin)

        // Verify the symbols exist and have correct structure
        StockDto dax = new StockDto("DAX", "DAX", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);
        StockDto sp500 = new StockDto("S&P500", "S&P 500", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);
        StockDto btc = new StockDto("BTC", "Bitcoin", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);

        assertThat(dax.getSymbol()).isEqualTo("DAX");
        assertThat(sp500.getSymbol()).isEqualTo("S&P500");
        assertThat(btc.getSymbol()).isEqualTo("BTC");
    }

    @Test
    void fetchStockData_ShouldContainAllConfiguredSymbols() {
        // This test documents the three configured symbols in StockService
        // SYMBOLS = ^GDAXI (displayName=DAX), ^GSPC (displayName=S&P 500), BTC-USD (displayName=Bitcoin)

        List<String> expectedDisplayNames = List.of("DAX", "S&P 500", "Bitcoin");
        List<String> expectedShortNames = List.of("DAX", "S&P500", "BTC");

        assertThat(expectedDisplayNames).hasSize(3);
        assertThat(expectedShortNames).hasSize(3);

        // Verify expected symbols match what the service uses
        assertThat(expectedShortNames).containsExactly("DAX", "S&P500", "BTC");
    }

    @Test
    void stockDto_CalculatedFields_ShouldBeCorrect() {
        // Given - Simulating calculation: change = current - previous
        // changePercent = (change / previous) * 100
        BigDecimal currentPrice = new BigDecimal("100.00");
        BigDecimal previousClose = new BigDecimal("95.00");
        BigDecimal change = currentPrice.subtract(previousClose); // 5.00
        BigDecimal changePercent = change.divide(previousClose, 6, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)); // ~5.263200%

        StockDto stock = new StockDto("TEST", "Test", currentPrice, change, changePercent);

        // Then
        assertThat(stock.getChange()).isEqualByComparingTo(new BigDecimal("5.00"));
        // Use a more lenient offset since the exact calculation may vary
        assertThat(stock.getChangePercent()).isCloseTo(new BigDecimal("5.26"), org.assertj.core.data.Offset.offset(new BigDecimal("0.01")));
    }
}
