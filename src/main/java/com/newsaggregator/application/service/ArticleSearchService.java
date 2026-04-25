package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.ArticleDto;
import com.newsaggregator.application.mapper.ArticleMapper;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.port.in.SearchArticlesUseCase;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.mapper.ArticlePersistenceMapper;
import com.newsaggregator.infrastructure.adapter.persistence.repository.ArticleJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Application Service fuer Artikel-Suche.
 */
@Service
@Transactional(readOnly = true)
public class ArticleSearchService implements SearchArticlesUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ArticleSearchService.class);
    private static final Pattern NON_WORD = Pattern.compile("[^\\w\\s-]");
    private static final String CURRENT_USER = "user-001";

    private final ArticleRepository articleRepository;
    private final ArticleJpaRepository articleJpaRepository;
    private final ArticleMapper articleMapper;
    private final ArticlePersistenceMapper articlePersistenceMapper;

    public ArticleSearchService(ArticleRepository articleRepository,
                                ArticleJpaRepository articleJpaRepository,
                                ArticleMapper articleMapper,
                                ArticlePersistenceMapper articlePersistenceMapper) {
        this.articleRepository = articleRepository;
        this.articleJpaRepository = articleJpaRepository;
        this.articleMapper = articleMapper;
        this.articlePersistenceMapper = articlePersistenceMapper;
    }

    // ==================== Use Case: SearchArticles (legacy LIKE) ====================
    @Override
    public List<Article> searchArticles(String query) {
        logger.debug("Suche nach Artikeln mit Query: {}", query);
        if (query == null || query.trim().isEmpty()) { return List.of(); }
        return articleRepository.searchByQuery(query.trim());
    }

    public List<ArticleDto> searchArticlesDto(String query) {
        return searchArticles(query).stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== Full-Text Search (PostgreSQL tsvector) ====================

    /**
     * PostgreSQL Full-Text Search mit Ranking.
     */
    public Page<ArticleDto> searchFullText(String rawQuery, Pageable pageable) {
        String tsQuery = normalizeToTsQuery(rawQuery);
        logger.info("FTS-Suche mit tsQuery: {}", tsQuery);
        return articleJpaRepository.searchByTextVector(tsQuery, pageable)
                .map(articlePersistenceMapper::toDomain)
                .map(articleMapper::toDto);
    }

    /**
     * PostgreSQL Full-Text Search mit Feed-Category- und Read-Status-Filtern.
     *
     * @param rawQuery        roher Nutzerinput
     * @param categoryId      UUID-String der Kategorie (null = egal)
     * @param readFilter     "READ", "UNREAD" oder null
     * @param favoriteFilter "FAVORITE", "NOT_FAVORITE" oder null
     * @param pageable       Pageable ohne Sort (native Query sortiert selbst)
     */
    public Page<ArticleDto> searchFullTextWithFilters(String rawQuery,
                                                       String categoryId,
                                                       String readFilter,
                                                       String favoriteFilter,
                                                       Pageable pageable) {
        String tsQuery = normalizeToTsQuery(rawQuery);
        logger.info("FTS-Suche mit Filtern: tsQuery={}, categoryId={}, readFilter={}, favoriteFilter={}",
                tsQuery, categoryId, readFilter, favoriteFilter);
        return articleJpaRepository.searchByTextVectorWithFilters(
                        tsQuery, categoryId, CURRENT_USER, readFilter, favoriteFilter, pageable)
                .map(articlePersistenceMapper::toDomain)
                .map(articleMapper::toDto);
    }

    /**
     * Normalisiert einen Nutzer-String zu einer PostgreSQL tsquery.
     * "Bundestagswahl 2024" -> "Bundestagswahl & 2024"
     */
    public String normalizeToTsQuery(String raw) {
        if (raw == null || raw.isBlank()) { return ""; }
        String cleaned = NON_WORD.matcher(raw.trim()).replaceAll(" ");
        String[] tokens = cleaned.split("\\s+");
        return String.join(" & ", tokens);
    }

    // ==================== Use Case: GetArticlesByFeed ====================
    @Override
    public List<Article> getArticlesByFeed(Long feedId) {
        logger.debug("Rufe Artikel fuer Feed ab: {}", feedId);
        return articleRepository.findByFeedId(feedId);
    }

    public List<ArticleDto> getArticlesByFeedDto(Long feedId) {
        return getArticlesByFeed(feedId).stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }

    // ==================== Zusaetzliche Methoden ====================
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