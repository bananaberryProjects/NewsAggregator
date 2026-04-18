package com.newsaggregator.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.newsaggregator.application.dto.AddFeedCommand;
import com.newsaggregator.application.dto.FeedDto;
import com.newsaggregator.application.mapper.FeedMapper;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.in.AddFeedUseCase;
import com.newsaggregator.domain.port.in.DeleteFeedUseCase;
import com.newsaggregator.domain.port.in.GetAllFeedsUseCase;
import com.newsaggregator.domain.port.in.GetFeedByIdUseCase;
import com.newsaggregator.domain.port.out.FeedRepository;

/**
 * Application Service für Feed-Management.
 *
 * <p>Implementiert die Use Cases:
 * <ul>
 *   <li>{@link AddFeedUseCase}</li>
 *   <li>{@link GetAllFeedsUseCase}</li>
 *   <li>{@link GetFeedByIdUseCase}</li>
 *   <li>{@link DeleteFeedUseCase}</li>
 * </ul>
 * </p>
 */
@Service
@Transactional
public class FeedManagementService implements AddFeedUseCase, GetAllFeedsUseCase, GetFeedByIdUseCase, DeleteFeedUseCase {

    private static final Logger logger = LoggerFactory.getLogger(FeedManagementService.class);

    private final FeedRepository feedRepository;
    private final FeedMapper feedMapper;

    public FeedManagementService(FeedRepository feedRepository, FeedMapper feedMapper) {
        this.feedRepository = feedRepository;
        this.feedMapper = feedMapper;
    }

    // ==================== Use Case: AddFeed ====================

    @Override
    public Feed addFeed(String name, String url, String description) {
        logger.info("Füge neuen Feed hinzu: {}", name);

        // Prüfen, ob Feed bereits existiert
        if (feedRepository.existsByUrl(url)) {
            throw new IllegalArgumentException("Ein Feed mit dieser URL existiert bereits: " + url);
        }

        // Domain-Entity erstellen
        Feed feed = Feed.createNew(name, url, description);

        // Persistieren
        Feed savedFeed = feedRepository.save(feed);

        logger.info("Feed erfolgreich erstellt: {} (ID: {})", savedFeed.getName(), savedFeed.getId());
        return savedFeed;
    }

    /**
     * Convenience-Methode mit Command-DTO.
     */
    public FeedDto addFeed(AddFeedCommand command) {
        Feed feed = addFeed(command.getName(), command.getUrl(), command.getDescription());
        return feedMapper.toDto(feed);
    }

    // ==================== Use Case: GetAllFeeds ====================

    @Override
    @Transactional(readOnly = true)
    public List<Feed> getAllFeeds() {
        logger.debug("Rufe alle Feeds ab");
        return feedRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feed> getActiveFeeds() {
        logger.debug("Rufe aktive Feeds ab");
        return feedRepository.findAll().stream()
                .filter(feed -> feed.getStatus() == com.newsaggregator.domain.model.FeedStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    // ==================== Use Case: GetFeedById ====================

    @Override
    @Transactional(readOnly = true)
    public Feed getFeedById(Long id) {
        return feedRepository.findById(com.newsaggregator.domain.model.FeedId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Feed nicht gefunden: " + id));
    }

    // ==================== Use Case: DeleteFeed ====================

    @Override
    public void deleteFeed(Long id) {
        logger.info("Lösche Feed: {}", id);
        feedRepository.deleteById(com.newsaggregator.domain.model.FeedId.of(id));
    }

    // ==================== Legacy Methoden (für Rückwärtskompatibilität) ====================

    @Transactional(readOnly = true)
    public FeedDto getFeedByIdAsDto(Long id) {
        return feedMapper.toDto(getFeedById(id));
    }

    // ==================== Kategorie-Management ====================

    /**
     * Weist einem Feed Kategorien zu.
     */
    public void assignCategoriesToFeed(Long feedId, List<String> categoryIds) {
        logger.info("Weise Feed {} {} Kategorien zu", feedId, categoryIds.size());

        Feed feed = feedRepository.findById(com.newsaggregator.domain.model.FeedId.of(feedId))
                .orElseThrow(() -> new IllegalArgumentException("Feed nicht gefunden: " + feedId));

        // Kategorien setzen (setCategories löscht bestehende und setzt neue)
        if (categoryIds != null) {
            List<com.newsaggregator.domain.model.CategoryId> cats = categoryIds.stream()
                    .map(com.newsaggregator.domain.model.CategoryId::of)
                    .collect(Collectors.toList());
            feed.setCategories(cats);
        } else {
            feed.setCategories(List.of());
        }

        feedRepository.save(feed);
        logger.info("Kategorien erfolgreich zugewiesen zu Feed: {}", feedId);
    }
}
