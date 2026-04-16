package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.ReadingStatisticsDto;
import com.newsaggregator.application.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-Test für StatisticsController.
 *
 * <p>Testet den REST Controller für Statistiken mit MockMvc.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        StatisticsController statisticsController = new StatisticsController(statisticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(statisticsController).build();
    }

    @Test
    void getStatistics_ShouldReturnStatistics() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        List<ReadingStatisticsDto.DailyStats> dailyStats = List.of(
            new ReadingStatisticsDto.DailyStats(today, 5L, 2L)
        );
        List<ReadingStatisticsDto.FeedStats> feedStats = List.of(
            new ReadingStatisticsDto.FeedStats("Test Feed", 10L, 3L)
        );

        ReadingStatisticsDto statistics = new ReadingStatisticsDto(
            100L,  // totalArticles
            30L,   // readArticles
            5L,    // favoriteArticles
            dailyStats,
            feedStats
        );

        when(statisticsService.getStatistics()).thenReturn(statistics);

        // When / Then
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArticles").value(100))
                .andExpect(jsonPath("$.readArticles").value(30))
                .andExpect(jsonPath("$.unreadArticles").value(70))
                .andExpect(jsonPath("$.favoriteArticles").value(5))
                .andExpect(jsonPath("$.readPercentage").value(30.0))
                .andExpect(jsonPath("$.articlesPerDay").isArray())
                .andExpect(jsonPath("$.articlesPerDay[0].articleCount").value(5))
                .andExpect(jsonPath("$.articlesPerDay[0].readCount").value(2))
                .andExpect(jsonPath("$.articlesPerFeed").isArray())
                .andExpect(jsonPath("$.articlesPerFeed[0].feedName").value("Test Feed"))
                .andExpect(jsonPath("$.articlesPerFeed[0].totalArticles").value(10))
                .andExpect(jsonPath("$.articlesPerFeed[0].readArticles").value(3));
    }

    @Test
    void getStatistics_ShouldReturnEmptyStatistics() throws Exception {
        // Given
        ReadingStatisticsDto emptyStatistics = new ReadingStatisticsDto(
            0L,  // totalArticles
            0L,  // readArticles
            0L,  // favoriteArticles
            List.of(),
            List.of()
        );

        when(statisticsService.getStatistics()).thenReturn(emptyStatistics);

        // When / Then
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArticles").value(0))
                .andExpect(jsonPath("$.readArticles").value(0))
                .andExpect(jsonPath("$.unreadArticles").value(0))
                .andExpect(jsonPath("$.favoriteArticles").value(0))
                .andExpect(jsonPath("$.readPercentage").value(0.0))
                .andExpect(jsonPath("$.articlesPerDay").isArray())
                .andExpect(jsonPath("$.articlesPerDay").isEmpty())
                .andExpect(jsonPath("$.articlesPerFeed").isArray())
                .andExpect(jsonPath("$.articlesPerFeed").isEmpty());
    }
}
