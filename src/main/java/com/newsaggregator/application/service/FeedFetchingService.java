package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.mapper.ArticleMapper;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.port.in.FetchFeedUseCase;
import com.newsaggregator.domain.port.out.ArticleContentExtractor;
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
    private final ArticleContentExtractor contentExtractor;
    private final ArticleMapper articleMapper;
    private final AiSummaryService aiSummaryService;

    public FeedFetchingService(FeedRepository feedRepository,
                                ArticleRepository articleRepository,
                                RssFeedReader rssFeedReader,
                                ArticleContentExtractor contentExtractor,
                                ArticleMapper articleMapper,
                                AiSummaryService aiSummaryService) {
        this.feedRepository = feedRepository;
        this.articleRepository = articleRepository;
        this.rssFeedReader = rssFeedReader;
        this.contentExtractor = contentExtractor;
        this.articleMapper = articleMapper;
        this.aiSummaryService = aiSummaryService;
    }

    // ==================== Content Extraction ====================

    /**
     * Extrahiert den Content für einen Artikel mittels Readability4J.
     * Bei Fehlern wird der Artikel ohne Content zurückgegeben und als fehlgeschlagen markiert.
     *
     * @param article Der Artikel ohne Content
     * @param extractContent true wenn Content extrahiert werden soll
     * @return Der Artikel mit extrahiertem Content (wenn erfolgreich) oder ohne Content
     */
    private Article extractContentForArticle(Article article, boolean extractContent) {
        // Wenn Content-Extraktion deaktiviert ist, nicht versuchen
        if (!extractContent) {
            logger.debug("Content-Extraktion für Feed '{}' deaktiviert, überspringe Artikel '{}'",
                    article.getFeed().getName(), article.getTitle());
            return article;
        }

        try {
            if (!contentExtractor.canExtract(article.getLink())) {
                logger.debug("Content-Extraktion nicht möglich für URL: {}", article.getLink());
                return article.withExtractionFailed();
            }

            String extractedContent = contentExtractor.extractContent(article.getLink());

            if (extractedContent != null && !extractedContent.isBlank()) {
                logger.debug("Content extrahiert für '{}': {} Zeichen", article.getTitle(), extractedContent.length());
                return article.withExtractedContent(extractedContent);
            } else {
                // Leere Antwort = Fehler
                logger.warn("Leere Content-Antwort für '{}'", article.getLink());
                return article.withExtractionFailed();
            }

        } catch (ArticleContentExtractor.ContentExtractionException e) {
            logger.warn("Content-Extraktion fehlgeschlagen für '{}': {}", article.getLink(), e.getMessage());
            return article.withExtractionFailed();
        } catch (Exception e) {
            logger.error("Unerwarteter Fehler bei Content-Extraktion für '{}': {}", article.getLink(), e.getMessage());
            return article.withExtractionFailed();
        }
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
            int blockedCount = 0;
            int duplicateCount = 0;

            for (Article article : newArticles) {
                // 1. Keyword-Filter: Titel enthält geblocktes Keyword?
                if (feed.isTitleBlocked(article.getTitle())) {
                    logger.info("Artikel '{}' wird blockiert (Keyword-Filter)", article.getTitle());
                    blockedCount++;
                    continue;
                }

                // 2. Link-basierte Duplikat-Erkennung
                if (articleRepository.existsByLink(article.getLink())) {
                    logger.debug("Artikel '{}' existiert bereits (Link-Duplikat)", article.getTitle());
                    duplicateCount++;
                    continue;
                }

                // 3. Titel-basierte Duplikat-Erkennung
                if (articleRepository.existsByTitle(article.getTitle())) {
                    logger.debug("Artikel '{}' existiert bereits (Titel-Duplikat)", article.getTitle());
                    duplicateCount++;
                    continue;
                }

                // Content-Extraktion für neue Artikel durchführen (nur wenn Feed.extractContent true)
                Article articleWithContent = extractContentForArticle(article, feed.shouldExtractContent());
                articleRepository.save(articleWithContent);
                savedCount++;
            }

            // Feed als erfolgreich abgerufen markieren
            feed.markAsFetched();
            feedRepository.save(feed);

            logger.info("Feed '{}' abgerufen: {} neue Artikel ({} blockiert, {} Duplikate, {} bereits vorhanden)",
                    feed.getName(), savedCount, blockedCount, duplicateCount,
                    newArticles.size() - savedCount - blockedCount - duplicateCount);

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

        // KI-Summary-Cache invalidieren wenn neue Artikel importiert wurden
        if (successCount > 0) {
            aiSummaryService.invalidateTodayCache();
        }
    }
}
