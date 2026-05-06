package com.newsaggregator.application.dto;

import java.util.List;

/**
 * DTO für Trending-Themen aus der Hybrid-Statistik + KI-Filterung.
 */
public class TrendingTopicDto {

    private String window;
    private String generatedAt;
    private List<Topic> topics;
    private List<BreakingAlert> breakingAlerts;

    public TrendingTopicDto() {}

    public TrendingTopicDto(String window, String generatedAt,
                            List<Topic> topics, List<BreakingAlert> breakingAlerts) {
        this.window = window;
        this.generatedAt = generatedAt;
        this.topics = topics;
        this.breakingAlerts = breakingAlerts;
    }

    public String getWindow() { return window; }
    public void setWindow(String window) { this.window = window; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public List<Topic> getTopics() { return topics; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }

    public List<BreakingAlert> getBreakingAlerts() { return breakingAlerts; }
    public void setBreakingAlerts(List<BreakingAlert> breakingAlerts) { this.breakingAlerts = breakingAlerts; }

    /**
     * Einzelnes Trending-Thema mit Statistik.
     */
    public static class Topic {
        private String term;
        private int count;
        private String trend;       // up | down | stable
        private int deltaPercent;
        private int feeds;
        private boolean breaking;

        public Topic() {}

        public Topic(String term, int count, String trend, int deltaPercent, int feeds, boolean breaking) {
            this.term = term;
            this.count = count;
            this.trend = trend;
            this.deltaPercent = deltaPercent;
            this.feeds = feeds;
            this.breaking = breaking;
        }

        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }

        public int getDeltaPercent() { return deltaPercent; }
        public void setDeltaPercent(int deltaPercent) { this.deltaPercent = deltaPercent; }

        public int getFeeds() { return feeds; }
        public void setFeeds(int feeds) { this.feeds = feeds; }

        public boolean isBreaking() { return breaking; }
        public void setBreaking(boolean breaking) { this.breaking = breaking; }
    }

    /**
     * Explizite Breaking-News-Meldung für Top-Themen.
     */
    public static class BreakingAlert {
        private String term;
        private int newArticles;
        private int feedCount;
        private String snippet;

        public BreakingAlert() {}

        public BreakingAlert(String term, int newArticles, int feedCount, String snippet) {
            this.term = term;
            this.newArticles = newArticles;
            this.feedCount = feedCount;
            this.snippet = snippet;
        }

        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }

        public int getNewArticles() { return newArticles; }
        public void setNewArticles(int newArticles) { this.newArticles = newArticles; }

        public int getFeedCount() { return feedCount; }
        public void setFeedCount(int feedCount) { this.feedCount = feedCount; }

        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
    }
}
