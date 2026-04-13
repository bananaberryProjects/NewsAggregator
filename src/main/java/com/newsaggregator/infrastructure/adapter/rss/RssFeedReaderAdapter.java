package com.newsaggregator.infrastructure.adapter.rss;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.RssFeedReader;
import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.Thumbnail;
import com.rometools.modules.mediarss.types.UrlReference;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
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
        // 1. Versuche Media RSS Module (rome-modules dependency muss vorhanden sein)
        String mediaUrl = extractMediaRssUrl(entry);
        if (mediaUrl != null) {
            logger.debug("Bild aus MediaRSS: {}", mediaUrl);
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
            if (url != null && isImageUrl(url)) {
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
     * Extrahiert Bild-URL aus Media RSS (MRSS) über die Module API.
     * Funktioniert für WELT, Bild.de, FAZ und andere MRSS-Feeds.
     */
    private String extractMediaRssUrl(SyndEntry entry) {
        try {
            // Alle Module durchsuchen
            List<Module> modules = entry.getModules();
            if (modules == null || modules.isEmpty()) {
                logger.trace("Keine Module für Entry gefunden");
                return null;
            }
            
            for (Module module : modules) {
                // Prüfe auf MediaEntryModule
                if (module instanceof MediaEntryModule mediaModule) {
                    logger.debug("MediaEntryModule gefunden");
                    
                    // 1. Versuche Thumbnails zuerst (oft bessere Qualität)
                    Thumbnail[] thumbnails = mediaModule.getMetadata().getThumbnail();
                    if (thumbnails != null && thumbnails.length > 0 && thumbnails[0] != null) {
                        String url = thumbnails[0].getUrl().toString();
                        logger.debug("Thumbnail gefunden: {}", url);
                        return url;
                    }

                    // 2. Versuche MediaContents
                    if (mediaModule.getMediaContents() != null && mediaModule.getMediaContents().length > 0) {
                        for (int i = 0; i < mediaModule.getMediaContents().length; i++) {
                            var content = mediaModule.getMediaContents()[i];
                            if (content != null && content.getReference() instanceof UrlReference ref) {
                                String url = ref.getUrl().toString();
                                if (isImageUrl(url)) {
                                    logger.debug("MediaContent gefunden: {}", url);
                                    return url;
                                }
                            }
                        }
                    }
                    
                    // 3. Fallback: Erste verfügbare URL
                    if (mediaModule.getMetadata() != null && 
                        mediaModule.getMetadata().getThumbnail() != null &&
                        mediaModule.getMetadata().getThumbnail().length > 0) {
                        Thumbnail thumb = mediaModule.getMetadata().getThumbnail()[0];
                        if (thumb != null && thumb.getUrl() != null) {
                            return thumb.getUrl().toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Fehler beim Extrahieren von MediaRSS: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Prüft ob eine URL ein Bild ist.
     */
    private boolean isImageUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.contains(".jpg") || lower.contains(".jpeg") || 
               lower.contains(".png") || lower.contains(".gif") || 
               lower.contains(".webp") || lower.contains("/image") ||
               lower.contains("images.");
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
