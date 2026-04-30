package com.newsaggregator.infrastructure.adapter.web;

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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.newsaggregator.application.dto.DashboardStatsDto;
import com.newsaggregator.application.service.DashboardStatsService;

import java.time.Instant;

/**
 * Unit-Test für DashboardStatsController.
 *
 * <p>Testet den REST-Endpunkt für Dashboard-Statistiken.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class DashboardStatsControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DashboardStatsService dashboardStatsService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        DashboardStatsController controller = new DashboardStatsController(dashboardStatsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getDashboardStats_ShouldReturnStats() throws Exception {
        // Given
        DashboardStatsDto stats = new DashboardStatsDto(
                42, 15, 8, 25, 3, 5, 12,
                Instant.parse("2024-01-15T14:30:00Z")
        );

        when(dashboardStatsService.getDashboardStats()).thenReturn(stats);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        DashboardStatsDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), DashboardStatsDto.class);
        assertThat(responseBody.getUnreadCount()).isEqualTo(42);
        assertThat(responseBody.getTotalFeeds()).isEqualTo(15);
        assertThat(responseBody.getFeedsWithNewArticles()).isEqualTo(8);
        assertThat(responseBody.getFavoriteCount()).isEqualTo(25);
        assertThat(responseBody.getNewFavoritesToday()).isEqualTo(3);
        assertThat(responseBody.getReadStreakDays()).isEqualTo(5);
        assertThat(responseBody.getArticlesReadToday()).isEqualTo(12);
        assertThat(responseBody.getLastReadAt()).isEqualTo(Instant.parse("2024-01-15T14:30:00Z"));
    }

    @Test
    void getDashboardStats_WithZeroValues_ShouldReturnEmptyStats() throws Exception {
        // Given
        DashboardStatsDto stats = new DashboardStatsDto(
                0, 0, 0, 0, 0, 0, 0,
                null
        );

        when(dashboardStatsService.getDashboardStats()).thenReturn(stats);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        DashboardStatsDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), DashboardStatsDto.class);
        assertThat(responseBody.getUnreadCount()).isZero();
        assertThat(responseBody.getTotalFeeds()).isZero();
        assertThat(responseBody.getFavoriteCount()).isZero();
        assertThat(responseBody.getLastReadAt()).isNull();
    }

    @Test
    void getDashboardStats_WithLargeValues_ShouldReturnCorrectStats() throws Exception {
        // Given
        DashboardStatsDto stats = new DashboardStatsDto(
                9999, 500, 400, 8888, 100, 365, 250,
                Instant.parse("2024-12-31T23:59:59Z")
        );

        when(dashboardStatsService.getDashboardStats()).thenReturn(stats);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        DashboardStatsDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), DashboardStatsDto.class);
        assertThat(responseBody.getUnreadCount()).isEqualTo(9999);
        assertThat(responseBody.getTotalFeeds()).isEqualTo(500);
        assertThat(responseBody.getReadStreakDays()).isEqualTo(365);
    }

    @Test
    void getDashboardStats_ResponseShouldContainAllFields() throws Exception {
        // Given
        DashboardStatsDto stats = new DashboardStatsDto(
                10, 5, 3, 20, 2, 7, 5,
                Instant.parse("2024-01-15T10:00:00Z")
        );

        when(dashboardStatsService.getDashboardStats()).thenReturn(stats);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("unreadCount");
        assertThat(json).contains("totalFeeds");
        assertThat(json).contains("feedsWithNewArticles");
        assertThat(json).contains("favoriteCount");
        assertThat(json).contains("newFavoritesToday");
        assertThat(json).contains("readStreakDays");
        assertThat(json).contains("articlesReadToday");
        assertThat(json).contains("lastReadAt");
    }

    @Test
    void getDashboardStats_ShouldReturn200Status() throws Exception {
        // Given
        DashboardStatsDto stats = new DashboardStatsDto(
                0, 0, 0, 0, 0, 0, 0, null
        );
        when(dashboardStatsService.getDashboardStats()).thenReturn(stats);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void getDashboardStats_WithActiveReadingStreak_ShouldReturnStreakDays() throws Exception {
        // Given
        DashboardStatsDto stats = new DashboardStatsDto(
                10, 5, 3, 20, 2, 30, 5,
                Instant.now()
        );

        when(dashboardStatsService.getDashboardStats()).thenReturn(stats);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        DashboardStatsDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), DashboardStatsDto.class);
        assertThat(responseBody.getReadStreakDays()).isEqualTo(30);
    }

    @Test
    void getDashboardStats_WithNewArticlesToday_ShouldReturnCorrectCount() throws Exception {
        // Given
        DashboardStatsDto stats = new DashboardStatsDto(
                10, 5, 3, 20, 2, 7, 15,
                Instant.now()
        );

        when(dashboardStatsService.getDashboardStats()).thenReturn(stats);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        DashboardStatsDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), DashboardStatsDto.class);
        assertThat(responseBody.getArticlesReadToday()).isEqualTo(15);
    }

    @Test
    void getDashboardStats_WithFeedsWithNewArticles_ShouldReturnCorrectCount() throws Exception {
        // Given
        DashboardStatsDto stats = new DashboardStatsDto(
                100, 20, 8, 50, 5, 10, 15,
                Instant.now()
        );

        when(dashboardStatsService.getDashboardStats()).thenReturn(stats);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        DashboardStatsDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), DashboardStatsDto.class);
        assertThat(responseBody.getFeedsWithNewArticles()).isEqualTo(8);
    }
}
