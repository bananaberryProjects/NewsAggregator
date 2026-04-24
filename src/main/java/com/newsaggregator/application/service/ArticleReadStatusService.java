package com.newsaggregator.application.service;

import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleReadStatus;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleReadStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleReadStatusService {

    @Autowired
    private ArticleReadStatusRepository repository;

    // User ID aus dem Request (später durch Auth)
    private static final String CURRENT_USER = "user-001";

    public ArticleReadStatus markAsRead(Long articleId) {
        Optional<ArticleReadStatus> existing = repository.findByArticleIdAndUserId(articleId, CURRENT_USER);
        
        if (existing.isPresent()) {
            ArticleReadStatus status = existing.get();
            status.setRead(true);
            return repository.save(status);
        } else {
            ArticleReadStatus status = new ArticleReadStatus(articleId, CURRENT_USER);
            status.setRead(true);
            return repository.save(status);
        }
    }

    public ArticleReadStatus markAsUnread(Long articleId) {
        Optional<ArticleReadStatus> existing = repository.findByArticleIdAndUserId(articleId, CURRENT_USER);
        
        if (existing.isPresent()) {
            ArticleReadStatus status = existing.get();
            status.setRead(false);
            return repository.save(status);
        }
        return null;
    }

    public ArticleReadStatus toggleFavorite(Long articleId) {
        Optional<ArticleReadStatus> existing = repository.findByArticleIdAndUserId(articleId, CURRENT_USER);
        
        if (existing.isPresent()) {
            ArticleReadStatus status = existing.get();
            status.setFavorite(!status.isFavorite());
            return repository.save(status);
        } else {
            ArticleReadStatus status = new ArticleReadStatus(articleId, CURRENT_USER);
            status.setFavorite(true);
            return repository.save(status);
        }
    }

    public List<ArticleReadStatus> getAllByUser() {
        return repository.findByUserId(CURRENT_USER);
    }

    public List<ArticleReadStatus> getReadArticles() {
        return repository.findByUserIdAndIsRead(CURRENT_USER, true);
    }

    public List<ArticleReadStatus> getFavoriteArticles() {
        return repository.findByUserIdAndIsFavorite(CURRENT_USER, true);
    }

    public boolean isRead(Long articleId) {
        return repository.findByArticleIdAndUserId(articleId, CURRENT_USER)
                .map(ArticleReadStatus::isRead)
                .orElse(false);
    }

    public boolean isFavorite(Long articleId) {
        return repository.findByArticleIdAndUserId(articleId, CURRENT_USER)
                .map(ArticleReadStatus::isFavorite)
                .orElse(false);
    }
}
