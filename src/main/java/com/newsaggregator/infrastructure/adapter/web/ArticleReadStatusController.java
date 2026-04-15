package com.newsaggregator.infrastructure.adapter.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.newsaggregator.application.service.ArticleReadStatusService;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleReadStatus;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = {"http://localhost:5173", "http://mac-mini.local:5173"})
public class ArticleReadStatusController {

    private ArticleReadStatusService service;

    @Autowired
    public void setService(ArticleReadStatusService service) {
        this.service = service;
    }

    @PostMapping("/{articleId}/read")
    public ResponseEntity<ArticleReadStatus> markAsRead(@PathVariable String articleId) {
        return ResponseEntity.ok(service.markAsRead(articleId));
    }

    @PostMapping("/{articleId}/unread")
    public ResponseEntity<ArticleReadStatus> markAsUnread(@PathVariable String articleId) {
        return ResponseEntity.ok(service.markAsUnread(articleId));
    }

    @PostMapping("/{articleId}/favorite")
    public ResponseEntity<ArticleReadStatus> toggleFavorite(@PathVariable String articleId) {
        return ResponseEntity.ok(service.toggleFavorite(articleId));
    }

    @GetMapping("/read")
    public ResponseEntity<List<ArticleReadStatus>> getReadArticles() {
        return ResponseEntity.ok(service.getReadArticles());
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<ArticleReadStatus>> getFavoriteArticles() {
        return ResponseEntity.ok(service.getFavoriteArticles());
    }

    @GetMapping("/{articleId}/status")
    public ResponseEntity<Map<String, Boolean>> getStatus(@PathVariable String articleId) {
        Map<String, Boolean> status = new HashMap<>();
        status.put("isRead", service.isRead(articleId));
        status.put("isFavorite", service.isFavorite(articleId));
        return ResponseEntity.ok(status);
    }
}
