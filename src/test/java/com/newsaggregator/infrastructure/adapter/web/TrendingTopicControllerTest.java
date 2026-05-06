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
import com.newsaggregator.application.dto.TrendingTopicDto;
import com.newsaggregator.application.service.TrendingTopicService;

import java.util.List;

/**
 * Unit-Test für TrendingTopicController.
 */
@ExtendWith(MockitoExtension.class)
class TrendingTopicControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TrendingTopicService trendingTopicService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        TrendingTopicController controller = new TrendingTopicController(trendingTopicService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getTrendingTopics_ShouldReturnTopics() throws Exception {
        // Given
        TrendingTopicDto dto = new TrendingTopicDto(
                "24h",
                "2026-05-06T09:00:00Z",
                List.of(
                        new TrendingTopicDto.Topic("Bitcoin", 12, "up", 140, 4, true),
                        new TrendingTopicDto.Topic("DAX", 8, "stable", 0, 3, false)
                ),
                List.of(
                        new TrendingTopicDto.BreakingAlert("Bitcoin", 12, 4, "12 neue Artikel in 4 Feeds")
                )
        );
        when(trendingTopicService.getTrendingTopics(24, 20)).thenReturn(dto);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/trending")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        TrendingTopicDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), TrendingTopicDto.class);
        assertThat(responseBody.getWindow()).isEqualTo("24h");
        assertThat(responseBody.getTopics()).hasSize(2);
        assertThat(responseBody.getTopics().get(0).getTerm()).isEqualTo("Bitcoin");
        assertThat(responseBody.getTopics().get(0).isBreaking()).isTrue();
        assertThat(responseBody.getBreakingAlerts()).hasSize(1);
    }

    @Test
    void getTrendingTopics_WithCustomParams_ShouldPassParams() throws Exception {
        // Given
        TrendingTopicDto dto = new TrendingTopicDto(
                "48h", "2026-05-06T10:00:00Z", List.of(), List.of()
        );
        when(trendingTopicService.getTrendingTopics(48, 10)).thenReturn(dto);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/trending")
                .param("hours", "48")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void getTrendingTopicsFast_ShouldReturnTopics() throws Exception {
        // Given
        TrendingTopicDto dto = new TrendingTopicDto(
                "24h",
                "2026-05-06T09:00:00Z",
                List.of(new TrendingTopicDto.Topic("KI", 5, "stable", 0, 2, false)),
                List.of()
        );
        when(trendingTopicService.getTrendingTopicsFast(24, 20)).thenReturn(dto);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/trending/fast")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        TrendingTopicDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), TrendingTopicDto.class);
        assertThat(responseBody.getTopics()).hasSize(1);
        assertThat(responseBody.getTopics().get(0).getTerm()).isEqualTo("KI");
    }

    @Test
    void getTrendingTopics_EmptyResult_ShouldReturn200() throws Exception {
        // Given
        TrendingTopicDto dto = new TrendingTopicDto(
                "24h", "2026-05-06T09:00:00Z", List.of(), List.of()
        );
        when(trendingTopicService.getTrendingTopics(24, 20)).thenReturn(dto);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/trending")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(result.getResponse().getContentAsString()).contains("\"topics\":[]");
    }
}
