package com.newsaggregator.infrastructure.adapter.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleJpaEntity;

/**
 * Spring Data JPA Repository fuer ArticleJpaEntity.
 */
@Repository
public interface ArticleJpaRepository extends JpaRepository<ArticleJpaEntity, Long> {

    boolean existsByLink(String link);

    /**
     * Full-Text Search via PostgreSQL tsvector (Performance: GIN-Index).
     *
     * @param tsQuery normalisierte tsquery (z.B. "Bundestag")
     */
    @Query(value = """
        SELECT a.* FROM articles a
        WHERE a.search_vector @@ to_tsquery('german', :tsQuery)
        ORDER BY ts_rank(a.search_vector, to_tsquery('german', :tsQuery)) DESC
        """, nativeQuery = true)
    Page<ArticleJpaEntity> searchByTextVector(@Param("tsQuery") String tsQuery, Pageable pageable);

    /**
     * Full-Text Search mit optionalem Feed-Category- und Read-Status-Filter.
     */
    @Query(value = """
        SELECT DISTINCT a.* FROM articles a
        JOIN feeds f ON a.feed_id = f.id
        LEFT JOIN feed_categories fc ON f.id = fc.feed_id
        LEFT JOIN article_read_status ars ON ars.article_id = a.id AND ars.user_id = :userId
        WHERE a.search_vector @@ to_tsquery('german', :tsQuery)
          AND (:categoryId IS NULL OR CAST(fc.category_id AS VARCHAR) = :categoryId)
          AND (:readFilter IS NULL OR
               (:readFilter = 'READ'    AND ars.is_read = true) OR
               (:readFilter = 'UNREAD'  AND (ars.is_read IS NULL OR ars.is_read = false)))
          AND (:favoriteFilter IS NULL OR
               (:favoriteFilter = 'FAVORITE'     AND ars.is_favorite = true) OR
               (:favoriteFilter = 'NOT_FAVORITE' AND (ars.is_favorite IS NULL OR ars.is_favorite = false)))
        ORDER BY ts_rank(a.search_vector, to_tsquery('german', :tsQuery)) DESC
        """,
        countQuery = """
        SELECT COUNT(DISTINCT a.id) FROM articles a
        JOIN feeds f ON a.feed_id = f.id
        LEFT JOIN feed_categories fc ON f.id = fc.feed_id
        LEFT JOIN article_read_status ars ON ars.article_id = a.id AND ars.user_id = :userId
        WHERE a.search_vector @@ to_tsquery('german', :tsQuery)
          AND (:categoryId IS NULL OR CAST(fc.category_id AS VARCHAR) = :categoryId)
          AND (:readFilter IS NULL OR
               (:readFilter = 'READ'    AND ars.is_read = true) OR
               (:readFilter = 'UNREAD'  AND (ars.is_read IS NULL OR ars.is_read = false)))
          AND (:favoriteFilter IS NULL OR
               (:favoriteFilter = 'FAVORITE'     AND ars.is_favorite = true) OR
               (:favoriteFilter = 'NOT_FAVORITE' AND (ars.is_favorite IS NULL OR ars.is_favorite = false)))
        """,
        nativeQuery = true)
    Page<ArticleJpaEntity> searchByTextVectorWithFilters(
            @Param("tsQuery") String tsQuery,
            @Param("categoryId") String categoryId,
            @Param("userId") String userId,
            @Param("readFilter") String readFilter,
            @Param("favoriteFilter") String favoriteFilter,
            Pageable pageable);

    /**
     * Headline-Preview: Artikel mit ts_headline-Snippet zurueckgeben.
     * (fuer spaetere Highlighting-Phase)
     */
    @Query(value = """
        SELECT a.id, a.title, a.description, a.link, a.image_url, a.published_at, a.created_at,
               a.feed_id, a.content_html, a.content_extraction_failed,
               ts_headline('german', COALESCE(a.content_html, a.description, a.title),
                         to_tsquery('german', :tsQuery)) AS search_snippet,
               ts_rank(a.search_vector, to_tsquery('german', :tsQuery)) AS search_rank
        FROM articles a
        WHERE a.search_vector @@ to_tsquery('german', :tsQuery)
        ORDER BY search_rank DESC
        """, nativeQuery = true)
    List<Object[]> searchWithSnippet(@Param("tsQuery") String tsQuery, Pageable pageable);

    @Query("SELECT a FROM ArticleJpaEntity a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<ArticleJpaEntity> searchByQuery(@Param("query") String query);

    List<ArticleJpaEntity> findByFeedId(Long feedId);
    long countByFeedId(Long feedId);
    List<ArticleJpaEntity> findByPublishedAtAfter(LocalDateTime date);

    @Query("SELECT DISTINCT a FROM ArticleJpaEntity a LEFT JOIN FETCH a.feed f LEFT JOIN FETCH f.categories")
    List<ArticleJpaEntity> findAllWithFeedAndCategories();

    @Query("SELECT a FROM ArticleJpaEntity a JOIN FETCH a.feed f WHERE a.contentHtml IS NULL AND (a.contentExtractionFailed IS NULL OR a.contentExtractionFailed = false) AND f.extractContent = true ORDER BY a.createdAt DESC")
    List<ArticleJpaEntity> findByContentHtmlIsNull(Pageable pageable);

    @Query("SELECT COUNT(a) FROM ArticleJpaEntity a JOIN a.feed f WHERE a.contentHtml IS NULL AND (a.contentExtractionFailed IS NULL OR a.contentExtractionFailed = false) AND f.extractContent = true")
    long countByContentHtmlIsNull();

    @Modifying
    @Query("DELETE FROM ArticleJpaEntity a WHERE a.publishedAt < :cutoffDate")
    int deleteByPublishedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

}