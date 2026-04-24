package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.service.ArticleSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller fuer Artikel-Operationen.
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
     * Gibt alle Artikel zurueck.
     */
    @GetMapping
    public List<ArticleDto> getAllArticles() {
        logger.debug("GET /api/articles aufgerufen");
        return articleSearchService.getAllArticles();
    }

    /**
     * Gibt einen einzelnen Artikel zurueck.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDto> getArticle(@PathVariable Long id) {
        logger.debug("GET /api/articles/{} aufgerufen", id);
        try {
            return ResponseEntity.ok(articleSearchService.getArticleById(id));
        } catch (IllegalArgumentException e) {
            logger.warn("Artikel nicht gefunden: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * FULL-TEXT SEARCH (PostgreSQL tsvector).
     *
     * @param q        Suchbegriff (z.B. "Bundestagswahl 2024")
     * @param page     Seitennummer (0-basiert)
     * @param size     Groesse der Ergebnisliste
     * @param sort     Sortierfeld (publishedAt, createdAt, searchRank)
     * @param direction asc/desc
     */
    @GetMapping("/search")
    public Page<ArticleDto> searchArticles(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "searchRank") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        logger.debug("GET /api/articles/search?q={}", q);
        Pageable pageable = PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(
                        "desc".equalsIgnoreCase(direction) ?
                                org.springframework.data.domain.Sort.Direction.DESC :
                                org.springframework.data.domain.Sort.Direction.ASC,
                        sort));
        return articleSearchService.searchFullText(q, pageable);
    }

    /**
     * Gibt alle Artikel eines bestimmten Feeds zurueck.
     */
    @GetMapping("/feed/{feedId}")
    public ResponseEntity<List<ArticleDto>> getArticlesByFeed(@PathVariable Long feedId) {
        logger.debug("GET /api/articles/feed/{} aufgerufen", feedId);
        return ResponseEntity.ok(articleSearchService.getArticlesByFeedDto(feedId));
    }

    /**
     * Gibt den HTML-Inhalt eines Artikels zurueck (fuer Reader View).
     */
    @GetMapping("/{id}/content")
    public ResponseEntity<ArticleDto> getArticleContent(@PathVariable Long id) {
        logger.debug("GET /api/articles/{}/content aufgerufen", id);
        try {
            return ResponseEntity.ok(articleSearchService.getArticleById(id));
        } catch (IllegalArgumentException e) {
            logger.warn("Artikel nicht gefunden: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
