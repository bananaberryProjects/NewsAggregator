package com.newsaggregator.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO für Lesestatistiken.
 */
public class ReadingStatisticsDto {
    
    // Gesamtanzahlen
    private long totalArticles;
    private long readArticles;
    private long unreadArticles;
    private long favoriteArticles;
    
    // Artikel pro Tag (letzte 30 Tage)
    private List<DailyStats> articlesPerDay;
    
    // Artikel pro Feed
    private List<FeedStats> articlesPerFeed;
    
    // Lesequote
    private double readPercentage;
    
    // Konstruktoren
    public ReadingStatisticsDto() {}
    
    public ReadingStatisticsDto(long totalArticles, long readArticles, long favoriteArticles,
                                List<DailyStats> articlesPerDay, List<FeedStats> articlesPerFeed) {
        this.totalArticles = totalArticles;
        this.readArticles = readArticles;
        this.unreadArticles = totalArticles - readArticles;
        this.favoriteArticles = favoriteArticles;
        this.articlesPerDay = articlesPerDay;
        this.articlesPerFeed = articlesPerFeed;
        this.readPercentage = totalArticles > 0 ? (double) readArticles / totalArticles * 100 : 0;
    }
    
    // Getter und Setter
    public long getTotalArticles() { return totalArticles; }
    public void setTotalArticles(long totalArticles) { 
        this.totalArticles = totalArticles;
        updateUnreadCount();
    }
    
    public long getReadArticles() { return readArticles; }
    public void setReadArticles(long readArticles) { 
        this.readArticles = readArticles;
        updateUnreadCount();
        updateReadPercentage();
    }
    
    public long getUnreadArticles() { return unreadArticles; }
    
    public long getFavoriteArticles() { return favoriteArticles; }
    public void setFavoriteArticles(long favoriteArticles) { this.favoriteArticles = favoriteArticles; }
    
    public List<DailyStats> getArticlesPerDay() { return articlesPerDay; }
    public void setArticlesPerDay(List<DailyStats> articlesPerDay) { this.articlesPerDay = articlesPerDay; }
    
    public List<FeedStats> getArticlesPerFeed() { return articlesPerFeed; }
    public void setArticlesPerFeed(List<FeedStats> articlesPerFeed) { this.articlesPerFeed = articlesPerFeed; }
    
    public double getReadPercentage() { return readPercentage; }
    public void setReadPercentage(double readPercentage) { this.readPercentage = readPercentage; }
    
    private void updateUnreadCount() {
        this.unreadArticles = this.totalArticles - this.readArticles;
    }
    
    private void updateReadPercentage() {
        this.readPercentage = this.totalArticles > 0 ? (double) this.readArticles / this.totalArticles * 100 : 0;
    }
    
    /**
     * Statistiken pro Tag.
     */
    public static class DailyStats {
        private LocalDate date;
        private long articleCount;
        private long readCount;
        
        public DailyStats() {}
        
        public DailyStats(LocalDate date, long articleCount, long readCount) {
            this.date = date;
            this.articleCount = articleCount;
            this.readCount = readCount;
        }
        
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public long getArticleCount() { return articleCount; }
        public void setArticleCount(long articleCount) { this.articleCount = articleCount; }
        
        public long getReadCount() { return readCount; }
        public void setReadCount(long readCount) { this.readCount = readCount; }
    }
    
    /**
     * Statistiken pro Feed.
     */
    public static class FeedStats {
        private String feedName;
        private long totalArticles;
        private long readArticles;
        
        public FeedStats() {}
        
        public FeedStats(String feedName, long totalArticles, long readArticles) {
            this.feedName = feedName;
            this.totalArticles = totalArticles;
            this.readArticles = readArticles;
        }
        
        public String getFeedName() { return feedName; }
        public void setFeedName(String feedName) { this.feedName = feedName; }
        
        public long getTotalArticles() { return totalArticles; }
        public void setTotalArticles(long totalArticles) { this.totalArticles = totalArticles; }
        
        public long getReadArticles() { return readArticles; }
        public void setReadArticles(long readArticles) { this.readArticles = readArticles; }
    }
}
