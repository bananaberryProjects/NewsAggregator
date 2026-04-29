package com.newsaggregator.application.dto;

import java.util.List;

/**
 * DTO für strukturierte KI-Tageszusammenfassung v2.
 */
public class AiSummaryDto {

    private List<AiCategory> categories;
    private List<AiTopic> topTopics;
    private String generatedAt;

    public AiSummaryDto() {}

    public AiSummaryDto(List<AiCategory> categories, List<AiTopic> topTopics, String generatedAt) {
        this.categories = categories;
        this.topTopics = topTopics;
        this.generatedAt = generatedAt;
    }

    public List<AiCategory> getCategories() { return categories; }
    public void setCategories(List<AiCategory> categories) { this.categories = categories; }

    public List<AiTopic> getTopTopics() { return topTopics; }
    public void setTopTopics(List<AiTopic> topTopics) { this.topTopics = topTopics; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    /**
     * Kategorie mit Sentiment und Zusammenfassung.
     */
    public static class AiCategory {
        private String name;
        private String summary;
        private int articleCount;
        private String sentiment; // positive, neutral, negative

        public AiCategory() {}

        public AiCategory(String name, String summary, int articleCount, String sentiment) {
            this.name = name;
            this.summary = summary;
            this.articleCount = articleCount;
            this.sentiment = sentiment;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public int getArticleCount() { return articleCount; }
        public void setArticleCount(int articleCount) { this.articleCount = articleCount; }

        public String getSentiment() { return sentiment; }
        public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    }

    /**
     * Trending / Top-Topic.
     */
    public static class AiTopic {
        private String name;
        private int articleCount;
        private boolean trending;

        public AiTopic() {}

        public AiTopic(String name, int articleCount, boolean trending) {
            this.name = name;
            this.articleCount = articleCount;
            this.trending = trending;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getArticleCount() { return articleCount; }
        public void setArticleCount(int articleCount) { this.articleCount = articleCount; }

        public boolean isTrending() { return trending; }
        public void setTrending(boolean trending) { this.trending = trending; }
    }
}
