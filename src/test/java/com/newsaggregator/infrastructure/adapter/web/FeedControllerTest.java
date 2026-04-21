package com.newsaggregator.infrastructure.adapter.web;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.newsaggregator.application.dto.AssignCategoriesCommand;
import com.newsaggregator.application.dto.UpdateFeedCommand;
import com.newsaggregator.application.mapper.FeedMapper;
import com.newsaggregator.application.service.FeedManagementService;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.domain.port.in.AddFeedUseCase;
import com.newsaggregator.domain.port.in.DeleteFeedUseCase;
import com.newsaggregator.domain.port.in.FetchFeedUseCase;
import com.newsaggregator.domain.port.in.GetAllFeedsUseCase;
import com.newsaggregator.domain.port.in.GetFeedByIdUseCase;
import com.newsaggregator.domain.port.in.UpdateFeedUseCase;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;

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
    private UpdateFeedUseCase updateFeedUseCase;

    @Mock
    private ArticleJpaRepository articleJpaRepository;

@Mock
    private FeedManagementService feedManagementService;

    private FeedMapper feedMapper;

    @BeforeEach
    void setUp() {
        feedMapper = new FeedMapper(articleJpaRepository);
        FeedController feedController = new FeedController(
                getAllFeedsUseCase,
                getFeedByIdUseCase,
                addFeedUseCase,
                deleteFeedUseCase,
                feedManagementService, 
                fetchFeedUseCase,
                updateFeedUseCase,
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

    @Test
    void updateFeed_WithValidData_ShouldReturnUpdatedFeed() throws Exception {
        // Given
        Feed updatedFeed = createTestFeed(1L, "Updated Feed", "https://newurl.com/feed");

        when(updateFeedUseCase.updateFeed(1L, "Updated Feed", "https://newurl.com/feed", "Updated Description", true))
                .thenReturn(updatedFeed);

        UpdateFeedCommand command = new UpdateFeedCommand();
        command.setName("Updated Feed");
        command.setUrl("https://newurl.com/feed");
        command.setDescription("Updated Description");
        command.setExtractContent(true);

        // When / Then
        mockMvc.perform(put("/api/feeds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Feed\",\"url\":\"https://newurl.com/feed\",\"description\":\"Updated Description\",\"extractContent\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Feed"));
    }

    @Test
    void updateFeed_WithNonExistingId_ShouldReturn404() throws Exception {
        // Given
        when(updateFeedUseCase.updateFeed(1L, "Updated Feed", "https://newurl.com/feed", "Updated Description", true))
                .thenThrow(new IllegalArgumentException("Feed mit ID 1 nicht gefunden"));

        UpdateFeedCommand command = new UpdateFeedCommand();
        command.setName("Updated Feed");
        command.setUrl("https://newurl.com/feed");
        command.setDescription("Updated Description");

        // When / Then
        mockMvc.perform(put("/api/feeds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Feed\",\"url\":\"https://newurl.com/feed\",\"description\":\"Updated Description\",\"extractContent\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateFeed_WithInvalidData_ShouldReturn400() throws Exception {
        // Given
        when(updateFeedUseCase.updateFeed(1L, "", "https://newurl.com/feed", "Updated Description", false))
                .thenThrow(new IllegalArgumentException("Feed-Name darf nicht leer sein"));

        // When / Then
        mockMvc.perform(put("/api/feeds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"url\":\"https://newurl.com/feed\",\"description\":\"Updated Description\",\"extractContent\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFeed_WithDuplicateUrl_ShouldReturn400() throws Exception {
        // Given
        when(updateFeedUseCase.updateFeed(1L, "Updated Feed", "https://duplicate.com/feed", "Updated Description", true))
                .thenThrow(new IllegalArgumentException("Ein Feed mit dieser URL existiert bereits"));

        // When / Then
        mockMvc.perform(put("/api/feeds/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Feed\",\"url\":\"https://duplicate.com/feed\",\"description\":\"Updated Description\",\"extractContent\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignCategories_WithValidData_ShouldReturn200() throws Exception {
        // Given
        AssignCategoriesCommand command = new AssignCategoriesCommand(java.util.List.of("550e8400-e29b-41d4-a716-446655440000"));

        // When / Then
        mockMvc.perform(put("/api/feeds/1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryIds\":[\"550e8400-e29b-41d4-a716-446655440000\"]}"))
                .andExpect(status().isOk());

        verify(feedManagementService).assignCategoriesToFeed(1L, command.categoryIds());
    }

    @Test
    void assignCategories_WithNonExistingFeed_ShouldReturn404() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Feed nicht gefunden"))
                .when(feedManagementService).assignCategoriesToFeed(1L, java.util.List.of("550e8400-e29b-41d4-a716-446655440000"));

        // When / Then
        mockMvc.perform(put("/api/feeds/1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryIds\":[\"550e8400-e29b-41d4-a716-446655440000\"]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignCategories_WithEmptyList_ShouldReturn200() throws Exception {
        // Given
        AssignCategoriesCommand command = new AssignCategoriesCommand(java.util.List.of());

        // When / Then
        mockMvc.perform(put("/api/feeds/1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryIds\":[]}"))
                .andExpect(status().isOk());

        verify(feedManagementService).assignCategoriesToFeed(1L, command.categoryIds());
    }

    @Test
    void getAllFeeds_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(getAllFeedsUseCase.getAllFeeds()).thenReturn(java.util.List.of());

        // When / Then
        mockMvc.perform(get("/api/feeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void addFeed_WithNullDescription_ShouldCreateFeed() throws Exception {
        // Given
        Feed feed = createTestFeed(1L, "Test", "https://example.com/feed");

        when(addFeedUseCase.addFeed("Test", "https://example.com/feed", null)).thenReturn(feed);

        // When / Then
        mockMvc.perform(post("/api/feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"url\":\"https://example.com/feed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addFeed_WithEmptyDescription_ShouldCreateFeed() throws Exception {
        // Given
        Feed feed = createTestFeed(1L, "Test", "https://example.com/feed");

        when(addFeedUseCase.addFeed("Test", "https://example.com/feed", "")).thenReturn(feed);

        // When / Then
        mockMvc.perform(post("/api/feeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"url\":\"https://example.com/feed\",\"description\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
