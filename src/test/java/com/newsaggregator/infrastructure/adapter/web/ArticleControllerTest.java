package com.newsaggregator.infrastructure.adapter.web;

import java.time.LocalDateTime;
import java.util.List;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private ObjectMapper objectMapper;

    @Mock
    private ArticleSearchService articleSearchService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        ArticleController controller = new ArticleController(articleSearchService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
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

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<ArticleDto> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody).hasSize(2);
        assertThat(responseBody.get(0).getId()).isEqualTo(1L);
        assertThat(responseBody.get(0).getTitle()).isEqualTo("Test Article 1");
        assertThat(responseBody.get(0).getDescription()).isEqualTo("Description 1");
        assertThat(responseBody.get(0).getLink()).isEqualTo("http://example.com/1");
        assertThat(responseBody.get(0).getFeedName()).isEqualTo("Feed 1");
        assertThat(responseBody.get(1).getId()).isEqualTo(2L);
        assertThat(responseBody.get(1).getTitle()).isEqualTo("Test Article 2");
    }

    @Test
    void getAllArticles_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(articleSearchService.getAllArticles()).thenReturn(List.of());

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<ArticleDto> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody).isEmpty();
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

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        ArticleDto responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), ArticleDto.class);
        assertThat(responseBody.getId()).isEqualTo(1L);
        assertThat(responseBody.getTitle()).isEqualTo("Test Article");
        assertThat(responseBody.getDescription()).isEqualTo("Description");
        assertThat(responseBody.getLink()).isEqualTo("http://example.com/article");
        assertThat(responseBody.getImageUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(responseBody.getFeedName()).isEqualTo("Test Feed");
    }

    @Test
    void getArticle_WithNonExistingId_ShouldReturn404() throws Exception {
        // Given
        when(articleSearchService.getArticleById(999L))
                .thenThrow(new IllegalArgumentException("Artikel nicht gefunden: 999"));

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
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

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/search")
                .param("query", "Java")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<ArticleDto> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody).hasSize(1);
        assertThat(responseBody.get(0).getTitle()).isEqualTo("Java News");
        assertThat(responseBody.get(0).getDescription()).isEqualTo("Latest Java updates");
    }

    @Test
    void searchArticles_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        // Given
        when(articleSearchService.searchArticlesDto("nonexistent")).thenReturn(List.of());

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/search")
                .param("query", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<ArticleDto> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody).isEmpty();
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

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/feed/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<ArticleDto> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody).hasSize(2);
        assertThat(responseBody.get(0).getFeedId()).isEqualTo(1L);
        assertThat(responseBody.get(0).getFeedName()).isEqualTo("My Feed");
        assertThat(responseBody.get(1).getFeedId()).isEqualTo(1L);
    }

    @Test
    void getArticlesByFeed_WithNoArticles_ShouldReturnEmptyList() throws Exception {
        // Given
        when(articleSearchService.getArticlesByFeedDto(1L)).thenReturn(List.of());

        // When
        @SuppressWarnings("null")
        MvcResult result = mockMvc.perform(get("/api/articles/feed/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        List<ArticleDto> responseBody = objectMapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>() {});
        assertThat(responseBody).isEmpty();
    }
}
