package com.newsaggregator.infrastructure.adapter.web;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

    @Mock
    private ArticleReadStatusService service;

    @BeforeEach
    void setUp() {
        ArticleReadStatusController controller = new ArticleReadStatusController();
        controller.setService(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void markAsRead_ShouldReturnStatus() throws Exception {
        // Given
        ArticleReadStatus status = new ArticleReadStatus("article-1", "user-001");
        status.setRead(true);
        when(service.markAsRead("article-1")).thenReturn(status);

        // When / Then
        mockMvc.perform(post("/api/articles/article-1/read")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value("article-1"))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void markAsUnread_ShouldReturnStatus() throws Exception {
        // Given
        ArticleReadStatus status = new ArticleReadStatus("article-1", "user-001");
        status.setRead(false);
        when(service.markAsUnread("article-1")).thenReturn(status);

        // When / Then
        mockMvc.perform(post("/api/articles/article-1/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value("article-1"))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void toggleFavorite_ShouldReturnStatus() throws Exception {
        // Given
        ArticleReadStatus status = new ArticleReadStatus("article-1", "user-001");
        status.setFavorite(true);
        when(service.toggleFavorite("article-1")).thenReturn(status);

        // When / Then
        mockMvc.perform(post("/api/articles/article-1/favorite")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value("article-1"))
                .andExpect(jsonPath("$.favorite").value(true));
    }

    @Test
    void getReadArticles_ShouldReturnList() throws Exception {
        // Given
        ArticleReadStatus status1 = new ArticleReadStatus("article-1", "user-001");
        status1.setRead(true);
        ArticleReadStatus status2 = new ArticleReadStatus("article-2", "user-001");
        status2.setRead(true);
        when(service.getReadArticles()).thenReturn(List.of(status1, status2));

        // When / Then
        mockMvc.perform(get("/api/articles/read")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].articleId").value("article-1"))
                .andExpect(jsonPath("$[1].articleId").value("article-2"));
    }

    @Test
    void getFavoriteArticles_ShouldReturnList() throws Exception {
        // Given
        ArticleReadStatus status1 = new ArticleReadStatus("article-1", "user-001");
        status1.setFavorite(true);
        when(service.getFavoriteArticles()).thenReturn(List.of(status1));

        // When / Then
        mockMvc.perform(get("/api/articles/favorites")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].articleId").value("article-1"))
                .andExpect(jsonPath("$[0].favorite").value(true));
    }

    @Test
    void getStatus_ShouldReturnReadAndFavoriteStatus() throws Exception {
        // Given
        when(service.isRead("article-1")).thenReturn(true);
        when(service.isFavorite("article-1")).thenReturn(false);

        // When / Then
        mockMvc.perform(get("/api/articles/article-1/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true))
                .andExpect(jsonPath("$.isFavorite").value(false));
    }

    @Test
    void getStatus_WhenFavorite_ShouldReturnFavoriteTrue() throws Exception {
        // Given
        when(service.isRead("article-1")).thenReturn(false);
        when(service.isFavorite("article-1")).thenReturn(true);

        // When / Then
        mockMvc.perform(get("/api/articles/article-1/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(false))
                .andExpect(jsonPath("$.isFavorite").value(true));
    }
}