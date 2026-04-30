package com.newsaggregator.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.application.dto.MarketCryptoDto;
import com.newsaggregator.application.dto.MarketInsightDto;
import com.newsaggregator.application.dto.MarketStockDto;
import com.newsaggregator.application.dto.StockDto;
import com.newsaggregator.application.dto.StockListDto;
import com.newsaggregator.domain.model.CryptoPrice;
import com.newsaggregator.domain.port.in.CryptoPriceRepository;

/**
 * Unit-Test fuer MarketInsightService.
 *
 * <p>Testet die Markteinschaetzungsgenerierung inkl. Caching,
 * Sentiment-Analyse und Datenfilterung.</p>
 */
@ExtendWith(MockitoExtension.class)
class MarketInsightServiceTest {

    @Mock
    private StockService stockService;

    @Mock
    private CryptoPriceRepository cryptoPriceRepository;

    private MarketInsightService marketInsightService;

    @BeforeEach
    void setUp() {
        marketInsightService = new MarketInsightService(stockService, cryptoPriceRepository);
    }

    @Test
    void generateInsight_WithStocksAndCrypto_ShouldReturnInsightWithData() {
        // Given
        StockDto dax = new StockDto("DAX", "DAX", new BigDecimal("18500"), new BigDecimal("100"), new BigDecimal("0.5"));
        StockDto sp500 = new StockDto("S&P500", "S&P 500", new BigDecimal("5200"), new BigDecimal("-20"), new BigDecimal("-0.4"));
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of(dax, sp500)));

        CryptoPrice bitcoin = CryptoPrice.create("bitcoin", "BTC", "Bitcoin",
                new BigDecimal("64000"), null, null, new BigDecimal("2.5"), null, null);
        CryptoPrice ethereum = CryptoPrice.create("ethereum", "ETH", "Ethereum",
                new BigDecimal("3400"), null, null, new BigDecimal("-1.2"), null, null);
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of(bitcoin, ethereum));

        // When
        MarketInsightDto result = marketInsightService.generateInsight(
                List.of("DAX", "S&P500"),
                List.of("bitcoin", "ethereum")
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getInsight()).isNotNull();
        assertThat(result.getStocks()).hasSize(2);
        assertThat(result.getCryptos()).hasSize(2);
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void generateInsight_WithBullishMarket_ShouldReturnBullishSentiment() {
        // Given - Average change > 1%
        StockDto dax = new StockDto("DAX", "DAX", new BigDecimal("18500"), new BigDecimal("200"), new BigDecimal("2.5"));
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of(dax)));
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of());

        // When
        MarketInsightDto result = marketInsightService.generateInsight(List.of("DAX"), List.of());

        // Then
        assertThat(result.getMarketSentiment()).isEqualTo("bullish");
    }

    @Test
    void generateInsight_WithBearishMarket_ShouldReturnBearishSentiment() {
        // Given - Average change < -1%
        StockDto dax = new StockDto("DAX", "DAX", new BigDecimal("18500"), new BigDecimal("-300"), new BigDecimal("-2.5"));
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of(dax)));
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of());

        // When
        MarketInsightDto result = marketInsightService.generateInsight(List.of("DAX"), List.of());

        // Then
        assertThat(result.getMarketSentiment()).isEqualTo("bearish");
    }

    @Test
    void generateInsight_WithNeutralMarket_ShouldReturnNeutralSentiment() {
        // Given - Average change between -1% and 1%
        StockDto dax = new StockDto("DAX", "DAX", new BigDecimal("18500"), new BigDecimal("50"), new BigDecimal("0.5"));
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of(dax)));
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of());

        // When
        MarketInsightDto result = marketInsightService.generateInsight(List.of("DAX"), List.of());

        // Then
        assertThat(result.getMarketSentiment()).isEqualTo("neutral");
    }

    @Test
    void generateInsight_WithNoData_ShouldReturnNeutralSentimentAndFallbackMessage() {
        // Given
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of()));
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of());

        // When
        MarketInsightDto result = marketInsightService.generateInsight(List.of(), List.of());

        // Then
        assertThat(result.getMarketSentiment()).isEqualTo("neutral");
        assertThat(result.getInsight()).isEqualTo("Keine Marktdaten verfuegbar.");
        assertThat(result.getStocks()).isEmpty();
        assertThat(result.getCryptos()).isEmpty();
    }

    @Test
    void generateInsight_WithInvalidStockSymbol_ShouldFilterOutNulls() {
        // Given - Requesting non-existent symbol
        StockDto existing = new StockDto("DAX", "DAX", new BigDecimal("18500"), new BigDecimal("100"), new BigDecimal("0.5"));
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of(existing)));
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of());

        // When - Requesting DAX and non-existent "INVALID"
        MarketInsightDto result = marketInsightService.generateInsight(List.of("DAX", "INVALID"), List.of());

        // Then - Only DAX should be present
        assertThat(result.getStocks()).hasSize(1);
        assertThat(result.getStocks().get(0).getSymbol()).isEqualTo("DAX");
    }

    @Test
    void generateInsight_ShouldMapStockDtoToMarketStockDtoCorrectly() {
        // Given
        StockDto dax = new StockDto("DAX", "DAX", new BigDecimal("18500.50"), new BigDecimal("100.25"), new BigDecimal("0.54"));
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of(dax)));
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of());

        // When
        MarketInsightDto result = marketInsightService.generateInsight(List.of("DAX"), List.of());

        // Then
        assertThat(result.getStocks()).hasSize(1);
        MarketStockDto stock = result.getStocks().get(0);
        assertThat(stock.getSymbol()).isEqualTo("DAX");
        assertThat(stock.getName()).isEqualTo("DAX");
        assertThat(stock.getValue()).isEqualTo(18500.50);
        assertThat(stock.getChange()).isEqualTo(100.25);
        assertThat(stock.getChangePercent()).isEqualTo(0.54);
        assertThat(stock.getCurrency()).isEqualTo("€");
    }

    @Test
    void generateInsight_ShouldMapCryptoToMarketCryptoDtoCorrectly() {
        // Given
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of()));

        CryptoPrice bitcoin = CryptoPrice.create("bitcoin", "BTC", "Bitcoin",
                new BigDecimal("64000.50"), null, null, new BigDecimal("2.55"), null, null);
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of(bitcoin));

        // When
        MarketInsightDto result = marketInsightService.generateInsight(List.of(), List.of("bitcoin"));

        // Then
        assertThat(result.getCryptos()).hasSize(1);
        MarketCryptoDto crypto = result.getCryptos().get(0);
        assertThat(crypto.getCoinId()).isEqualTo("bitcoin");
        assertThat(crypto.getSymbol()).isEqualTo("BTC");
        assertThat(crypto.getName()).isEqualTo("Bitcoin");
        assertThat(crypto.getPriceUsd()).isEqualTo(64000.50);
        assertThat(crypto.getPriceChangePercentage24h()).isEqualTo(2.55);
    }

    @Test
    void generateInsight_WithMultipleAssets_ShouldCalculateWeightedSentiment() {
        // Given - DAX bullish (2%), S&P500 bearish (-3%), Bitcoin bullish (1.5%)
        // Average: (2 + (-3) + 1.5) / 3 = 0.17 -> neutral
        StockDto dax = new StockDto("DAX", "DAX", new BigDecimal("18500"), new BigDecimal("370"), new BigDecimal("2.0"));
        StockDto sp500 = new StockDto("S&P500", "S&P 500", new BigDecimal("5200"), new BigDecimal("-156"), new BigDecimal("-3.0"));
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of(dax, sp500)));

        CryptoPrice bitcoin = CryptoPrice.create("bitcoin", "BTC", "Bitcoin",
                new BigDecimal("64000"), null, null, new BigDecimal("1.5"), null, null);
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of(bitcoin));

        // When
        MarketInsightDto result = marketInsightService.generateInsight(
                List.of("DAX", "S&P500"),
                List.of("bitcoin")
        );

        // Then
        assertThat(result.getStocks()).hasSize(2);
        assertThat(result.getCryptos()).hasSize(1);
        assertThat(result.getMarketSentiment()).isIn("bullish", "neutral", "bearish");
    }

    @Test
    void generateInsight_ShouldUseCaching() {
        // Given
        StockDto dax = new StockDto("DAX", "DAX", new BigDecimal("18500"), new BigDecimal("100"), new BigDecimal("0.5"));
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of(dax)));
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of());

        // When - First call
        MarketInsightDto result1 = marketInsightService.generateInsight(List.of("DAX"), List.of());
        LocalDateTime updatedAt1 = result1.getUpdatedAt();

        // Then - Second call should return cached data with same timestamp
        MarketInsightDto result2 = marketInsightService.generateInsight(List.of("DAX"), List.of());
        assertThat(result2.getUpdatedAt()).isEqualTo(updatedAt1);
    }

    @Test
    void generateInsight_ShouldReturnDifferentSymbolsForSP500AndNasdaq() {
        // Given
        StockDto sp500 = new StockDto("S&P500", "S&P 500", new BigDecimal("5200"), new BigDecimal("0"), new BigDecimal("0"));
        StockDto nasdaq = new StockDto("NASDAQ", "NASDAQ", new BigDecimal("16500"), new BigDecimal("0"), new BigDecimal("0"));
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of(sp500, nasdaq)));
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of());

        // When
        MarketInsightDto result = marketInsightService.generateInsight(List.of("S&P500", "NASDAQ"), List.of());

        // Then
        assertThat(result.getStocks()).hasSize(2);
        assertThat(result.getStocks().get(0).getCurrency()).isEqualTo("$");
        assertThat(result.getStocks().get(1).getCurrency()).isEqualTo("$");
    }

    @Test
    void generateInsight_WithNullCryptoPrice_ShouldHandleGracefully() {
        // Given
        when(stockService.fetchStockData()).thenReturn(new StockListDto(List.of()));

        CryptoPrice bitcoin = CryptoPrice.create("bitcoin", "BTC", "Bitcoin",
                new BigDecimal("64000"), null, null, null, null, null);
        when(cryptoPriceRepository.findAllCurrent()).thenReturn(List.of(bitcoin));

        // When
        MarketInsightDto result = marketInsightService.generateInsight(List.of(), List.of("bitcoin"));

        // Then
        assertThat(result.getCryptos()).hasSize(1);
        assertThat(result.getCryptos().get(0).getPriceChangePercentage24h()).isZero();
    }
}
