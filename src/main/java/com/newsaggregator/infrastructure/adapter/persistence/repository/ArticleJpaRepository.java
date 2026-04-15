package com.newsaggregator.infrastructure.adapter.persistence.repository;

import com.newsaggregator.infrastructure.adapter.persistence.entity.ArticleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository für ArticleJpaEntity.
 */
@Repository
public interface ArticleJpaRepository extends JpaRepository<ArticleJpaEntity, Long> {

    /**
     * Prüft, ob ein Artikel mit diesem Link existiert.
     *
     * @param link Der zu prüfende Link
     * @return true, falls existiert
     */
    boolean existsByLink(String link);

    /**
     * Sucht Artikel anhand eines Suchbegriffs.
     *
     * @param query Der Suchbegriff
     * @return Liste der passenden Artikel
     */
    @Query("SELECT a FROM ArticleJpaEntity a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<ArticleJpaEntity> searchByQuery(@Param("query") String query);

    /**
     * Gibt alle Artikel eines bestimmten Feeds zurück.
     *
     * @param feedId Die ID des Feeds
     * @return Liste der Artikel
     */
    List<ArticleJpaEntity> findByFeedId(Long feedId);
    /**
     * Zählt die Artikel eines bestimmten Feeds.
     *
     * @param feedId Die ID des Feeds
     * @return Anzahl der Artikel
     */
    long countByFeedId(Long feedId);

    /**
     * Lädt alle Artikel mit Feed und Kategorien (Eager Loading).
     *
     * @return Liste aller Artikel mit Feed und Kategorien
     */
    @Query("SELECT DISTINCT a FROM ArticleJpaEntity a LEFT JOIN FETCH a.feed f LEFT JOIN FETCH f.categories")
    List<ArticleJpaEntity> findAllWithFeedAndCategories();
}
