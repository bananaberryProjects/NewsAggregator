package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.service.ArticleSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller für Artikel-Operationen.
 *
 * <p>Dieser Controller delegiert alle Anfragen an die Application Services.
 * Er enthält keine Business-Logik, nur HTTP-spezifische Konvertierungen.</p>
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    private final ArticleSearchService articleSearchService;

    public ArticleController(ArticleSearchService articleSearchService) {
        this.articleSearchService = articleSearchService;
    }

    /**
     * Gibt alle Artikel zurück.
     */
    @GetMapping
    public List<ArticleDto> getAllArticles() {
        logger.debug("GET /api/articles aufgerufen");
        return articleSearchService.getAllArticles();
    }

    /**
     * Gibt einen einzelnen Artikel zurück.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDto> getArticle(@PathVariable Long id) {
        logger.debug("GET /api/articles/{} aufgerufen", id);
        try {
            ArticleDto article = articleSearchService.getArticleById(id);
            return ResponseEntity.ok(article);
        } catch (IllegalArgumentException e) {
            logger.warn("Artikel nicht gefunden: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Sucht nach Artikeln.
     */
    @GetMapping("/search")
    public List<ArticleDto> searchArticles(@RequestParam String query) {
        logger.debug("GET /api/articles/search aufgerufen mit Query: {}", query);
        return articleSearchService.searchArticlesDto(query);
    }

    /**
     * Gibt alle Artikel eines bestimmten Feeds zurück.
     */
    @GetMapping("/feed/{feedId}")
    public ResponseEntity<List<ArticleDto>> getArticlesByFeed(@PathVariable Long feedId) {
        logger.debug("GET /api/articles/feed/{} aufgerufen", feedId);
        List<ArticleDto> articles = articleSearchService.getArticlesByFeedDto(feedId);
        return ResponseEntity.ok(articles);
    }

    /**
     * Gibt den HTML-Inhalt eines Artikels zurück (für Reader View).
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<ArticleDto> getArticleContent(@PathVariable Long id) {
        logger.debug("GET /api/articles/{}/content aufgerufen", id);
        try {
            ArticleDto article = articleSearchService.getArticleById(id);
            return ResponseEntity.ok(article);
        } catch (IllegalArgumentException e) {
            logger.warn("Artikel nicht gefunden: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
