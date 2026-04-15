package com.newsaggregator.infrastructure.adapter.web;

import java.time.LocalDateTime;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.service.ArticleSearchService;

/**
 * Unit-Test für ArticleController.
 *
 * <p>Testet die REST-Endpunkte für Artikel-Operationen.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class ArticleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ArticleSearchService articleSearchService;

    @BeforeEach
    void setUp() {
        ArticleController controller = new ArticleController(articleSearchService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllArticles_ShouldReturnList() throws Exception {
        // Given
        ArticleDto article1 = ArticleDto.builder()
                .id(1L)
                .title("Test Article 1")
                .description("Description 1")
                .link("http://example.com/1")
                .publishedAt(LocalDateTime.now())
                .feedId(1L)
                .feedName("Feed 1")
                .build();

        ArticleDto article2 = ArticleDto.builder()
                .id(2L)
                .title("Test Article 2")
                .description("Description 2")
                .link("http://example.com/2")
                .publishedAt(LocalDateTime.now())
                .feedId(2L)
                .feedName("Feed 2")
                .build();

        when(articleSearchService.getAllArticles()).thenReturn(List.of(article1, article2));

        // When / Then
        mockMvc.perform(get("/api/articles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Article 1"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[0].link").value("http://example.com/1"))
                .andExpect(jsonPath("$[0].feedName").value("Feed 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Test Article 2"));
    }

    @Test
    void getAllArticles_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(articleSearchService.getAllArticles()).thenReturn(List.of());

        // When / Then
        mockMvc.perform(get("/api/articles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getArticle_WithExistingId_ShouldReturnArticle() throws Exception {
        // Given
        ArticleDto article = ArticleDto.builder()
                .id(1L)
                .title("Test Article")
                .description("Description")
                .link("http://example.com/article")
                .imageUrl("http://example.com/image.jpg")
                .publishedAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .feedId(1L)
                .feedName("Test Feed")
                .build();

        when(articleSearchService.getArticleById(1L)).thenReturn(article);

        // When / Then
        mockMvc.perform(get("/api/articles/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Article"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.link").value("http://example.com/article"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$.feedName").value("Test Feed"));
    }

    @Test
    void getArticle_WithNonExistingId_ShouldReturn404() throws Exception {
        // Given
        when(articleSearchService.getArticleById(999L))
                .thenThrow(new IllegalArgumentException("Artikel nicht gefunden: 999"));

        // When / Then
        mockMvc.perform(get("/api/articles/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchArticles_WithQuery_ShouldReturnResults() throws Exception {
        // Given
        ArticleDto article = ArticleDto.builder()
                .id(1L)
                .title("Java News")
                .description("Latest Java updates")
                .link("http://example.com/java")
                .publishedAt(LocalDateTime.now())
                .feedId(1L)
                .feedName("Tech Feed")
                .build();

        when(articleSearchService.searchArticlesDto("Java")).thenReturn(List.of(article));

        // When / Then
        mockMvc.perform(get("/api/articles/search")
                .param("query", "Java")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Java News"))
                .andExpect(jsonPath("$[0].description").value("Latest Java updates"));
    }

    @Test
    void searchArticles_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        // Given
        when(articleSearchService.searchArticlesDto("nonexistent")).thenReturn(List.of());

        // When / Then
        mockMvc.perform(get("/api/articles/search")
                .param("query", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getArticlesByFeed_WithExistingFeed_ShouldReturnArticles() throws Exception {
        // Given
        ArticleDto article1 = ArticleDto.builder()
                .id(1L)
                .title("Feed Article 1")
                .description("Description 1")
                .link("http://example.com/1")
                .publishedAt(LocalDateTime.now())
                .feedId(1L)
                .feedName("My Feed")
                .build();

        ArticleDto article2 = ArticleDto.builder()
                .id(2L)
                .title("Feed Article 2")
                .description("Description 2")
                .link("http://example.com/2")
                .publishedAt(LocalDateTime.now())
                .feedId(1L)
                .feedName("My Feed")
                .build();

        when(articleSearchService.getArticlesByFeedDto(1L)).thenReturn(List.of(article1, article2));

        // When / Then
        mockMvc.perform(get("/api/articles/feed/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].feedId").value(1))
                .andExpect(jsonPath("$[0].feedName").value("My Feed"))
                .andExpect(jsonPath("$[1].feedId").value(1));
    }

    @Test
    void getArticlesByFeed_WithNoArticles_ShouldReturnEmptyList() throws Exception {
        // Given
        when(articleSearchService.getArticlesByFeedDto(1L)).thenReturn(List.of());

        // When / Then
        mockMvc.perform(get("/api/articles/feed/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}