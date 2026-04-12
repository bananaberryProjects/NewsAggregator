package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.mapper.ArticleMapper;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.port.in.SearchArticlesUseCase;
import com.newsaggregator.domain.port.out.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service für Artikel-Suche.
 *
 * <p>Implementiert den Use Case:
 * {@link SearchArticlesUseCase}
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class ArticleSearchService implements SearchArticlesUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ArticleSearchService.class);

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    public ArticleSearchService(ArticleRepository articleRepository, ArticleMapper articleMapper) {
        this.articleRepository = articleRepository;
        this.articleMapper = articleMapper;
    }

    // ==================== Use Case: SearchArticles ====================

    @Override
    public List<Article> searchArticles(String query) {
        logger.debug("Suche nach Artikeln mit Query: {}", query);

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        return articleRepository.searchByQuery(query.trim());
    }

    /**
     * Sucht nach Artikeln und gibt DTOs zurück.
     */
    public List<ArticleDto> searchArticlesDto(String query) {
        return searchArticles(query).stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== Use Case: GetArticlesByFeed ====================

    @Override
    public List<Article> getArticlesByFeed(Long feedId) {
        logger.debug("Rufe Artikel für Feed ab: {}", feedId);
        return articleRepository.findByFeedId(feedId);
    }

    /**
     * Gibt Artikel eines Feeds als DTOs zurück.
     */
    public List<ArticleDto> getArticlesByFeedDto(Long feedId) {
        return getArticlesByFeed(feedId).stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== Zusätzliche Methoden ====================

    public List<ArticleDto> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }

    public ArticleDto getArticleById(Long id) {
        return articleRepository.findById(com.newsaggregator.domain.model.ArticleId.of(id))
                .map(articleMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Artikel nicht gefunden: " + id));
    }
}
