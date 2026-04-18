package com.newsaggregator.infrastructure.adapter.web;

import java.util.List;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.application.service.ArticleReadStatusService;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleReadStatus;

/**
 * Unit-Test für ArticleReadStatusController.
 *
 * <p>Testet die REST-Endpunkte für Lesestatus und Favoriten.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class ArticleReadStatusControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ArticleReadStatusService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        ArticleReadStatusController controller = new ArticleReadStatusController();
        controller.setService(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void markAsRead_ShouldReturnStatus() throws Exception {
        // Given
        ArticleReadStatus status = new ArticleReadStatus("article-1", "user-001");
        status.setRead(true);
        when(service.markAsRead("article-1")).thenReturn(status);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(post("/api/articles/article-1/read")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        ArticleReadStatus responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), ArticleReadStatus.class);
        assertThat(responseBody.getArticleId()).isEqualTo("article-1");
        assertThat(responseBody.isRead()).isTrue();
    }

    @Test
    void markAsUnread_ShouldReturnStatus() throws Exception {
        // Given
        ArticleReadStatus status = new ArticleReadStatus("article-1", "user-001");
        status.setRead(false);
        when(service.markAsUnread("article-1")).thenReturn(status);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(post("/api/articles/article-1/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        ArticleReadStatus responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), ArticleReadStatus.class);
        assertThat(responseBody.getArticleId()).isEqualTo("article-1");
        assertThat(responseBody.isRead()).isFalse();
    }

    @Test
    void toggleFavorite_ShouldReturnStatus() throws Exception {
        // Given
        ArticleReadStatus status = new ArticleReadStatus("article-1", "user-001");
        status.setFavorite(true);
        when(service.toggleFavorite("article-1")).thenReturn(status);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(post("/api/articles/article-1/favorite")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        ArticleReadStatus responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), ArticleReadStatus.class);
        assertThat(responseBody.getArticleId()).isEqualTo("article-1");
        assertThat(responseBody.isFavorite()).isTrue();
    }

    @Test
    void getReadArticles_ShouldReturnList() throws Exception {
        // Given
        ArticleReadStatus status1 = new ArticleReadStatus("article-1", "user-001");
        status1.setRead(true);
        ArticleReadStatus status2 = new ArticleReadStatus("article-2", "user-001");
        status2.setRead(true);
        when(service.getReadArticles()).thenReturn(List.of(status1, status2));

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/read")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<ArticleReadStatus> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody).hasSize(2);
        assertThat(responseBody.get(0).getArticleId()).isEqualTo("article-1");
        assertThat(responseBody.get(1).getArticleId()).isEqualTo("article-2");
    }

    @Test
    void getFavoriteArticles_ShouldReturnList() throws Exception {
        // Given
        ArticleReadStatus status1 = new ArticleReadStatus("article-1", "user-001");
        status1.setFavorite(true);
        when(service.getFavoriteArticles()).thenReturn(List.of(status1));

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/favorites")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<ArticleReadStatus> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody).hasSize(1);
        assertThat(responseBody.get(0).getArticleId()).isEqualTo("article-1");
        assertThat(responseBody.get(0).isFavorite()).isTrue();
    }

    @Test
    void getStatus_ShouldReturnReadAndFavoriteStatus() throws Exception {
        // Given
        when(service.isRead("article-1")).thenReturn(true);
        when(service.isFavorite("article-1")).thenReturn(false);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/article-1/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        Map<String, Boolean> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody.get("isRead")).isTrue();
        assertThat(responseBody.get("isFavorite")).isFalse();
    }

    @Test
    void getStatus_WhenFavorite_ShouldReturnFavoriteTrue() throws Exception {
        // Given
        when(service.isRead("article-1")).thenReturn(false);
        when(service.isFavorite("article-1")).thenReturn(true);

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/article-1/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        Map<String, Boolean> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody.get("isRead")).isFalse();
        assertThat(responseBody.get("isFavorite")).isTrue();
    }
}
