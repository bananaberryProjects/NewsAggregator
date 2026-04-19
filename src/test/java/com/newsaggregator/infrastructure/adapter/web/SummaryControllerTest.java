package com.newsaggregator.infrastructure.adapter.web;

import java.util.Map;

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
import com.newsaggregator.application.service.SummaryService;

/**
 * Unit-Test für SummaryController.
 *
 * <p>Testet den REST-Endpunkt für Zusammenfassungsgenerierung.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class SummaryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SummaryService summaryService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        SummaryController controller = new SummaryController(summaryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getSummary_WithGeneratedSummary_ShouldReturnOkWithSummary() throws Exception {
        // Given
        String expectedSummary = "Heute gab es wichtige Entwicklungen in der Technologiebranche. Neue KI-Modelle wurden veröffentlicht.";
        when(summaryService.generateSummary()).thenReturn(expectedSummary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Map.class);
        assertThat(responseBody).containsKey("summary");
        assertThat(responseBody.get("summary")).isEqualTo(expectedSummary);
    }

    @Test
    void getSummary_WithEmptySummary_ShouldReturnOkWithEmptyString() throws Exception {
        // Given
        when(summaryService.generateSummary()).thenReturn("");

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Map.class);
        assertThat(responseBody).containsKey("summary");
        assertThat(responseBody.get("summary")).isEmpty();
    }

    @Test
    void getSummary_WithFallbackSummary_ShouldReturnOkWithFallbackMessage() throws Exception {
        // Given
        String fallbackSummary = "Heute gibt es 5 neue Artikel. Die wichtigsten Themen: Tech-News, Wirtschaft";
        when(summaryService.generateSummary()).thenReturn(fallbackSummary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Map.class);
        assertThat(responseBody.get("summary")).isEqualTo(fallbackSummary);
        assertThat(responseBody.get("summary")).contains("Heute gibt es");
    }

    @Test
    void getSummary_WithLongSummary_ShouldReturnOkWithFullText() throws Exception {
        // Given
        String longSummary = """
            Die heutigen Nachrichten sind geprägt von mehreren wichtigen Ereignissen.
            In der Technologiebranche wurden neue KI-Entwicklungen vorgestellt.
            Die Wirtschaft zeigt positive Signale mit steigenden Aktienkursen.
            Politische Entwicklungen in Europa stehen im Fokus.
            Sportlich gab es spannende Spiele in der Bundesliga.
            """.stripIndent();
        when(summaryService.generateSummary()).thenReturn(longSummary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Map.class);
        assertThat(responseBody.get("summary")).isEqualTo(longSummary);
        assertThat(responseBody.get("summary")).hasLineCount(5);
    }

    @Test
    void getSummary_WithNoArticlesMessage_ShouldReturnOkWithMessage() throws Exception {
        // Given - SummaryService returns a message when no articles available
        String noArticlesMessage = "Keine neuen Artikel fuer heute verfuegbar.";
        when(summaryService.generateSummary()).thenReturn(noArticlesMessage);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then - Controller still returns 200 OK, just with the message in the body
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Map.class);
        assertThat(responseBody.get("summary")).isEqualTo(noArticlesMessage);
    }

    @Test
    void getSummary_WithSpecialCharacters_ShouldReturnOkWithEncodedText() throws Exception {
        // Given
        String summaryWithSpecialChars = "Überblick: Aktuelle Nachrichten aus München & Köln! Preise: 100€";
        when(summaryService.generateSummary()).thenReturn(summaryWithSpecialChars);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Map.class);
        assertThat(responseBody.get("summary")).isEqualTo(summaryWithSpecialChars);
        assertThat(responseBody.get("summary")).contains("Ü", "&", "€");
    }

    @Test
    void getSummary_ResponseShouldContainOnlySummaryKey() throws Exception {
        // Given
        String summary = "Daily news summary";
        when(summaryService.generateSummary()).thenReturn(summary);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Map.class);
        assertThat(responseBody).hasSize(1);
        assertThat(responseBody.keySet()).containsExactly("summary");
    }
}
