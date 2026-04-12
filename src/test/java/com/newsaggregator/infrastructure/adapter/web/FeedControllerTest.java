package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.mapper.FeedMapper;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.domain.port.in.AddFeedUseCase;
import com.newsaggregator.domain.port.in.DeleteFeedUseCase;
import com.newsaggregator.domain.port.in.FetchFeedUseCase;
import com.newsaggregator.domain.port.in.GetAllFeedsUseCase;
import com.newsaggregator.domain.port.in.GetFeedByIdUseCase;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-Test für FeedController.
 *
 * <p>Testet den REST Controller für Feeds mit MockMvc.
 * Verwendet MockitoExtension statt @WebMvcTest, um Probleme mit Java 25 zu vermeiden.</p>
 */
@ExtendWith(MockitoExtension.class)
class FeedControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetAllFeedsUseCase getAllFeedsUseCase;

    @Mock
    private GetFeedByIdUseCase getFeedByIdUseCase;

    @Mock
    private AddFeedUseCase addFeedUseCase;

    @Mock
    private DeleteFeedUseCase deleteFeedUseCase;

    @Mock
    private FetchFeedUseCase fetchFeedUseCase;

    @Mock
    private ArticleJpaRepository articleJpaRepository;

    private FeedMapper feedMapper;

    @BeforeEach
    void setUp() {
        feedMapper = new FeedMapper(articleJpaRepository);
        FeedController feedController = new FeedController(
                getAllFeedsUseCase,
                getFeedByIdUseCase,
                addFeedUseCase,
                deleteFeedUseCase,
                fetchFeedUseCase,
                feedMapper
        );
        mockMvc = MockMvcBuilders.standaloneSetup(feedController).build();
    }

    private Feed createTestFeed(Long id, String name, String url) {
        return Feed.of(
                FeedId.of(id),
                name,
                url,
                "Description",
                LocalDateTime.now(),
                null,
                FeedStatus.ACTIVE
        );
    }

    @Test
    void getAllFeeds_ShouldReturnListOfFeeds() throws Exception {
        // Given
        Feed feed = createTestFeed(1L, "Feed 1", "https://example.com/1");
        
        when(getAllFeedsUseCase.getAllFeeds()).thenReturn(List.of(feed));

        // When / Then
        mockMvc.perform(get("/api/feeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Feed 1"));
    }

    @Test
    void getFeed_ShouldReturnFeed_WhenExists() throws Exception {
        // Given
        Feed feed = createTestFeed(1L, "Test Feed", "https://example.com/feed");
        
        when(getFeedByIdUseCase.getFeedById(1L)).thenReturn(feed);

        // When / Then
        mockMvc.perform(get("/api/feeds/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Feed"));
    }

    @Test
    void getFeed_ShouldReturn404_WhenNotExists() throws Exception {
        // Given
        when(getFeedByIdUseCase.getFeedById(1L)).thenThrow(new IllegalArgumentException("Not found"));

        // When / Then
        mockMvc.perform(get("/api/feeds/1"))
                .andExpect(status().isNotFound());
    }

    @SuppressWarnings("null")
    @Test
    void addFeed_ShouldCreateAndReturnFeed() throws Exception {
        // Given
        Feed feed = createTestFeed(1L, "Test", "https://example.com/feed");
        
        when(addFeedUseCase.addFeed("Test", "https://example.com/feed", "Description")).thenReturn(feed);

        // When / Then
        mockMvc.perform(post("/api/feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"url\":\"https://example.com/feed\",\"description\":\"Description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @SuppressWarnings("null")
    @Test
    void addFeed_ShouldReturn400_WhenInvalid() throws Exception {
        // Given
        when(addFeedUseCase.addFeed(any(), any(), any())).thenThrow(new IllegalArgumentException("Invalid"));

        // When / Then
        mockMvc.perform(post("/api/feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"url\":\"https://example.com/feed\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetchFeed_ShouldReturn200_WhenSuccessful() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/feeds/1/fetch"))
                .andExpect(status().isOk());

        verify(fetchFeedUseCase).fetchFeed(FeedId.of(1L));
    }

    @Test
    void fetchFeed_ShouldReturn404_WhenFeedNotFound() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Not found")).when(fetchFeedUseCase).fetchFeed(FeedId.of(1L));

        // When / Then
        mockMvc.perform(post("/api/feeds/1/fetch"))
                .andExpect(status().isNotFound());
    }

    @Test
    void fetchFeed_ShouldReturn400_WhenErrorOccurs() throws Exception {
        // Given
        doThrow(new RuntimeException("Error")).when(fetchFeedUseCase).fetchFeed(FeedId.of(1L));

        // When / Then
        mockMvc.perform(post("/api/feeds/1/fetch"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteFeed_ShouldReturn200_WhenSuccessful() throws Exception {
        // When / Then
        mockMvc.perform(delete("/api/feeds/1"))
                .andExpect(status().isOk());

        verify(deleteFeedUseCase).deleteFeed(1L);
    }

    @Test
    void deleteFeed_ShouldReturn404_WhenNotExists() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Not found")).when(deleteFeedUseCase).deleteFeed(1L);

        // When / Then
        mockMvc.perform(delete("/api/feeds/1"))
                .andExpect(status().isNotFound());
    }
}
