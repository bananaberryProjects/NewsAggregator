package com.newsaggregator.infrastructure.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.domain.model.CryptoPrice;
import com.newsaggregator.domain.port.in.CryptoPriceRepository;

@ExtendWith(MockitoExtension.class)
class CoinGeckoScheduledTaskTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CryptoPriceRepository cryptoPriceRepository;

    @Captor
    private ArgumentCaptor<CryptoPrice> cryptoPriceCaptor;

    private CoinGeckoScheduledTask scheduledTask;

    @BeforeEach
    void setUp() {
        scheduledTask = new CoinGeckoScheduledTask(restTemplate, objectMapper, cryptoPriceRepository);
    }

    @Test
    void fetchCurrentPrices_ShouldFetchAndSaveAllCoins() throws Exception {
        // Given
        String mockResponse = "{" +
                "\"bitcoin\":{\"usd\":65000.00,\"eur\":60000.00,\"usd_24h_change\":500.00,\"usd_market_cap\":1200000000000,\"usd_24h_vol\":30000000000}," +
                "\"ethereum\":{\"usd\":3500.00,\"eur\":3200.00,\"usd_24h_change\":-50.00,\"usd_market_cap\":400000000000,\"usd_24h_vol\":15000000000}," +
                "\"solana\":{\"usd\":150.00,\"eur\":138.00,\"usd_24h_change\":5.00,\"usd_market_cap\":60000000000,\"usd_24h_vol\":2000000000}," +
                "\"ripple\":{\"usd\":0.60,\"eur\":0.55,\"usd_24h_change\":0.01,\"usd_market_cap\":30000000000,\"usd_24h_vol\":1000000000}" +
                "}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        scheduledTask.fetchCurrentPrices();

        // Then
        verify(cryptoPriceRepository, org.mockito.Mockito.times(4)).save(cryptoPriceCaptor.capture());

        var savedPrices = cryptoPriceCaptor.getAllValues();
        assertThat(savedPrices).hasSize(4);

        // Bitcoin assertions
        CryptoPrice bitcoin = savedPrices.stream()
                .filter(p -> p.getCoinId().equals("bitcoin"))
                .findFirst()
                .orElseThrow();
        assertThat(bitcoin.getSymbol()).isEqualTo("BTC");
        assertThat(bitcoin.getName()).isEqualTo("Bitcoin");
        assertThat(bitcoin.getPriceUsd()).isEqualByComparingTo(new BigDecimal("65000.00"));
        assertThat(bitcoin.getPriceEur()).isEqualByComparingTo(new BigDecimal("60000.00"));
        assertThat(bitcoin.getPriceChange24h()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(bitcoin.getMarketCapUsd()).isEqualTo(1200000000000L);
        assertThat(bitcoin.getVolume24hUsd()).isEqualTo(30000000000L);

        // Ethereum assertions
        CryptoPrice ethereum = savedPrices.stream()
                .filter(p -> p.getCoinId().equals("ethereum"))
                .findFirst()
                .orElseThrow();
        assertThat(ethereum.getSymbol()).isEqualTo("ETH");
        assertThat(ethereum.getName()).isEqualTo("Ethereum");
        assertThat(ethereum.getPriceUsd()).isEqualByComparingTo(new BigDecimal("3500.00"));

        // Solana assertions
        CryptoPrice solana = savedPrices.stream()
                .filter(p -> p.getCoinId().equals("solana"))
                .findFirst()
                .orElseThrow();
        assertThat(solana.getSymbol()).isEqualTo("SOL");
        assertThat(solana.getName()).isEqualTo("Solana");
        assertThat(solana.getPriceUsd()).isEqualByComparingTo(new BigDecimal("150.00"));

        // Ripple assertions
        CryptoPrice ripple = savedPrices.stream()
                .filter(p -> p.getCoinId().equals("ripple"))
                .findFirst()
                .orElseThrow();
        assertThat(ripple.getSymbol()).isEqualTo("XRP");
        assertThat(ripple.getName()).isEqualTo("XRP");
        assertThat(ripple.getPriceUsd()).isEqualByComparingTo(new BigDecimal("0.60"));
    }

    @Test
    void fetchCurrentPrices_WhenCoinDataMissing_ShouldSkipThatCoin() throws Exception {
        // Given - Response missing ethereum and solana
        String mockResponse = "{" +
                "\"bitcoin\":{\"usd\":65000.00,\"eur\":60000.00,\"usd_24h_change\":500.00,\"usd_market_cap\":1200000000000,\"usd_24h_vol\":30000000000}," +
                "\"ripple\":{\"usd\":0.60,\"eur\":0.55,\"usd_24h_change\":0.01,\"usd_market_cap\":30000000000,\"usd_24h_vol\":1000000000}" +
                "}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        scheduledTask.fetchCurrentPrices();

        // Then - Only 2 coins should be saved (bitcoin and ripple)
        verify(cryptoPriceRepository, org.mockito.Mockito.times(2)).save(any(CryptoPrice.class));
    }

    @Test
    void fetchCurrentPrices_WhenApiThrowsException_ShouldNotSaveAnything() throws Exception {
        // Given
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API unavailable"));

        // When
        scheduledTask.fetchCurrentPrices();

        // Then
        verify(cryptoPriceRepository, never()).save(any(CryptoPrice.class));
    }

    @Test
    void fetchCurrentPrices_ShouldCalculateChangePercentage() throws Exception {
        // Given
        String mockResponse = "{" +
                "\"bitcoin\":{\"usd\":10000.00,\"eur\":9200.00,\"usd_24h_change\":500.00,\"usd_market_cap\":100000000000,\"usd_24h_vol\":10000000000}" +
                "}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        scheduledTask.fetchCurrentPrices();

        // Then
        verify(cryptoPriceRepository).save(cryptoPriceCaptor.capture());
        CryptoPrice savedPrice = cryptoPriceCaptor.getValue();

        // Change percentage should be (500 / 10000) * 100 = 5%
        assertThat(savedPrice.getPriceChange24h()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(savedPrice.getPriceChangePercentage24h())
                .isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    void fetchCurrentPrices_WithNegativeChange_ShouldCalculateNegativePercentage() throws Exception {
        // Given
        String mockResponse = "{" +
                "\"ethereum\":{\"usd\":4000.00,\"eur\":3680.00,\"usd_24h_change\":-200.00,\"usd_market_cap\":400000000000,\"usd_24h_vol\":15000000000}" +
                "}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        scheduledTask.fetchCurrentPrices();

        // Then
        verify(cryptoPriceRepository).save(cryptoPriceCaptor.capture());
        CryptoPrice savedPrice = cryptoPriceCaptor.getValue();

        // Change percentage should be (-200 / 4000) * 100 = -5%
        assertThat(savedPrice.getPriceChange24h()).isEqualByComparingTo(new BigDecimal("-200.00"));
        assertThat(savedPrice.getPriceChangePercentage24h())
                .isEqualByComparingTo(new BigDecimal("-5.00"));
    }

    @Test
    void fetchCurrentPrices_WithMissingDataForCoin_ShouldSkipThatCoin() throws Exception {
        // Given - Response with partial data (bitcoin has values, ethereum missing some fields)
        String mockResponse = "{" +
                "\"bitcoin\":{\"usd\":65000.00,\"eur\":60000.00,\"usd_24h_change\":500.00,\"usd_market_cap\":1200000000000,\"usd_24h_vol\":30000000000}," +
                "\"ethereum\":{\"usd\":3500.00,\"eur\":3200.00,\"usd_24h_change\":-50.00}" +
                "}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        scheduledTask.fetchCurrentPrices();

        // Then - Should save both coins (missing fields use defaults)
        verify(cryptoPriceRepository, org.mockito.Mockito.times(2)).save(cryptoPriceCaptor.capture());

        var savedPrices = cryptoPriceCaptor.getAllValues();
        CryptoPrice ethereum = savedPrices.stream()
                .filter(p -> p.getCoinId().equals("ethereum"))
                .findFirst()
                .orElseThrow();

        assertThat(ethereum.getMarketCapUsd()).isEqualTo(0L);
        assertThat(ethereum.getVolume24hUsd()).isEqualTo(0L);
    }

    @Test
    void fetchCurrentPrices_ShouldUseCorrectUrl() throws Exception {
        // Given
        String expectedUrl = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana,ripple" +
                "&vs_currencies=usd,eur" +
                "&include_24hr_change=true" +
                "&include_market_cap=true" +
                "&include_24hr_vol=true";

        String mockResponse = "{\"bitcoin\":{\"usd\":65000.00,\"eur\":60000.00,\"usd_24h_change\":500.00,\"usd_market_cap\":1200000000000,\"usd_24h_vol\":30000000000}}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        scheduledTask.fetchCurrentPrices();

        // Then
        verify(restTemplate).getForObject(expectedUrl, String.class);
    }

    @Test
    void fetchCurrentPrices_ShouldSetLastUpdatedTimestamp() throws Exception {
        // Given
        String mockResponse = "{" +
                "\"bitcoin\":{\"usd\":65000.00,\"eur\":60000.00,\"usd_24h_change\":500.00,\"usd_market_cap\":1200000000000,\"usd_24h_vol\":30000000000}" +
                "}";
        JsonNode mockRoot = new ObjectMapper().readTree(mockResponse);

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRoot);

        // When
        scheduledTask.fetchCurrentPrices();

        // Then
        verify(cryptoPriceRepository).save(cryptoPriceCaptor.capture());
        CryptoPrice savedPrice = cryptoPriceCaptor.getValue();

        assertThat(savedPrice.getLastUpdated()).isNotNull();
        assertThat(savedPrice.getId()).isNotNull();
    }
}
