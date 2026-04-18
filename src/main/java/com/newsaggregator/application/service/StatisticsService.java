package com.newsaggregator.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.newsaggregator.application.dto.ReadingStatisticsDto;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleReadStatus;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleReadStatusRepository;

/**
 * Service für Lesestatistiken.
 */
@Service
public class StatisticsService {

    private final ArticleRepository articleRepository;
    private final ArticleReadStatusRepository readStatusRepository;
    
    // User ID aus dem Request (später durch Auth)
    private static final String CURRENT_USER = "user-001";

    public StatisticsService(ArticleRepository articleRepository,
                           ArticleReadStatusRepository readStatusRepository) {
        this.articleRepository = articleRepository;
        this.readStatusRepository = readStatusRepository;
    }

    /**
     * Holt alle Lesestatistiken für den aktuellen User.
     */
    public ReadingStatisticsDto getStatistics() {
        var articles = articleRepository.findAll();
        var readStatuses = readStatusRepository.findByUserId(CURRENT_USER);
        
        long totalArticles = articles.size();
        long readArticles = readStatuses.stream().filter(ArticleReadStatus::isRead).count();
        long favoriteArticles = readStatuses.stream().filter(ArticleReadStatus::isFavorite).count();
        
        // Artikel pro Tag (letzte 30 Tage) - basierend auf Veröffentlichung und Lesezeit
        var articlesPerDay = calculateArticlesPerDay(articles, readStatuses);
        
        // Artikel pro Feed
        var articlesPerFeed = calculateArticlesPerFeed(articles, readStatuses);
        
        return new ReadingStatisticsDto(totalArticles, readArticles, favoriteArticles, 
                                         articlesPerDay, articlesPerFeed);
    }

    /**
     * Berechnet Artikel pro Tag für die letzten 30 Tage.
     * 
     * - articleCount: Neue Artikel nach Veröffentlichungsdatum
     * - readCount: Gelesene Artikel nach Lesedatum (readAt)
     */
    private List<ReadingStatisticsDto.DailyStats> calculateArticlesPerDay(
            List<com.newsaggregator.domain.model.Article> articles,
            List<ArticleReadStatus> readStatuses) {
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        
        // 1. Neue Artikel nach Veröffentlichungsdatum gruppieren
        var articlesByPublishedDate = articles.stream()
                .filter(a -> a.getPublishedAt() != null)
                .filter(a -> !a.getPublishedAt().toLocalDate().isBefore(thirtyDaysAgo))
                .collect(Collectors.groupingBy(
                    a -> a.getPublishedAt().toLocalDate(),
                    Collectors.counting()
                ));
        
        // 2. Gelesene Artikel nach Lesedatum (readAt) gruppieren
        var readByDate = readStatuses.stream()
                .filter(ArticleReadStatus::isRead)
                .filter(s -> s.getReadAt() != null)
                .filter(s -> !s.getReadAt().toLocalDate().isBefore(thirtyDaysAgo))
                .collect(Collectors.groupingBy(
                    s -> s.getReadAt().toLocalDate(),
                    Collectors.counting()
                ));
        
        // 3. Kombiniere beide Datensätze für jeden Tag
        // Erstelle einen Set aller Tage (von Veröffentlichungen und Lesevorgängen)
        var allDates = articlesByPublishedDate.keySet().stream()
                .collect(Collectors.toSet());
        allDates.addAll(readByDate.keySet());
        
        // Wenn keine Daten vorhanden, erstelle leere Liste für die letzten 30 Tage
        if (allDates.isEmpty()) {
            // Erstelle Einträge für die letzten 30 Tage mit 0 Werten
            return java.util.stream.IntStream.rangeClosed(0, 29)
                    .mapToObj(i -> {
                        LocalDate date = today.minusDays(29 - i);
                        return new ReadingStatisticsDto.DailyStats(date, 0, 0);
                    })
                    .collect(Collectors.toList());
        }
        
        return allDates.stream()
                .sorted()
                .map(date -> {
                    long articleCount = articlesByPublishedDate.getOrDefault(date, 0L);
                    long readCount = readByDate.getOrDefault(date, 0L);
                    return new ReadingStatisticsDto.DailyStats(date, articleCount, readCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Berechnet Artikel pro Feed.
     */
    private List<ReadingStatisticsDto.FeedStats> calculateArticlesPerFeed(
            List<com.newsaggregator.domain.model.Article> articles,
            List<ArticleReadStatus> readStatuses) {
        
        // Map für gelesene Artikel nach ID (als String)
        var readArticleIds = readStatuses.stream()
                .filter(ArticleReadStatus::isRead)
                .map(ArticleReadStatus::getArticleId)
                .collect(Collectors.toSet());
        
        // Gruppiere nach Feed
        var articlesByFeed = articles.stream()
                .filter(a -> a.getFeed() != null)
                .collect(Collectors.groupingBy(
                    a -> a.getFeed().getName(),
                    Collectors.toList()
                ));
        
        return articlesByFeed.entrySet().stream()
                .map(entry -> {
                    String feedName = entry.getKey();
                    var feedArticles = entry.getValue();
                    long totalCount = feedArticles.size();
                    long readCount = feedArticles.stream()
                            .filter(a -> readArticleIds.contains(String.valueOf(a.getId().getValue())))
                            .count();
                    return new ReadingStatisticsDto.FeedStats(feedName, totalCount, readCount);
                })
                .sorted((a, b) -> Long.compare(b.getTotalArticles(), a.getTotalArticles()))
                .limit(10) // Top 10 Feeds
                .collect(Collectors.toList());
    }
}
