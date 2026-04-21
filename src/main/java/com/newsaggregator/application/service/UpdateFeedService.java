package com.newsaggregator.application.service;

import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.port.in.UpdateFeedUseCase;
import com.newsaggregator.domain.port.out.FeedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application Service für das Aktualisieren von Feeds.
 */
@Service
@Transactional
public class UpdateFeedService implements UpdateFeedUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UpdateFeedService.class);

    private final FeedRepository feedRepository;

    public UpdateFeedService(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    @Override
    public Feed updateFeed(Long id, String name, String url, String description, Boolean extractContent) {
        logger.info("Aktualisiere Feed mit ID {}: name={}, url={}, extractContent={}", id, name, url, extractContent);

        // Validierung
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Feed-Name darf nicht leer sein");
        }
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Feed-URL darf nicht leer sein");
        }

        // Feed laden
        Optional<Feed> existingFeed = feedRepository.findById(FeedId.of(id));
        if (existingFeed.isEmpty()) {
            throw new IllegalArgumentException("Feed mit ID " + id + " nicht gefunden");
        }

        // Prüfen ob URL bereits von anderem Feed verwendet wird
        Optional<Feed> feedWithSameUrl = feedRepository.findByUrl(url.trim());
        if (feedWithSameUrl.isPresent() && !feedWithSameUrl.get().getId().equals(FeedId.of(id))) {
            throw new IllegalArgumentException("Ein Feed mit dieser URL existiert bereits");
        }

        Feed feed = existingFeed.get();
        if (extractContent != null) {
            feed.update(name.trim(), url.trim(), description != null ? description.trim() : null, extractContent);
        } else {
            feed.update(name.trim(), url.trim(), description != null ? description.trim() : null);
        }
        
        Feed updatedFeed = feedRepository.save(feed);
        logger.info("Feed {} erfolgreich aktualisiert", id);
        
        return updatedFeed;
    }
}
