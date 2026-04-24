package com.newsaggregator.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "article_read_status")
public class ArticleReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "is_favorite")
    private boolean isFavorite = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "favorited_at")
    private LocalDateTime favoritedAt;

    // Default constructor
    public ArticleReadStatus() {}

    public ArticleReadStatus(Long articleId, String userId) {
        this.articleId = articleId;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
        if (read) {
            this.readAt = LocalDateTime.now();
        }
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
        if (favorite && this.favoritedAt == null) {
            this.favoritedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getFavoritedAt() {
        return favoritedAt;
    }

    public void setFavoritedAt(LocalDateTime favoritedAt) {
        this.favoritedAt = favoritedAt;
    }
}
