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
import com.newsaggregator.application.dto.AiSummaryDto;
import com.newsaggregator.application.service.AiSummaryService;

import java.util.List;

/**
 * Unit-Test für AiSummaryController.
 *
 * <p>Testet den REST-Endpunkt für strukturierte KI-Zusammenfassungen.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class AiSummaryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AiSummaryService aiSummaryService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        AiSummaryController controller = new AiSummaryController(aiSummaryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getStructuredSummary_ShouldReturnAiSummaryDto() throws Exception {
        // Given
        AiSummaryDto.AiCategory tech = new AiSummaryDto.AiCategory("Technologie", "Neue KI-Entwicklungen", 5, "positive");
        AiSummaryDto.AiTopic topic = new AiSummaryDto.AiTopic("Künstliche Intelligenz", 8, true);
        AiSummaryDto summary = new AiSummaryDto(
                List.of(tech),
                List.of(topic),
                java.time.LocalDateTime.now().toString()
        );

        when(aiSummaryService.generateStructuredSummary()).thenReturn(summary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary/v2")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        AiSummaryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), AiSummaryDto.class);
        assertThat(responseBody.getCategories()).hasSize(1);
        assertThat(responseBody.getCategories().get(0).getName()).isEqualTo("Technologie");
        assertThat(responseBody.getCategories().get(0).getSummary()).isEqualTo("Neue KI-Entwicklungen");
        assertThat(responseBody.getCategories().get(0).getSentiment()).isEqualTo("positive");
        assertThat(responseBody.getTopTopics()).hasSize(1);
        assertThat(responseBody.getTopTopics().get(0).getName()).isEqualTo("Künstliche Intelligenz");
    }

    @Test
    void getStructuredSummary_WithEmptyCategories_ShouldReturnEmptyDto() throws Exception {
        // Given
        AiSummaryDto summary = new AiSummaryDto(
                List.of(),
                List.of(),
                java.time.LocalDateTime.now().toString()
        );

        when(aiSummaryService.generateStructuredSummary()).thenReturn(summary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary/v2")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        AiSummaryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), AiSummaryDto.class);
        assertThat(responseBody.getCategories()).isEmpty();
        assertThat(responseBody.getTopTopics()).isEmpty();
    }

    @Test
    void getStructuredSummary_WithMultipleCategories_ShouldReturnAllCategories() throws Exception {
        // Given
        AiSummaryDto.AiCategory tech = new AiSummaryDto.AiCategory("Technologie", "KI-News", 5, "positive");
        AiSummaryDto.AiCategory business = new AiSummaryDto.AiCategory("Wirtschaft", "Marktupdate", 3, "neutral");
        AiSummaryDto.AiCategory politics = new AiSummaryDto.AiCategory("Politik", "Wahlen", 2, "negative");

        AiSummaryDto summary = new AiSummaryDto(
                List.of(tech, business, politics),
                List.of(),
                java.time.LocalDateTime.now().toString()
        );

        when(aiSummaryService.generateStructuredSummary()).thenReturn(summary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary/v2")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        AiSummaryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), AiSummaryDto.class);
        assertThat(responseBody.getCategories()).hasSize(3);
        assertThat(responseBody.getCategories().stream().map(AiSummaryDto.AiCategory::getName))
                .containsExactly("Technologie", "Wirtschaft", "Politik");
    }

    @Test
    void getStructuredSummary_WithDifferentSentiments_ShouldReturnCorrectSentiment() throws Exception {
        // Given
        AiSummaryDto.AiCategory positive = new AiSummaryDto.AiCategory("Tech", "Good news", 5, "positive");
        AiSummaryDto.AiCategory neutral = new AiSummaryDto.AiCategory("Biz", "Steady", 3, "neutral");
        AiSummaryDto.AiCategory negative = new AiSummaryDto.AiCategory("Politics", "Issues", 2, "negative");

        AiSummaryDto summary = new AiSummaryDto(
                List.of(positive, neutral, negative),
                List.of(),
                java.time.LocalDateTime.now().toString()
        );

        when(aiSummaryService.generateStructuredSummary()).thenReturn(summary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary/v2")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        AiSummaryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), AiSummaryDto.class);
        assertThat(responseBody.getCategories().get(0).getSentiment()).isEqualTo("positive");
        assertThat(responseBody.getCategories().get(1).getSentiment()).isEqualTo("neutral");
        assertThat(responseBody.getCategories().get(2).getSentiment()).isEqualTo("negative");
    }

    @Test
    void getStructuredSummary_WithTrendingTopics_ShouldReturnTrendingFlag() throws Exception {
        // Given
        AiSummaryDto.AiTopic trending = new AiSummaryDto.AiTopic("Breaking News", 15, true);
        AiSummaryDto.AiTopic normal = new AiSummaryDto.AiTopic("Regular", 3, false);

        AiSummaryDto summary = new AiSummaryDto(
                List.of(),
                List.of(trending, normal),
                java.time.LocalDateTime.now().toString()
        );

        when(aiSummaryService.generateStructuredSummary()).thenReturn(summary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary/v2")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        AiSummaryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), AiSummaryDto.class);
        assertThat(responseBody.getTopTopics().get(0).isTrending()).isTrue();
        assertThat(responseBody.getTopTopics().get(1).isTrending()).isFalse();
    }

    @Test
    void getStructuredSummary_ResponseShouldHaveCorrectStructure() throws Exception {
        // Given
        AiSummaryDto summary = new AiSummaryDto(
                List.of(new AiSummaryDto.AiCategory("Test", "Test summary", 1, "neutral")),
                List.of(new AiSummaryDto.AiTopic("Test Topic", 1, false)),
                java.time.LocalDateTime.now().toString()
        );

        when(aiSummaryService.generateStructuredSummary()).thenReturn(summary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary/v2")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("categories");
        assertThat(json).contains("topTopics");
        assertThat(json).contains("generatedAt");
    }

    @Test
    void getStructuredSummary_WithArticleCount_ShouldReturnCorrectCounts() throws Exception {
        // Given
        AiSummaryDto.AiCategory category = new AiSummaryDto.AiCategory("Tech", "News", 42, "positive");
        AiSummaryDto.AiTopic topic = new AiSummaryDto.AiTopic("AI", 25, true);

        AiSummaryDto summary = new AiSummaryDto(
                List.of(category),
                List.of(topic),
                java.time.LocalDateTime.now().toString()
        );

        when(aiSummaryService.generateStructuredSummary()).thenReturn(summary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary/v2")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        AiSummaryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), AiSummaryDto.class);
        assertThat(responseBody.getCategories().get(0).getArticleCount()).isEqualTo(42);
        assertThat(responseBody.getTopTopics().get(0).getArticleCount()).isEqualTo(25);
    }

    @Test
    void getStructuredSummary_WithTimestamp_ShouldIncludeGeneratedAt() throws Exception {
        // Given
        String now = java.time.LocalDateTime.now().toString();
        AiSummaryDto summary = new AiSummaryDto(
                List.of(),
                List.of(),
                now
        );

        when(aiSummaryService.generateStructuredSummary()).thenReturn(summary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary/v2")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        AiSummaryDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), AiSummaryDto.class);
        assertThat(responseBody.getGeneratedAt()).isEqualTo(now);
    }
}
