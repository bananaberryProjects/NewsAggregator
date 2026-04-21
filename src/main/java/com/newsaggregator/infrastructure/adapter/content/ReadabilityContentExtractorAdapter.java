package com.newsaggregator.infrastructure.adapter.content;

import com.newsaggregator.domain.port.out.ArticleContentExtractor;
import net.dankito.readability4j.Readability4J;
import net.dankito.readability4j.extended.Readability4JExtended;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Adapter für die Content-Extraktion mit Readability4J.
 *
 * <p>Implementiert den Domain-Port {@link ArticleContentExtractor} mit
 * der Readability4J-Bibliothek. Lädt HTML von der angegebenen URL und
 * extrahiert den Hauptartikel mit dem Readability-Algorithmus.</p>
 *
 * <p>Readability4J ist eine Java-Portierung von Mozilla Readability,
 * die in Firefox für den Reader-Mode verwendet wird.</p>
 */
@Component
public class ReadabilityContentExtractorAdapter implements ArticleContentExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ReadabilityContentExtractorAdapter.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_REDIRECTS = 5;
    private static final int MAX_CONTENT_LENGTH = 5 * 1024 * 1024; // 5MB max

    private final HttpClient httpClient;

    public ReadabilityContentExtractorAdapter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

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
            // HTML von der URL laden
            String html = fetchHtml(url);

            if (html == null || html.isEmpty()) {
                logger.warn("Leere Antwort von URL: {}", url);
                return ExtractionResult.failure("Empty response");
            }

            // Readability-Extraktion durchführen
            Readability4J readability4J = new Readability4JExtended(url, html);
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

        } catch (IOException e) {
            logger.error("IO-Fehler beim Laden von {}: {}", url, e.getMessage());
            throw new ContentExtractionException(url, "Failed to fetch content: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Unterbrechung beim Laden von {}: {}", url, e.getMessage());
            throw new ContentExtractionException(url, "Request interrupted: " + e.getMessage(), e);
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
     * @param url Die URL
     * @return Der HTML-Content als String
     * @throws IOException bei Netzwerkfehlern
     * @throws InterruptedException bei Unterbrechung
     */
    private String fetchHtml(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "de,en-US;q=0.7,en;q=0.3")
                .header("Accept-Encoding", "gzip, deflate, br")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " for URL: " + url);
        }

        String body = response.body();

        // Größenlimit prüfen
        if (body.length() > MAX_CONTENT_LENGTH) {
            throw new IOException("Content too large: " + body.length() + " bytes");
        }

        return body;
    }

    /**
     * Bereinigt den extrahierten Content von potenziell gefährlichen Elementen.
     *
     * <p>Entfert Script-Tags, on*-Event-Handler und andere potenziell
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
                element.attributes().asList().removeIf(attr ->
                        attr.getKey().toLowerCase().startsWith("on")
                );
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
            logger.warn("Fehler bei Content-Sanitisierung: {}", e.getMessage());
            return content;
        }
    }
}
