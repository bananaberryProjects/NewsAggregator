package com.newsaggregator.infrastructure.adapter.content;

import com.newsaggregator.domain.port.out.ArticleContentExtractor;
import net.dankito.readability4j.Readability4J;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Adapter für die Content-Extraktion mit Readability4J.
 *
 * <p>Implementiert den Domain-Port {@link ArticleContentExtractor} mit
 * der Readability4J-Bibliothek. Lädt HTML von der angegebenen URL und
 * extrahiert den Hauptartikel mit dem Readability-Algorithmus.</p>
 *
 * <p>Readability4J ist eine Java-Portierung von Mozilla Readability,
 * die in Firefox für den Reader-Mode verwendet wird.</p>
 *
 * <p>Verwendet JSoup für das Laden, da JSoup automatisch mit
 * GZip- und Deflate-Kompression umgeht.</p>
 */
@Component
public class ReadabilityContentExtractorAdapter implements ArticleContentExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ReadabilityContentExtractorAdapter.class);

    private static final int TIMEOUT_MS = 30_000; // 30 Sekunden
    private static final int MAX_CONTENT_LENGTH = 5 * 1024 * 1024; // 5MB max

    @Override
    public String extractContent(String url) throws ContentExtractionException {
        ExtractionResult result = extractContentWithMetadata(url);
        if (result.successful()) {
            return result.content();
        }
        return null;
    }

    @Override
    public ExtractionResult extractContentWithMetadata(String url) throws ContentExtractionException {
        if (!canExtract(url)) {
            logger.warn("URL kann nicht für Content-Extraktion verwendet werden: {}", url);
            return ExtractionResult.failure("URL schema not supported");
        }

        logger.debug("Extrahiere Content von: {}", url);

        try {
            // HTML von der URL laden mit JSoup (unterstützt automatisch GZip/Deflate)
            String html = fetchHtml(url);

            if (html == null || html.isEmpty()) {
                logger.warn("Leere Antwort von URL: {}", url);
                return ExtractionResult.failure("Empty response");
            }

            // Readability-Extraktion durchführen
            Readability4J readability4J = new Readability4J(url, html);
            var extractedArticle = readability4J.parse();

            if (extractedArticle == null) {
                logger.warn("Artikel konnte nicht extrahiert werden von: {}", url);
                return ExtractionResult.failure("Content not readable");
            }

            // Extrahierten Content bereinigen und aufbereiten
            String content = extractedArticle.getArticleContent().html();
            if (content != null && !content.isEmpty()) {
                content = sanitizeContent(content);
            }

            String title = extractedArticle.getTitle();
            String excerpt = extractedArticle.getExcerpt();

            logger.info("Content erfolgreich extrahiert von {}: {} Zeichen, Titel: {}",
                    url, content != null ? content.length() : 0, title);

            return ExtractionResult.success(content, title, excerpt);

        } catch (SocketTimeoutException e) {
            logger.error("Timeout beim Laden von {}: {}", url, e.getMessage());
            throw new ContentExtractionException(url, "Request timeout: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("IO-Fehler beim Laden von {}: {}", url, e.getMessage());
            throw new ContentExtractionException(url, "Failed to fetch content: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Fehler bei Content-Extraktion von {}: {}", url, e.getMessage(), e);
            throw new ContentExtractionException(url, "Extraction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean canExtract(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String lowerUrl = url.toLowerCase();
        return lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://");
    }

    /**
     * Lädt das HTML von der angegebenen URL.
     *
     * <p>Verwendet JSoup, das automatisch mit GZip und Deflate umgeht
     * und keine manuelle Dekomprimierung erfordert.</p>
     *
     * <p>Entfernt vorab häufige Cookie-Banner-Elemente, um die
     * Readability-Extraktion zu verbessern.</p>
     *
     * @param urlString Die URL
     * @return Der HTML-Content als String
     * @throws IOException bei Netzwerkfehlern
     */
    private String fetchHtml(String urlString) throws IOException {
        URL url = new URL(urlString);
        
        Document doc = Jsoup.connect(url.toString())
                .timeout(TIMEOUT_MS)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "de,en-US;q=0.7,en;q=0.3")
                .followRedirects(true)
                .maxBodySize(MAX_CONTENT_LENGTH)
                .get();

        // Cookie-Banner und Consent-Dialoge vorab entfernen
        removeCookieBanners(doc);

        return doc.html();
    }

    /**
     * Entfernt häufige Cookie-Banner, Consent-Dialoge und andere
     * störende Elemente aus dem HTML vor der Readability-Extraktion.
     *
     * @param doc Das JSoup Document
     */
    private void removeCookieBanners(Document doc) {
        // Typische Cookie-Banner IDs und Klassen
        String[] cookieSelectors = {
            // IDs
            "#cookie-banner", "#cookie-consent", "#cookie-notice", "#cookie-popup",
            "#cookie-overlay", "#cookie-container", "#cookie-dialog",
            "#gdpr-banner", "#gdpr-popup", "#gdpr-notice", "#gdpr-consent",
            "#cmpbox", "#cmpbox-frame", "#usercentrics-root", "#uc-banner",
            "#cc-banner", "#cc-window", "#cc-container",
            "#consent-banner", "#consent-popup", "#consent-notice",
            "#onetrust-consent-sdk", "#onetrust-banner-sdk",
            "#CybotCookiebotDialog", "#CybotCookiebotDialogBodyUnderlay",
            "#cookie-law-bar", "#eu-cookie-law",
            
            // Klassen
            ".cookie-banner", ".cookie-consent", ".cookie-notice", ".cookie-popup",
            ".cookie-overlay", ".cookie-container", ".cookie-dialog", ".cookie-bar",
            ".gdpr-banner", ".gdpr-popup", ".gdpr-notice", ".gdpr-consent",
            ".cmpbox", ".cmpbox-frame", ".usercentrics-root",
            ".cc-banner", ".cc-window", ".cc-container",
            ".consent-banner", ".consent-popup", ".consent-notice",
            ".onetrust-consent-sdk", ".onetrust-banner-sdk",
            ".cookie-law-bar", ".eu-cookie-law",
            ".cmp-banner", ".cmp-popup", ".cmp-container",
            
            // Data-Attribute (häufige Consent-Manager)
            "[data-cmp-id]", "[data-testid*='cookie']", "[data-testid*='consent']",
            "[data-qa*='cookie']", "[data-qa*='consent']",
            "[data-tracking*='cookie']", "[data-tracking*='consent']",
            "[aria-label*='cookie' i]", "[aria-label*='consent' i]",
            "[role='dialog'][aria-label*='cookie' i]"
        };

        int removedCount = 0;
        for (String selector : cookieSelectors) {
            try {
                var elements = doc.select(selector);
                if (!elements.isEmpty()) {
                    removedCount += elements.size();
                    elements.remove();
                }
            } catch (Exception e) {
                // Ignoriere ungültige Selectoren
                logger.trace("Ungültiger Selektor '{}': {}", selector, e.getMessage());
            }
        }

        if (removedCount > 0) {
            logger.debug("{} Cookie-Banner/Consent-Elemente entfernt", removedCount);
        }
    }

    /**
     * Bereinigt den extrahierten Content von potenziell gefährlichen Elementen.
     *
     * <p>Entfernt Script-Tags, on*-Event-Handler und andere potenziell
     * unsichere Elemente aus dem HTML.</p>
     *
     * @param content Der rohe HTML-Content
     * @return Der bereinigte HTML-Content
     */
    private String sanitizeContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        try {
            org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(content);

            // Script- und Style-Tags entfernen
            doc.select("script, style, iframe, object, embed").remove();

            // on*-Event-Handler entfernen
            doc.getAllElements().forEach(element -> {
                // Attribute sammeln die entfernt werden sollen
                var attrsToRemove = element.attributes().asList().stream()
                        .filter(attr -> attr.getKey().toLowerCase().startsWith("on"))
                        .map(attr -> attr.getKey())
                        .toList();
                // Attribute einzeln entfernen
                attrsToRemove.forEach(element::removeAttr);
            });

            // Externe Links mit rel="noopener noreferrer" versehen
            doc.select("a[href]").forEach(link -> {
                if (link.attr("href").startsWith("http")) {
                    link.attr("rel", "noopener noreferrer nofollow");
                    link.attr("target", "_blank");
                }
            });

            // Bilder mit Lazy-Loading optimieren
            doc.select("img").forEach(img -> {
                img.attr("loading", "lazy");
                if (!img.hasAttr("alt")) {
                    img.attr("alt", "");
                }
            });

            return doc.body() != null ? doc.body().html() : content;

        } catch (Exception e) {
            logger.warn("Fehler bei Content-Sanitisierung: {} - {}", content, e.getMessage());
            return content;
        }
    }
}
