package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.AddFeedCommand;
import com.newsaggregator.application.dto.AssignCategoriesCommand;
import com.newsaggregator.application.service.FeedManagementService;
import com.newsaggregator.application.dto.FeedDto;
import com.newsaggregator.application.mapper.FeedMapper;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.port.in.AddFeedUseCase;
import com.newsaggregator.domain.port.in.DeleteFeedUseCase;
import com.newsaggregator.domain.port.in.FetchFeedUseCase;
import com.newsaggregator.domain.port.in.GetAllFeedsUseCase;
import com.newsaggregator.domain.port.in.GetFeedByIdUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller für Feed-Management.
 *
 * <p>Dieser Controller delegiert alle Anfragen an die Application Services über Use-Case Interfaces.
 * Er enthält keine Business-Logik, nur HTTP-spezifische Konvertierungen.</p>
 */
@RestController
@RequestMapping("/api/feeds")
public class FeedController {

    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    private final GetAllFeedsUseCase getAllFeedsUseCase;
    private final GetFeedByIdUseCase getFeedByIdUseCase;
    private final AddFeedUseCase addFeedUseCase;
    private final DeleteFeedUseCase deleteFeedUseCase;
    private final FeedManagementService feedManagementService;
    private final FetchFeedUseCase fetchFeedUseCase;
    private final FeedMapper feedMapper;

    public FeedController(GetAllFeedsUseCase getAllFeedsUseCase,
                          GetFeedByIdUseCase getFeedByIdUseCase,
                          AddFeedUseCase addFeedUseCase,
                          DeleteFeedUseCase deleteFeedUseCase,
                          FeedManagementService feedManagementService,
                          FetchFeedUseCase fetchFeedUseCase,
                          FeedMapper feedMapper) {
        this.getAllFeedsUseCase = getAllFeedsUseCase;
        this.getFeedByIdUseCase = getFeedByIdUseCase;
        this.addFeedUseCase = addFeedUseCase;
        this.deleteFeedUseCase = deleteFeedUseCase;
        this.feedManagementService = feedManagementService;
        this.fetchFeedUseCase = fetchFeedUseCase;
        this.feedMapper = feedMapper;
    }

    /**
     * Gibt alle Feeds zurück.
     */
    @GetMapping
    public List<FeedDto> getAllFeeds() {
        logger.debug("GET /api/feeds aufgerufen");
        return getAllFeedsUseCase.getAllFeeds().stream()
                .map(feedMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Gibt einen einzelnen Feed zurück.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedDto> getFeed(@PathVariable Long id) {
        logger.debug("GET /api/feeds/{} aufgerufen", id);
        try {
            var feed = getFeedByIdUseCase.getFeedById(id);
            return ResponseEntity.ok(feedMapper.toDto(feed));
        } catch (IllegalArgumentException e) {
            logger.warn("Feed nicht gefunden: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Erstellt einen neuen Feed.
     */
    @PostMapping
    public ResponseEntity<FeedDto> addFeed(@RequestBody AddFeedCommand command) {
        logger.info("POST /api/feeds aufgerufen: {}", command.getName());

        try {
            var feed = addFeedUseCase.addFeed(command.getName(), command.getUrl(), command.getDescription());
            return ResponseEntity.ok(feedMapper.toDto(feed));
        } catch (IllegalArgumentException e) {
            logger.warn("Fehler beim Erstellen des Feeds: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Ruft einen Feed manuell ab.
     */
    @PostMapping("/{id}/fetch")
    public ResponseEntity<Void> fetchFeed(@PathVariable Long id) {
        logger.info("POST /api/feeds/{}/fetch aufgerufen", id);

        try {
            fetchFeedUseCase.fetchFeed(FeedId.of(id));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Feed nicht gefunden: {}", id);
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            logger.error("Fehler beim Abrufen des Feeds: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Weist Kategorien zu.
     */
    @PutMapping("/{id}/categories")
    public ResponseEntity<Void> assignCategories(
            @PathVariable Long id,
            @RequestBody AssignCategoriesCommand command) {
        logger.info("PUT /api/feeds/{}/categories aufgerufen", id);
        
        try {
            feedManagementService.assignCategoriesToFeed(id, command.categoryIds());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Feed nicht gefunden: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Löscht einen Feed.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long id) {
        logger.info("DELETE /api/feeds/{} aufgerufen", id);

        try {
            deleteFeedUseCase.deleteFeed(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Feed nicht gefunden: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
