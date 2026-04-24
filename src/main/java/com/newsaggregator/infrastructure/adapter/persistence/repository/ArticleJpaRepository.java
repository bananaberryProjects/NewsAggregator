package com.newsaggregator.infrastructure.adapter.persistence.repository;

import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository fuer ArticleJpaEntity.
 */
@Repository
public interface ArticleJpaRepository extends JpaRepository<ArticleJpaEntity, Long> {

    boolean existsByLink(String link);

    /**
     * Full-Text Search via PostgreSQL tsvector (Performance: GIN-Index).
     *
     * @param tsQuery normalisierte tsquery (z.B. &quot;Bundestag&quot;
     */
    @Query(value = """
        SELECT a.* FROM articles a
        WHERE a.search_vector @@ to_tsquery(&#39;german&#39;, :tsQuery)
        ORDER BY ts_rank(a.search_vector, to_tsquery(&#39;german&#39;, :tsQuery)) DESC
        """, nativeQuery = true)
    Page<ArticleJpaEntity> searchByTextVector(@Param("tsQuery") String tsQuery, Pageable pageable);

    /**
     * Headline-Preview: Artikel mit ts_headline-Snippet zurueckgeben.
     * (fuer spaetere Highlighting-Phase)
     */
    @Query(value = """
        SELECT a.id, a.title, a.description, a.link, a.image_url, a.published_at, a.created_at,
               a.feed_id, a.content_html, a.content_extraction_failed,
               ts_headline(&#39;german&#39;, COALESCE(a.content_html, a.description, a.title),
                         to_tsquery(&#39;german&#39;, :tsQuery)) AS search_snippet,
               ts_rank(a.search_vector, to_tsquery(&#39;german&#39;, :tsQuery)) AS search_rank
        FROM articles a
        WHERE a.search_vector @@ to_tsquery(&#39;german&#39;, :tsQuery)
        ORDER BY search_rank DESC
        """, nativeQuery = true)
    List<Object[] []]> searchWithSnippet(@Param("tsQuery") String tsQuery, Pageable pageable);

    @Query("
        SELECT a FROM ArticleJpaEntity a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT(&#39;%&#39;, :query, &#39;%&#39;)) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT(&#39;%&#39;, :query, &#39;%&#39;))")
    List<ArticleJpaEntity> searchByQuery(@Param("query") String query);

    List<ArticleJpaEntity> findByFeedId(Long feedId);
    long countByFeedId(Long feedId);
    List<ArticleJpaEntity> findByPublishedAtAfter(LocalDateTime date);

    @Query("
        SELECT DISTINCT a FROM ArticleJpaEntity a LEFT JOIN FETCH a.feed f LEFT JOIN FETCH f.categories")
    List<ArticleJpaEntity> findAllWithFeedAndCategories();

    @Query("
        SELECT a FROM ArticleJpaEntity a JOIN FETCH a.feed f WHERE a.contentHtml IS NULL AND (a.contentExtractionFailed IS NULL OR a.contentExtractionFailed = false) AND f.extractContent = true ORDER BY a.createdAt DESC")
    List<ArticleJpaEntity> findByContentHtmlIsNull(Pageable pageable);

    @Query("SELECT COUNT(a) FROM ArticleJpaEntity a JOIN a.feed f WHERE a.contentHtml IS NULL AND (a.contentExtractionFailed IS NULL OR a.contentExtractionFailed = false) AND f.extractContent = true")
    long countByContentHtmlIsNull();

    @Modifying
    @Query("DELETE FROM ArticleJpaEntity a WHERE a.publishedAt < :cutoffDate")
    int deleteByPublishedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

}
