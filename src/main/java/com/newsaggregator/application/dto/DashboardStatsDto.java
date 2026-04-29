package com.newsaggregator.application.dto;

import java.time.Instant;

/**
 * DTO für Dashboard Morning-Briefing Statistiken.
 */
public class DashboardStatsDto {

    private long unreadCount;
    private long totalFeeds;
    private long feedsWithNewArticles;
    private long favoriteCount;
    private long newFavoritesToday;
    private long readStreakDays;
    private long articlesReadToday;
    private Instant lastReadAt;

    public DashboardStatsDto() {}

    public DashboardStatsDto(long unreadCount, long totalFeeds, long feedsWithNewArticles,
                             long favoriteCount, long newFavoritesToday,
                             long readStreakDays, long articlesReadToday,
                             Instant lastReadAt) {
        this.unreadCount = unreadCount;
        this.totalFeeds = totalFeeds;
        this.feedsWithNewArticles = feedsWithNewArticles;
        this.favoriteCount = favoriteCount;
        this.newFavoritesToday = newFavoritesToday;
        this.readStreakDays = readStreakDays;
        this.articlesReadToday = articlesReadToday;
        this.lastReadAt = lastReadAt;
    }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }

    public long getTotalFeeds() { return totalFeeds; }
    public void setTotalFeeds(long totalFeeds) { this.totalFeeds = totalFeeds; }

    public long getFeedsWithNewArticles() { return feedsWithNewArticles; }
    public void setFeedsWithNewArticles(long feedsWithNewArticles) { this.feedsWithNewArticles = feedsWithNewArticles; }

    public long getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(long favoriteCount) { this.favoriteCount = favoriteCount; }

    public long getNewFavoritesToday() { return newFavoritesToday; }
    public void setNewFavoritesToday(long newFavoritesToday) { this.newFavoritesToday = newFavoritesToday; }

    public long getReadStreakDays() { return readStreakDays; }
    public void setReadStreakDays(long readStreakDays) { this.readStreakDays = readStreakDays; }

    public long getArticlesReadToday() { return articlesReadToday; }
    public void setArticlesReadToday(long articlesReadToday) { this.articlesReadToday = articlesReadToday; }

    public Instant getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(Instant lastReadAt) { this.lastReadAt = lastReadAt; }
}
