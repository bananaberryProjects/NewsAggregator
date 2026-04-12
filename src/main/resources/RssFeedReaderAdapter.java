package com.newsaggregator.infrastructure.adapter.rss;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.RssFeedReader;
import com.rometools.modules.mediarss.MediaModule;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter für das Lesen von RSS-Feeds.
 *
 * <p>Implementiert den Domain-Port {@link RssFeedReader} mit
 * der Rome-Bibliothek zur RSS/Atom-Verarbeitung.</p>
 */
@Component
public class RssFeedReaderAdapter implements RssFeedReader {

    private static final Logger logger = LoggerFactory.getLogger(RssFeedReaderAdapter.class);
    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]+src=['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);

    @Override
    public List<Article> readFeed(Feed feed) throws RssReadException {
        if (feed == null || feed.getUrl() == null) {
            throw new RssReadException("Feed oder URL darf nicht null sein");
        }

        logger.info("Lese Feed: {}", feed.getName());

        try {
            URL feedUrl = new URL(feed.getUrl());
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed syndFeed = input.build(new XmlReader(feedUrl));

            List<Article> articles = new ArrayList<>();

            for (SyndEntry entry : syndFeed.getEntries()) {
                Article article = mapEntryToArticle(entry, feed);
                if (article != null) {
                    articles.add(article);
                }
            }

            logger.info("{} Artikel aus Feed '{}' gelesen", articles.size(), feed.getName());
            return articles;

        } catch (Exception e) {
            logger.error("Fehler beim Lesen des Feeds '{}': {}", feed.getName(), e.getMessage());
            throw new RssReadException(
                    "Feed konnte nicht gelesen werden: " + feed.getUrl(), e);
        }
    }

    /**
     * Wandelt einen SyndEntry in ein Domain-Article um.
     */
    private Article mapEntryToArticle(SyndEntry entry, Feed feed) {
        if (entry.getTitle() == null || entry.getLink() == null) {
            logger.warn("Artikel ohne Titel oder Link übersprungen");
            return null;
        }

        String title = entry.getTitle();
        String link = entry.getLink();

        // Beschreibung extrahieren (kann HTML enthalten)
        String description = "";
        if (entry.getDescription() != null) {
            description = entry.getDescription().getValue();
        }

        // Bild-URL extrahieren
        String imageUrl = extractImageUrl(entry, description);

        // Veröffentlichungsdatum extrahieren
        LocalDateTime publishedAt = convertToLocalDateTime(entry.getPublishedDate());

        return Article.createNew(title, description, link, imageUrl, publishedAt, feed);
    }

    /**
     * Extrahiert die Bild-URL aus dem Entry.
     * Versucht verschiedene Quellen: Enclosures, Media RSS, img-Tags im Content.
     */
    private String extractImageUrl(SyndEntry entry, String description) {
        // 1. Versuche Enclosures (häufig bei Podcasts/Bilder)
        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
            for (var enclosure : entry.getEnclosures()) {
                String type = enclosure.getType();
                if (type != null && (type.startsWith("image/") || type.contains("image"))) {
                    return enclosure.getUrl();
                }
            }
            // Falls kein Bild-Typ, nimm das erste Enclosure (oft Bild)
            String url = entry.getEnclosures().get(0).getUrl();
            if (url != null && (url.endsWith(".jpg") || url.endsWith(".jpeg") || 
                url.endsWith(".png") || url.endsWith(".gif") || url.endsWith(".webp"))) {
                return url;
            }
        }

        // 2. Versuche Media RSS Module
        MediaModule mediaModule = (MediaModule) entry.getModule(MediaModule.URI);
        if (mediaModule != null && mediaModule.getMediaContents() != null && 
            mediaModule.getMediaContents().length > 0) {
            for (var content : mediaModule.getMediaContents()) {
                if (content.getThumbnails() != null && content.getThumbnails().length > 0) {
                    return content.getThumbnails()[0].getUrl().toString();
                }
            }
        }

        // 3. Extrahiere erstes img-Tag aus der Beschreibung
        if (description != null && !description.isEmpty()) {
            Matcher matcher = IMG_PATTERN.matcher(description);
            if (matcher.find()) {
                String imgUrl = matcher.group(1);
                if (imgUrl.startsWith("http")) {
                    return imgUrl;
                }
            }
        }

        // Kein Bild gefunden
        return null;
    }

    /**
     * Konvertiert ein java.util.Date in LocalDateTime.
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return LocalDateTime.now();
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
    }
}
