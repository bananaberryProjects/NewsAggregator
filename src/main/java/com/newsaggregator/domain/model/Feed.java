package com.newsaggregator.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Domain Entity für Feed.
 * <p>
 * Diese Klasse repräsentiert einen RSS/Atom Feed in der Domain.
 * Sie enthält ausschließlich Business-Logik und Domain-Regeln.
 * </p>
 * <p>
 * Unveränderliche Felder (immutable): id, name, url, description, createdAt
 * Veränderliche Felder: lastFetched, status, articles, extractContent
 * </p>
 */
public class Feed {

    private final FeedId id;
    private String name;
    private String url;
    private String description;
    private final LocalDateTime createdAt;

    private LocalDateTime lastFetched;
    private FeedStatus status;
    private boolean extractContent;
    private List<String> blockedKeywords;
    private final Set<Article> articles;
    private final List<CategoryId> categoryIds;

    /**
     * Factory-Methode für neue Feeds (ohne ID - wird von Repository vergeben).
     */
    public static Feed createNew(String name, String url, String description) {
        return new Feed(null, name, url, description, LocalDateTime.now(), null, FeedStatus.ACTIVE, true, null);
    }

    /**
     * Factory-Methode für neue Feeds mit extractContent Option.
     */
    public static Feed createNew(String name, String url, String description, boolean extractContent) {
        return new Feed(null, name, url, description, LocalDateTime.now(), null, FeedStatus.ACTIVE, extractContent, null);
    }

    /**
     * Factory-Methode für vorhandene Feeds (mit ID - z.B. aus DB geladen).
     */
    public static Feed of(FeedId id, String name, String url, String description,
                          LocalDateTime createdAt, LocalDateTime lastFetched, FeedStatus status) {
        return new Feed(id, name, url, description, createdAt, lastFetched, status, true, null);
    }

    /**
     * Factory-Methode für vorhandene Feeds mit extractContent Option.
     */
    public static Feed of(FeedId id, String name, String url, String description,
                          LocalDateTime createdAt, LocalDateTime lastFetched, FeedStatus status, boolean extractContent) {
        return new Feed(id, name, url, description, createdAt, lastFetched, status, extractContent, null);
    }

    /**
     * Factory-Methode für vorhandene Feeds mit allen Optionen.
     */
    public static Feed of(FeedId id, String name, String url, String description,
                          LocalDateTime createdAt, LocalDateTime lastFetched, FeedStatus status,
                          boolean extractContent, List<String> blockedKeywords) {
        return new Feed(id, name, url, description, createdAt, lastFetched, status, extractContent, blockedKeywords);
    }

    private Feed(FeedId id, String name, String url, String description,
                 LocalDateTime createdAt, LocalDateTime lastFetched, FeedStatus status, boolean extractContent, List<String> blockedKeywords) {
        this.id = id;
        this.name = validateNotEmpty(name, "Name");
        this.url = validateUrl(url);
        this.description = description;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt darf nicht null sein");
        this.lastFetched = lastFetched;
        this.status = Objects.requireNonNull(status, "status darf nicht null sein");
        this.extractContent = extractContent;
        this.blockedKeywords = blockedKeywords != null ? new ArrayList<>(blockedKeywords) : new ArrayList<>();
        this.articles = new HashSet<>();
        this.categoryIds = new ArrayList<>();
    }

    // ==================== Validierung ====================

