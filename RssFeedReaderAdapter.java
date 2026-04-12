package com.newsaggregator.infrastructure.adapter.rss;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.RssFeedReader;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import java.net.URI;
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
            URL feedUrl = new URI(feed.getUrl()).toURL();
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed syndFeed = input.build(new InputSource(feedUrl.openStream()));
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
     * Versucht verschiedene Quellen: Media RSS (media:content), Enclosures, img-Tags im Content.
     */
    private String extractImageUrl(SyndEntry entry, String description) {
        // 1. Versuche Media RSS (media:content) - z.B. bei WELT
        String mediaUrl = extractMediaContentUrl(entry);
        if (mediaUrl != null) {
            return mediaUrl;
        }

        // 2. Versuche Enclosures (häufig bei Podcasts/Bilder)
        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
            for (SyndEnclosure enclosure : entry.getEnclosures()) {
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
     * Extrahiert Bild-URL aus Media RSS (MRSS) media:content/media:thumbnail Tags.
     * Wird z.B. von WELT, Bild.de, FAZ verwendet.
     * Nutzt ForeignMarkup für direkten Zugriff auf XML-Extensions.
     */
    private String extractMediaContentUrl(SyndEntry entry) {
        List<Element> foreignMarkup = entry.getForeignMarkup();
        if (foreignMarkup == null || foreignMarkup.isEmpty()) {
            return null;
        }

        for (Element element : foreignMarkup) {
            // Prüfe Namespace (Yahoo Media RSS oder ähnlich)
            String nsUri = element.getNamespace() != null ? element.getNamespace().getURI() : "";
            boolean isMediaNs = nsUri.contains("yahoo.com/mrss") || nsUri.contains("search.yahoo");
            
            if (!isMediaNs) {
                continue;
            }
            
            // Suche nach media:content
            if ("content".equals(element.getName())) {
                String url = element.getAttributeValue("url");
                String type = element.getAttributeValue("type");
                // Nur Bilder (image/*) oder URLs mit Bild-Extension
                if (url != null && !url.isEmpty()) {
                    if (type == null || type.startsWith("image/") || 
                        url.matches(".*\.(jpg|jpeg|png|gif|webp|bmp)(\?.*)?$")) {
                        logger.debug("Media RSS content Bild gefunden: {}", url);
                        return url;
                    }
                }
            }
            
            // Fallback: media:thumbnail
            if ("thumbnail".equals(element.getName())) {
                String url = element.getAttributeValue("url");
                if (url != null && !url.isEmpty()) {
                    logger.debug("Media RSS thumbnail gefunden: {}", url);
                    return url;
                }
            }
        }
        
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
                .toLocalDateTime();
    }
}
