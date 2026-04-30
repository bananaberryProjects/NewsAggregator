package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.DashboardStatsDto;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.domain.port.out.FeedRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleReadStatus;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleReadStatusRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service für Dashboard Morning-Briefing Statistiken.
 */
@Service
public class DashboardStatsService {

    private final ArticleRepository articleRepository;
    private final ArticleReadStatusRepository readStatusRepository;
    private final FeedRepository feedRepository;

    private static final String CURRENT_USER = "user-001";

    public DashboardStatsService(ArticleRepository articleRepository,
                                  ArticleReadStatusRepository readStatusRepository,
                                  FeedRepository feedRepository) {
        this.articleRepository = articleRepository;
        this.readStatusRepository = readStatusRepository;
        this.feedRepository = feedRepository;
    }

    public DashboardStatsDto getDashboardStats() {
        var articles = articleRepository.findAll();
        var feeds = feedRepository.findAll();
        var readStatuses = readStatusRepository.findByUserId(CURRENT_USER);

        // Gelesene Artikel IDs
        Set<Long> readArticleIds = readStatuses.stream()
                .filter(ArticleReadStatus::isRead)
                .map(ArticleReadStatus::getArticleId)
                .collect(Collectors.toSet());

        long unreadCount = articles.stream()
                .filter(a -> !readArticleIds.contains(a.getId().getValue()))
                .count();

        long totalFeeds = feeds.size();

        // Feeds die mind. einen ungelesenen Artikel haben
        Set<Long> feedsWithUnread = articles.stream()
                .filter(a -> !readArticleIds.contains(a.getId().getValue()))
                .map(a -> a.getFeed().getId().getValue())
                .collect(Collectors.toSet());
        long feedsWithNewArticles = feedsWithUnread.size();

        // Favoriten
        long favoriteCount = readStatuses.stream()
                .filter(ArticleReadStatus::isFavorite)
                .count();

        LocalDate today = LocalDate.now();

        long newFavoritesToday = readStatuses.stream()
                .filter(ArticleReadStatus::isFavorite)
                .filter(s -> s.getFavoritedAt() != null)
                .filter(s -> s.getFavoritedAt().toLocalDate().equals(today))
                .count();

        // Lesestreak
        long readStreakDays = calculateReadStreak(readStatuses);

        // Gelesene Artikel heute
        long articlesReadToday = readStatuses.stream()
                .filter(ArticleReadStatus::isRead)
                .filter(s -> s.getReadAt() != null)
                .filter(s -> s.getReadAt().toLocalDate().equals(today))
                .count();

        // Letzter Lesezeitpunkt
        Instant lastReadAt = readStatuses.stream()
                .filter(ArticleReadStatus::isRead)
                .filter(s -> s.getReadAt() != null)
                .map(s -> s.getReadAt().atZone(ZoneId.systemDefault()).toInstant())
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new DashboardStatsDto(
                unreadCount,
                totalFeeds,
                feedsWithNewArticles,
                favoriteCount,
                newFavoritesToday,
                readStreakDays,
                articlesReadToday,
                lastReadAt
        );
    }

    /**
     * Berechnet die konsekutive Lesestreak:
     - Startet bei heute (inklusive)
     - Zählt solange es einen Tag gibt, an dem mindestens ein Artikel gelesen wurde
     - Bricht ab, sobald keine Lücke gefüllt ist
     */
    private long calculateReadStreak(List<ArticleReadStatus> readStatuses) {
        Set<LocalDate> readDates = readStatuses.stream()
                .filter(ArticleReadStatus::isRead)
                .filter(s -> s.getReadAt() != null)
                .map(s -> s.getReadAt().toLocalDate())
                .collect(Collectors.toSet());

        if (readDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate date = today;

        // Wenn heute noch nicht gelesen, pruefe gestern (Grace Period)
        if (!readDates.contains(date)) {
            date = date.minusDays(1);
            if (!readDates.contains(date)) {
                return 0;
            }
        }

        long streak = 0;
        while (readDates.contains(date)) {
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }
}
