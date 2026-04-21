package com.newsaggregator.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain Entity für Article.
 *
 * <p>
 * Diese Klasse repräsentiert einen einzelnen Artikel/News-Eintrag in der Domain.
 * Ein Artikel gehört immer zu einem Feed.
 * </p>
 * <p>
 * Unveränderliche Felder (immutable): id, title, description, link, publishedAt, feed, createdAt
 * </p>
 */
public class Article {

    private final ArticleId id;
    private final String title;
    private final String description;
    private final String link;
    private final String imageUrl;
    private final String extractedContent;
    private final LocalDateTime publishedAt;
    private final Feed feed;
    private final LocalDateTime createdAt;

    /**
     * Factory-Methode für neue Artikel (ohne ID - wird von Repository vergeben).
     */
    public static Article createNew(String title, String description, String link,
                                    LocalDateTime publishedAt, Feed feed) {
        return new Article(null, title, description, link, null, null, publishedAt, feed, LocalDateTime.now());
    }

    /**
     * Factory-Methode für neue Artikel mit Bild-URL.
     */
    public static Article createNew(String title, String description, String link, String imageUrl,
                                    LocalDateTime publishedAt, Feed feed) {
        return new Article(null, title, description, link, imageUrl, null, publishedAt, feed, LocalDateTime.now());
    }

    /**
     * Factory-Methode für neue Artikel mit Bild-URL und extrahiertem Content.
     */
    public static Article createNew(String title, String description, String link, String imageUrl,
                                    String extractedContent, LocalDateTime publishedAt, Feed feed) {
        return new Article(null, title, description, link, imageUrl, extractedContent, publishedAt, feed, LocalDateTime.now());
    }

    /**
     * Factory-Methode für vorhandene Artikel (mit ID - z.B. aus DB geladen).
     */
    public static Article of(ArticleId id, String title, String description, String link,
                             LocalDateTime publishedAt, Feed feed, LocalDateTime createdAt) {
        return new Article(id, title, description, link, null, null, publishedAt, feed, createdAt);
    }

    /**
     * Factory-Methode für vorhandene Artikel mit Bild-URL.
     */
    public static Article of(ArticleId id, String title, String description, String link, String imageUrl,
                             LocalDateTime publishedAt, Feed feed, LocalDateTime createdAt) {
        return new Article(id, title, description, link, imageUrl, null, publishedAt, feed, createdAt);
    }

    /**
     * Factory-Methode für vorhandene Artikel mit allen Feldern (inkl. extrahiertem Content).
     */
    public static Article of(ArticleId id, String title, String description, String link, String imageUrl,
                             String extractedContent, LocalDateTime publishedAt, Feed feed, LocalDateTime createdAt) {
        return new Article(id, title, description, link, imageUrl, extractedContent, publishedAt, feed, createdAt);
    }

    private Article(ArticleId id, String title, String description, String link, String imageUrl,
                    String extractedContent, LocalDateTime publishedAt, Feed feed, LocalDateTime createdAt) {
        this.id = id;
        this.title = validateNotEmpty(title, "Titel");
        this.description = description;
        this.link = validateNotEmpty(link, "Link");
        this.imageUrl = imageUrl;
        this.extractedContent = extractedContent;
        this.publishedAt = publishedAt != null ? publishedAt : LocalDateTime.now();
        this.feed = Objects.requireNonNull(feed, "Feed darf nicht null sein");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt darf nicht null sein");
    }

    // ==================== Validierung ====================

    private String validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein");
        }
        return value.trim();
    }

    // ==================== Business-Methoden ====================

    /**
     * Erzeugt eine Kopie dieses Artikels mit extrahiertem Content.
     * Nützlich für Content-Extraktion nach der Erstellung.
     *
     * @param content Der extrahierte HTML-Content
     * @return Ein neuer Article mit gesetztem extractedContent
     */
    public Article withExtractedContent(String content) {
        return new Article(
                this.id,
                this.title,
                this.description,
                this.link,
                this.imageUrl,
                content,
                this.publishedAt,
                this.feed,
                this.createdAt
        );
    }

    /**
     * Gibt eine Zusammenfassung des Artikels zurück (z.B. für Vorschau).
     * Kürzt die Beschreibung auf max. 200 Zeichen.
     */
    public String getSummary() {
        if (description == null || description.isEmpty()) {
            return title;
        }
        if (description.length() <= 200) {
            return description;
        }
        return description.substring(0, 200) + "...";
    }

    /**
     * Prüft, ob der Artikel kürzer als ein bestimmtes Datum veröffentlicht wurde.
     */
    public boolean isPublishedAfter(LocalDateTime dateTime) {
        return publishedAt.isAfter(dateTime);
    }

    /**
     * Prüft, ob der Artikel heute veröffentlicht wurde.
     */
    public boolean isPublishedToday() {
        return publishedAt.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    // ==================== Getter ====================

    public ArticleId getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getExtractedContent() {
        return extractedContent;
    }

    public boolean hasExtractedContent() {
        return extractedContent != null && !extractedContent.isEmpty();
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public Feed getFeed() {
        return feed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ==================== Objekt-Methoden ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        // Zwei Artikel sind gleich, wenn sie den gleichen Link haben (Business-Regel)
        return Objects.equals(link, article.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", hasContent=" + hasExtractedContent() +
                ", publishedAt=" + publishedAt +
                '}';
    }
}