    private String validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein");
        }
        return value.trim();
    }

    private String validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL darf nicht leer sein");
        }
        String trimmedUrl = url.trim();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            throw new IllegalArgumentException("URL muss mit http:// oder https:// beginnen");
        }
        return trimmedUrl;
    }

    // ==================== Business-Methoden ====================

    /**
     * Markiert den Feed als erfolgreich abgerufen.
     * Setzt lastFetched auf jetzt und Status auf ACTIVE.
     */
    public void markAsFetched() {
        this.lastFetched = LocalDateTime.now();
        this.status = FeedStatus.ACTIVE;
    }

    /**
     * Markiert den Feed mit einem Fehler.
     */
    public void markAsError() {
        this.status = FeedStatus.ERROR;
    }

    /**
     * Deaktiviert den Feed.
     */
    public void disable() {
        this.status = FeedStatus.DISABLED;
    }

    /**
     * Aktiviert den Feed wieder.
     */
    public void enable() {
        if (this.status == FeedStatus.DISABLED) {
            this.status = FeedStatus.ACTIVE;
        }
    }

    /**
     * Prüft, ob der Feed abgerufen werden kann.
     * Ein Feed ist abrufbar, wenn er nicht deaktiviert ist.
     */
    public boolean canBeFetched() {
        return this.status != FeedStatus.DISABLED;
    }

    /**
     * Prüft, ob Content für diesen Feed extrahiert werden soll.
     */
    public boolean shouldExtractContent() {
        return this.extractContent;
    }

    /**
     * Setzt die Content-Extraktion Option.
     */
    public void setExtractContent(boolean extractContent) {
        this.extractContent = extractContent;
    }

    /**
     * Fügt einen Artikel zum Feed hinzu.
     */
    public void addArticle(Article article) {
        Objects.requireNonNull(article, "Article darf nicht null sein");
        this.articles.add(article);
    }

    /**
     * Prüft, ob ein Artikel mit diesem Link bereits existiert.
     */
    public boolean hasArticleWithLink(String link) {
        return articles.stream()
                .anyMatch(article -> article.getLink().equals(link));
    }

    // ==================== Getter ====================

    public FeedId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastFetched() {
        return lastFetched;
    }

    public FeedStatus getStatus() {
        return status;
    }

    public boolean isExtractContent() {
        return extractContent;
    }

    public List<String> getBlockedKeywords() {
        return Collections.unmodifiableList(blockedKeywords);
    }

    public void setBlockedKeywords(List<String> keywords) {
        this.blockedKeywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
    }

    public void addBlockedKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty() && !this.blockedKeywords.contains(keyword.trim().toLowerCase())) {
            this.blockedKeywords.add(keyword.trim().toLowerCase());
        }
    }

    public void removeBlockedKeyword(String keyword) {
        if (keyword != null) {
            this.blockedKeywords.remove(keyword.trim().toLowerCase());
        }
    }

    /**
     * Prüft, ob der Artikel-Titel ein geblocktes Keyword enthält.
     * Case-insensitive, prüft Teilstring.
     */
    public boolean isTitleBlocked(String title) {
        if (title == null || this.blockedKeywords.isEmpty()) {
            return false;
        }
        String lowerTitle = title.toLowerCase();
        return this.blockedKeywords.stream().anyMatch(lowerTitle::contains);
    }

    public Set<Article> getArticles() {
        return Collections.unmodifiableSet(articles);
    }

    public List<CategoryId> getCategoryIds() {
        return Collections.unmodifiableList(categoryIds);
    }

    public void addCategory(CategoryId categoryId) {
        Objects.requireNonNull(categoryId, "categoryId darf nicht null sein");
        if (!this.categoryIds.contains(categoryId)) {
            this.categoryIds.add(categoryId);
        }
    }

    public void removeCategory(CategoryId categoryId) {
        Objects.requireNonNull(categoryId, "categoryId darf nicht null sein");
        this.categoryIds.remove(categoryId);
    }

    public void setCategories(List<CategoryId> categoryIds) {
        this.categoryIds.clear();
        if (categoryIds != null) {
            this.categoryIds.addAll(categoryIds);
        }
    }

    /**
     * Aktualisiert die Feed-Daten.
     */
    public void update(String name, String url, String description) {
        this.name = validateNotEmpty(name, "Name");
        this.url = validateUrl(url);
        this.description = description;
    }

    /**
     * Aktualisiert die Feed-Daten inklusive extractContent und blockedKeywords.
     */
    public void update(String name, String url, String description, boolean extractContent, List<String> blockedKeywords) {
        this.name = validateNotEmpty(name, "Name");
        this.url = validateUrl(url);
        this.description = description;
        this.extractContent = extractContent;
        if (blockedKeywords != null) {
            this.blockedKeywords = new ArrayList<>(blockedKeywords);
        }
    }

    // ==================== Objekt-Methoden ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        // Zwei Feeds sind gleich, wenn sie die gleiche URL haben (Business-Regel)
        return Objects.equals(url, feed.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        return "Feed{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", status=" + status +
                ", extractContent=" + extractContent +
                '}';
    }
}
