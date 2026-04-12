package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.mapper.ArticleMapper;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.port.in.FetchFeedUseCase;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.domain.port.out.FeedRepository;
import com.newsaggregator.domain.port.out.RssFeedReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service für Feed-Abruf.
 *
 * <p>Implementiert den Use Case:
 * {@link FetchFeedUseCase}
 * </p>
 */
@Service
@Transactional
public class FeedFetchingService implements FetchFeedUseCase {

    private static final Logger logger = LoggerFactory.getLogger(FeedFetchingService.class);

    private final FeedRepository feedRepository;
    private final ArticleRepository articleRepository;
    private final RssFeedReader rssFeedReader;
    private final ArticleMapper articleMapper;

    public FeedFetchingService(FeedRepository feedRepository,
                                ArticleRepository articleRepository,
                                RssFeedReader rssFeedReader,
                                ArticleMapper articleMapper) {
        this.feedRepository = feedRepository;
        this.articleRepository = articleRepository;
        this.rssFeedReader = rssFeedReader;
        this.articleMapper = articleMapper;
    }

    // ==================== Use Case: FetchFeed ====================

    @Override
    public void fetchFeed(FeedId feedId) {
        logger.info("Rufe Feed ab: {}", feedId);

        // Feed laden
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("Feed nicht gefunden: " + feedId));

        // Prüfen, ob Feed abrufbar ist
        if (!feed.canBeFetched()) {
            throw new IllegalStateException("Feed ist deaktiviert und kann nicht abgerufen werden: " + feedId);
        }

        // Feed lesen
        try {
            List<Article> newArticles = rssFeedReader.readFeed(feed);
            int savedCount = 0;

            // Neue Artikel speichern (Duplikate überspringen)
            for (Article article : newArticles) {
                if (!articleRepository.existsByLink(article.getLink())) {
                    articleRepository.save(article);
                    savedCount++;
                }
            }

            // Feed als erfolgreich abgerufen markieren
            feed.markAsFetched();
            feedRepository.save(feed);

            logger.info("Feed '{}' abgerufen: {} neue Artikel ({} bereits vorhanden)",
                    feed.getName(), savedCount, newArticles.size() - savedCount);

        } catch (RssFeedReader.RssReadException e) {
            // Feed als fehlerhaft markieren
            feed.markAsError();
            feedRepository.save(feed);

            logger.error("Fehler beim Abrufen des Feeds '{}': {}", feed.getName(), e.getMessage());
            throw new RuntimeException("Feed konnte nicht abgerufen werden: " + e.getMessage(), e);
        }
    }

    /**
     * Convenience-Methode für manuelles Abrufen.
     */
    public List<ArticleDto> fetchFeed(Long feedId) {
        fetchFeed(FeedId.of(feedId));

        // Artikel des Feeds zurückgeben
        return articleRepository.findByFeedId(feedId).stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== Scheduled Fetch ====================

    @Override
    @Scheduled(fixedDelay = 3600000) // Jede Stunde
    public void fetchAllActiveFeeds() {
        logger.info("Starte automatischen Abruf aller Feeds");

        List<Feed> feeds = feedRepository.findAll();
        int successCount = 0;
        int errorCount = 0;

        for (Feed feed : feeds) {
            if (!feed.canBeFetched()) {
                continue; // Überspringe deaktivierte Feeds
            }

            try {
                fetchFeed(feed.getId());
                successCount++;
            } catch (Exception e) {
                logger.error("Fehler beim Abrufen von Feed '{}': {}", feed.getName(), e.getMessage());
                errorCount++;
            }
        }

        logger.info("Automatischer Abruf abgeschlossen: {} erfolgreich, {} fehlgeschlagen",
                successCount, errorCount);
    }
}
