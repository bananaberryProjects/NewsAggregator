package com.newsaggregator.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.application.dto.TrendingTopicDto;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository.TermStats;

/**
 * Unit-Test für TrendingTopicService (Fast-Modus, ohne KI).
 */
@ExtendWith(MockitoExtension.class)
class TrendingTopicServiceTest {

    @Mock
    private TrendingTopicRepository trendingRepository;

    @Mock
    private GptClient gptClient;

    private TrendingTopicService service;

    @BeforeEach
    void setUp() {
        service = new TrendingTopicService(trendingRepository, gptClient);
    }

    @Test
    void getTrendingTopicsFast_WithArticles_ShouldReturnTopics() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<TermStats> currentStats = List.of(
                new TermStats("bitcoin etf zulassung", 8, 3),
                new TermStats("dax neue rekordhoch", 5, 2),
                new TermStats("ki regulierung eu", 4, 2)
        );

        when(trendingRepository.findTopTermsInWindow(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(currentStats);

        // When
        TrendingTopicDto result = service.getTrendingTopicsFast(24, 10);

        // Then
        assertThat(result.getWindow()).isEqualTo("24h");
        assertThat(result.getTopics()).isNotEmpty();

        // "bitcoin" sollte als eigenes Wort auftauchen (Tokenisierung)
        Map<String, TrendingTopicDto.Topic> topicMap = new java.util.HashMap<>();
        for (TrendingTopicDto.Topic t : result.getTopics()) {
            topicMap.put(t.getTerm().toLowerCase(), t);
        }

        assertThat(topicMap).containsKey("Bitcoin");
        assertThat(topicMap.get("bitcoin").getCount()).isGreaterThanOrEqualTo(8);
        assertThat(topicMap.get("bitcoin").getFeeds()).isEqualTo(3);
    }

    @Test
    void getTrendingTopicsFast_NoArticles_ShouldReturnEmpty() {
        // Given
        when(trendingRepository.findTopTermsInWindow(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(List.of());

        // When
        TrendingTopicDto result = service.getTrendingTopicsFast(24, 10);

        // Then
        assertThat(result.getWindow()).isEqualTo("24h");
        assertThat(result.getTopics()).isEmpty();
        assertThat(result.getBreakingAlerts()).isEmpty();
    }

    @Test
    void getTrendingTopicsFast_WithBreaking_ShouldFlagBreaking() {
        // Given
        List<TermStats> currentStats = List.of(
                new TermStats("bitcoin", 12, 4)  // 4 feeds, 12 Artikel
        );

        when(trendingRepository.findTopTermsInWindow(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(currentStats);

        // When
        TrendingTopicDto result = service.getTrendingTopicsFast(24, 10);

        // Then
        assertThat(result.getTopics()).isNotEmpty();
        TrendingTopicDto.Topic bitcoin = result.getTopics().get(0);
        assertThat(bitcoin.isBreaking()).isTrue();
        assertThat(result.getBreakingAlerts()).hasSize(1);
        assertThat(result.getBreakingAlerts().get(0).getTerm()).isEqualTo("Bitcoin");
    }

    @Test
    void getTrendingTopicsFast_Cache_ShouldReturnCachedResult() {
        // Given
        List<TermStats> currentStats = List.of(
                new TermStats("test", 5, 1)
        );
        when(trendingRepository.findTopTermsInWindow(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(currentStats);

        // First call
        TrendingTopicDto first = service.getTrendingTopicsFast(24, 10);

        // Second call (should hit cache)
        TrendingTopicDto second = service.getTrendingTopicsFast(24, 10);

        // Then
        assertThat(second).isEqualTo(first);
    }
}
