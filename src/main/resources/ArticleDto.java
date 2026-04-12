package com.newsaggregator.application.dto;

import java.time.LocalDateTime;

/**
 * DTO für Article-Informationen.
 *
 * <p>Wird für die Kommunikation zwischen Application Layer und Web Layer verwendet.
 * Enthält keine Domain-Logik, nur Daten.</p>
 */
public class ArticleDto {

    private Long id;
    private String title;
    private String description;
    private String link;
    private String imageUrl;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private Long feedId;
    private String feedName;

    // ==================== Konstruktoren ====================

    public ArticleDto() {
    }

    // ==================== Builder-Methoden ====================

    public static ArticleDto builder() {
        return new ArticleDto();
    }

    public ArticleDto id(Long id) {
        this.id = id;
        return this;
    }

    public ArticleDto title(String title) {
        this.title = title;
        return this;
    }

    public ArticleDto description(String description) {
        this.description = description;
        return this;
    }

    public ArticleDto link(String link) {
        this.link = link;
        return this;
    }

    public ArticleDto publishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    public ArticleDto createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ArticleDto feedId(Long feedId) {
        this.feedId = feedId;
        return this;
    }

    public ArticleDto imageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public ArticleDto feedName(String feedName) {
        this.feedName = feedName;
        return this;
    }

    public ArticleDto build() {
        return this;
    }

    // ==================== Getter ====================

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getFeedId() {
        return feedId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFeedName() {
        return feedName;
    }

    // ==================== Setter (für Deserialisierung) ====================

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setFeedId(Long feedId) {
        this.feedId = feedId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }
}
