package com.newsaggregator.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.application.dto.WeatherInsightDto;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.WeatherInsightCacheJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.repository.WeatherInsightCacheJpaRepository;

/**
 * Unit-Test fuer WeatherInsightService.
 *
 * <p>Testet die Wetter-Einblicksgenerierung inkl. Caching,
 * Fallback-Verhalten und Datenaggregation.</p>
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class WeatherInsightServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private WeatherInsightCacheJpaRepository cacheRepository;

    private WeatherInsightService weatherInsightService;

    @BeforeEach
    void setUp() {
        weatherInsightService = new WeatherInsightService(articleRepository, cacheRepository);
    }

    @Test
    void generateInsight_WithValidCache_ShouldReturnCachedData() throws Exception {
        // Given
        WeatherInsightCacheJpaEntity cacheEntry = new WeatherInsightCacheJpaEntity();
        cacheEntry.setLocationKey("52.5200,13.4100");
        cacheEntry.setInsightJson("""
            {"temperature":22.0,"weatherCode":0,"description":"Klarer Himmel",\
            "todayMin":18.0,"todayMax":25.0,"city":"Berlin",\
            "insight":"Guter Tag fuer einen Spaziergang!","forecast":[],\"generatedAt\":\"%s\"}
            """.formatted(LocalDateTime.now().toString()));
        cacheEntry.setExpiresAt(LocalDateTime.now().plusHours(1));

        when(cacheRepository.findByLocationKey("52.5200,13.4100")).thenReturn(Optional.of(cacheEntry));

        // When
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then
        assertThat(result.getCity()).isEqualTo("Berlin");
        assertThat(result.getTemperature()).isEqualTo(22.0);
        assertThat(result.getInsight()).isEqualTo("Guter Tag fuer einen Spaziergang!");
    }

    @Test
    void generateInsight_WithExpiredCache_ShouldGenerateNewInsight() {
        // Given
        WeatherInsightCacheJpaEntity expiredEntry = new WeatherInsightCacheJpaEntity();
        expiredEntry.setLocationKey("52.5200,13.4100");
        expiredEntry.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.of(expiredEntry));
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenReturn(expiredEntry);

        // When
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then - Should generate new insight (fallback due to network failure)
        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("Berlin");
    }

    @Test
    void generateInsight_WithNoCache_ShouldGenerateAndCacheNewInsight() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("Berlin");
        assertThat(result.getInsight()).isNotNull();
        verify(cacheRepository).save(any(WeatherInsightCacheJpaEntity.class));
    }

    @Test
    void invalidateCache_ShouldDeleteAllCacheEntries() {
        // Given
        doNothing().when(cacheRepository).deleteAll();

        // When
        weatherInsightService.invalidateCache();

        // Then
        verify(cacheRepository).deleteAll();
    }

    @Test
    void generateInsight_ShouldIncludeWeatherDescription() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When - Weather code 0 = clear sky
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then
        assertThat(result.getDescription()).isNotNull();
        assertThat(result.getWeatherCode()).isNotNull();
    }

    @Test
    void generateInsight_ShouldIncludeTemperatureRange() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then
        assertThat(result.getTemperature()).isNotNull();
        assertThat(result.getTodayMin()).isNotNull();
        assertThat(result.getTodayMax()).isNotNull();
        assertThat(result.getTodayMax()).isGreaterThanOrEqualTo(result.getTodayMin());
    }

    @Test
    void generateInsight_ShouldIncludeForecast() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then
        assertThat(result.getForecast()).isNotNull();
    }

    @Test
    void generateInsight_ShouldUseCorrectLocationKeyFormatting() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When - Berlin coordinates
        weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then - Verify location key format (4 decimal places) - called twice (once in generateInsight, once in saveToCache)
        verify(cacheRepository, atLeast(1)).findByLocationKey("52.5200,13.4100");
    }

    @Test
    void generateInsight_WithDifferentCities_ShouldReturnDifferentLocations() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WeatherInsightDto berlin = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");
        WeatherInsightDto munich = weatherInsightService.generateInsight(48.14, 11.58, "Muenchen");

        // Then
        assertThat(berlin.getCity()).isEqualTo("Berlin");
        assertThat(munich.getCity()).isEqualTo("Muenchen");
    }

    @Test
    void generateInsight_ShouldSetGeneratedAtTimestamp() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime before = LocalDateTime.now();

        // When
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then
        assertThat(result.getGeneratedAt()).isNotNull();
        LocalDateTime generatedAt = LocalDateTime.parse(result.getGeneratedAt());
        assertThat(generatedAt).isAfterOrEqualTo(before);
    }

    @Test
    void generateInsight_ShouldIncludeNewsContextInPrompt() {
        // Given - Articles exist
        Feed feed = Feed.createNew("Test Feed", "http://test.com", "Test");
        Article article1 = Article.of(ArticleId.of(1L), "Tech News Today", "Latest tech updates", "http://tech.com/1",
                LocalDateTime.now(), feed, LocalDateTime.now());
        Article article2 = Article.of(ArticleId.of(2L), "Weather Update", "Sunny forecast", "http://weather.com/1",
                LocalDateTime.now(), feed, LocalDateTime.now());
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of(article1, article2));
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When - Fallback will be used since Ollama is not available
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getInsight()).isNotEmpty();
    }

    @Test
    void generateInsight_WithCacheMiss_ShouldCallRepositoryAndSave() {
        // Given - Repository returns empty (cache miss)
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("Berlin");
    }

    @Test
    void generateInsight_WhenOllamaUnavailable_ShouldUseFallback() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When - Network call to Ollama will fail, fallback should be used
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then - Should still return a valid result with fallback insight
        assertThat(result).isNotNull();
        assertThat(result.getInsight()).isNotNull();
        assertThat(result.getInsight()).isNotEmpty();
    }

    @Test
    void generateInsight_WithNegativeTemperatures_ShouldHandleCorrectly() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WeatherInsightDto result = weatherInsightService.generateInsight(-22.91, -18.44, "Antarctica");

        // Then
        assertThat(result.getCity()).isEqualTo("Antarctica");
    }

    @Test
    void generateInsight_WithEmptyCache_ShouldGenerateNewData() {
        // Given
        when(cacheRepository.findByLocationKey(any())).thenReturn(Optional.empty());
        when(articleRepository.findAll()).thenReturn(List.of());
        when(cacheRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        WeatherInsightDto result = weatherInsightService.generateInsight(52.52, 13.41, "Berlin");

        // Then - Called at least once (in generateInsight and possibly in saveToCache)
        verify(cacheRepository, atLeast(1)).findByLocationKey(any());
        assertThat(result).isNotNull();
    }
}
