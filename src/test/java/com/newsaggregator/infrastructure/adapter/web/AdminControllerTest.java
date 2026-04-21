package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.service.BulkExtractArticleContentService;
import com.newsaggregator.domain.port.in.BulkExtractArticleContentUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit-Test für AdminController.
 *
 * <p>Testet die REST-Endpunkte für Admin-Operationen.
 * Verwendet MockitoExtension für Mock-Injection.</p>
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BulkExtractArticleContentService bulkExtractService;

    @BeforeEach
    void setUp() {
        AdminController controller = new AdminController(bulkExtractService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void extractContentForArticles_WithDefaultParameters_ShouldReturnSuccess() throws Exception {
        // Given
        BulkExtractArticleContentUseCase.ExtractionResult result =
                new BulkExtractArticleContentUseCase.ExtractionResult(10, 8, 2, 0, List.of());
        when(bulkExtractService.extractContentForArticlesWithoutContent(50, 2000)).thenReturn(result);

        // When / Then
        mockMvc.perform(post("/api/admin/articles/extract-content")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Content-Extraktion abgeschlossen"))
                .andExpect(jsonPath("$.processedCount").value(10))
                .andExpect(jsonPath("$.successCount").value(8))
                .andExpect(jsonPath("$.failedCount").value(2))
                .andExpect(jsonPath("$.skippedCount").value(0));
    }

    @Test
    void extractContentForArticles_WithCustomParameters_ShouldReturnSuccess() throws Exception {
        // Given
        BulkExtractArticleContentUseCase.ExtractionResult result =
                new BulkExtractArticleContentUseCase.ExtractionResult(5, 5, 0, 0, List.of());
        when(bulkExtractService.extractContentForArticlesWithoutContent(10, 1000)).thenReturn(result);

        // When / Then
        mockMvc.perform(post("/api/admin/articles/extract-content")
                        .param("limit", "10")
                        .param("delayMs", "1000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.processedCount").value(5))
                .andExpect(jsonPath("$.successCount").value(5));
    }

    @Test
    void extractContentForArticles_WithLimitTooLow_ShouldReturn400() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/admin/articles/extract-content")
                        .param("limit", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Limit muss zwischen 1 und 100 liegen"));
    }

    @Test
    void extractContentForArticles_WithLimitTooHigh_ShouldReturn400() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/admin/articles/extract-content")
                        .param("limit", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Limit muss zwischen 1 und 100 liegen"));
    }

    @Test
    void extractContentForArticles_WithDelayTooLow_ShouldReturn400() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/admin/articles/extract-content")
                        .param("delayMs", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Delay muss zwischen 500 und 10000 ms liegen"));
    }

    @Test
    void extractContentForArticles_WithDelayTooHigh_ShouldReturn400() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/admin/articles/extract-content")
                        .param("delayMs", "15000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Delay muss zwischen 500 und 10000 ms liegen"));
    }

    @Test
    void extractContentForArticles_WhenServiceThrowsException_ShouldReturn500() throws Exception {
        // Given
        when(bulkExtractService.extractContentForArticlesWithoutContent(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database error"));

        // When / Then
        mockMvc.perform(post("/api/admin/articles/extract-content")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Interner Fehler: Database error"));
    }

    @Test
    void getArticlesWithoutContentCount_ShouldReturnCount() throws Exception {
        // Given
        when(bulkExtractService.countArticlesWithoutContent()).thenReturn(15L);

        // When / Then
        mockMvc.perform(get("/api/admin/articles/without-content/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(15))
                .andExpect(jsonPath("$.hasArticlesWithoutContent").value(true));
    }

    @Test
    void getArticlesWithoutContentCount_WhenNoArticles_ShouldReturnZeroCount() throws Exception {
        // Given
        when(bulkExtractService.countArticlesWithoutContent()).thenReturn(0L);

        // When / Then
        mockMvc.perform(get("/api/admin/articles/without-content/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.hasArticlesWithoutContent").value(false));
    }
}
