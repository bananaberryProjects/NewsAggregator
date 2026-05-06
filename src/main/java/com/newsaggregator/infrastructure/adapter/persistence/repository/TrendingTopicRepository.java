package com.newsaggregator.infrastructure.adapter.persistence.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository für Trending-Topics Aggregation via JdbcTemplate.
 * Nutzt native SQL für Wortfrequenz-Statistik auf der articles-Tabelle.
 */
@Repository
public class TrendingTopicRepository {

    private final JdbcTemplate jdbcTemplate;

    public TrendingTopicRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Liefert Rohtext (Titel + Description) aller Artikel im Zeitraum.
     */
    public List<ArticleText> findArticleTextsSince(LocalDateTime since) {
        String sql = """
            SELECT title, description
            FROM articles
            WHERE created_at > ?
            ORDER BY created_at DESC
            """;
        return jdbcTemplate.query(sql, this::mapArticleText, since);
    }

    /**
     * Zählt Artikel pro Begriff + Feed-Anzahl im Zeitfenster.
     */
    public List<TermStats> findTopTermsInWindow(
            LocalDateTime windowStart, LocalDateTime windowEnd,
            int minCount, int limit) {
        String sql = """
            SELECT LOWER(REGEXP_REPLACE(title, '[^a-zA-ZäöüßÄÖÜ0-9\\s]', ' ', 'g')) AS term,
                   COUNT(*) AS article_count,
                   COUNT(DISTINCT feed_id) AS feed_count
            FROM articles
            WHERE created_at BETWEEN ? AND ?
            GROUP BY term
            HAVING COUNT(*) >= ?
            ORDER BY article_count DESC
            LIMIT ?
            """;
        return jdbcTemplate.query(sql, this::mapTermStats,
                windowStart, windowEnd, minCount, limit);
    }

    /**
     * Liefert Titel + Feed-ID für Breaking-News Detection.
     */
    public List<TitleFeed> findTitlesAndFeedIdsSince(LocalDateTime since) {
        String sql = """
            SELECT LOWER(REGEXP_REPLACE(title, '[^a-zA-ZäöüßÄÖÜ0-9\\s]', ' ', 'g')) AS raw_title,
                   feed_id
            FROM articles
            WHERE created_at > ?
            """;
        return jdbcTemplate.query(sql, this::mapTitleFeed, since);
    }

    private ArticleText mapArticleText(ResultSet rs, int rowNum) throws SQLException {
        return new ArticleText(rs.getString("title"), rs.getString("description"));
    }

    private TermStats mapTermStats(ResultSet rs, int rowNum) throws SQLException {
        return new TermStats(
                rs.getString("term"),
                rs.getInt("article_count"),
                rs.getInt("feed_count")
        );
    }

    private TitleFeed mapTitleFeed(ResultSet rs, int rowNum) throws SQLException {
        return new TitleFeed(
                rs.getString("raw_title"),
                rs.getLong("feed_id")
        );
    }

    // --- Record-like inner classes ---

    public static class ArticleText {
        public final String title;
        public final String description;
        public ArticleText(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    public static class TermStats {
        public final String term;
        public final int articleCount;
        public final int feedCount;
        public TermStats(String term, int articleCount, int feedCount) {
            this.term = term;
            this.articleCount = articleCount;
            this.feedCount = feedCount;
        }
    }

    public static class TitleFeed {
        public final String rawTitle;
        public final Long feedId;
        public TitleFeed(String rawTitle, Long feedId) {
            this.rawTitle = rawTitle;
            this.feedId = feedId;
        }
    }
}
