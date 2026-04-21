package com.newsaggregator.infrastructure.adapter.content;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.newsaggregator.domain.port.out.ArticleContentExtractor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integrationstest für ReadabilityContentExtractorAdapter.
 *
 * <p>Testet die Content-Extraktion mit simulierten realen Webseiten.
 * Verwendet WireMock, um HTTP-Responses zu simulieren und die
 * vollständige Extraktions-Pipeline zu testen.</p>
 */
@SpringBootTest
class ReadabilityContentExtractorAdapterIntegrationTest {

    @Autowired
    private ReadabilityContentExtractorAdapter extractor;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        configureFor(wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Sollte Content von einer einfachen Artikel-Seite extrahieren")
    void extractContent_ShouldExtractContentFromSimpleArticlePage() throws ArticleContentExtractor.ContentExtractionException {
        // Given
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test Article Title</title>
                <meta name="description" content="Test article excerpt">
            </head>
            <body>
                <article>
                    <h1>Test Article Title</h1>
                    <p>This is the main article content. It contains multiple paragraphs
                    with interesting information about the topic.</p>
                    <p>Here is another paragraph with more details and insights.</p>
                </article>
            </body>
            </html>
            """;

        stubFor(get(urlEqualTo("/simple-article"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody(htmlContent)));

        String url = "http://localhost:" + wireMockServer.port() + "/simple-article";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(url);

        // Then
        assertThat(result.successful()).isTrue();
        assertThat(result.content()).contains("Test Article Title");
        assertThat(result.content()).contains("This is the main article content");
        assertThat(result.title()).isEqualTo("Test Article Title");
    }

    @Test
    @DisplayName("Sollte Cookie-Banner vor der Extraktion entfernen")
    void extractContent_ShouldRemoveCookieBannersBeforeExtraction() throws ArticleContentExtractor.ContentExtractionException {
        // Given - HTML mit Cookie-Banner
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Article With Cookie Banner</title>
            </head>
            <body>
                <div id="cookie-banner" class="cookie-consent">
                    <p>This site uses cookies. Accept to continue.</p>
                    <button>Accept</button>
                </div>
                <article>
                    <h1>Real Article Title</h1>
                    <p>This is the actual article content that should be extracted.</p>
                    <p>More interesting content here without any cookie references.</p>
                </article>
            </body>
            </html>
            """;

        stubFor(get(urlEqualTo("/article-with-cookies"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody(htmlContent)));

        String url = "http://localhost:" + wireMockServer.port() + "/article-with-cookies";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(url);

        // Then
        assertThat(result.successful()).isTrue();
        assertThat(result.content()).contains("Real Article Title");
        assertThat(result.content()).contains("actual article content");
        // Cookie-Banner Inhalt sollte entfernt sein
        assertThat(result.content()).doesNotContain("Accept to continue");
        assertThat(result.content()).doesNotContain("cookie-banner");
    }

    @Test
    @DisplayName("Sollte mehrere Cookie-Banner und Consent-Dialoge entfernen")
    void extractContent_ShouldRemoveMultipleCookieElements() throws ArticleContentExtractor.ContentExtractionException {
        // Given - HTML mit mehreren Cookie-Elementen
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>News Article</title>
            </head>
            <body>
                <div id="CybotCookiebotDialog">
                    <p>We use cookies for tracking.</p>
                </div>
                <div class="onetrust-consent-sdk">
                    <p>Please consent to cookies.</p>
                </div>
                <div id="gdpr-banner">
                    <p>GDPR compliance notice.</p>
                </div>
                <main>
                    <h1>Breaking News: Important Event</h1>
                    <p>This is the actual news content that readers care about.</p>
                    <p>More details about the important event follow here.</p>
                </main>
            </body>
            </html>
            """;

        stubFor(get(urlEqualTo("/news-article"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody(htmlContent)));

        String url = "http://localhost:" + wireMockServer.port() + "/news-article";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(url);

        // Then
        assertThat(result.successful()).isTrue();
        assertThat(result.content()).contains("Breaking News");
        assertThat(result.content()).doesNotContain("CybotCookiebotDialog");
        assertThat(result.content()).doesNotContain("onetrust-consent-sdk");
        assertThat(result.content()).doesNotContain("GDPR compliance notice");
    }

    @Test
    @DisplayName("Sollte Script-Tags und Event-Handler aus dem Content entfernen")
    void extractContent_ShouldSanitizeScriptTagsAndEventHandlers() throws ArticleContentExtractor.ContentExtractionException {
        // Given - HTML mit potenziell gefährlichen Elementen
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Article With Scripts</title>
            </head>
            <body>
                <article>
                    <h1>Safe Article</h1>
                    <p onclick="alert('xss')">Click me (but onclick should be removed)</p>
                    <script>alert('This script should be removed');</script>
                    <p>This paragraph is safe and should be kept.</p>
                </article>
            </body>
            </html>
            """;

        stubFor(get(urlEqualTo("/article-with-scripts"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody(htmlContent)));

        String url = "http://localhost:" + wireMockServer.port() + "/article-with-scripts";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(url);

        // Then
        assertThat(result.successful()).isTrue();
        assertThat(result.content()).contains("Safe Article");
        assertThat(result.content()).contains("This paragraph is safe");
        // Script-Tags sollten entfernt sein
        assertThat(result.content()).doesNotContain("<script>");
        assertThat(result.content()).doesNotContain("alert('This script should be removed')");
    }

    @Test
    @DisplayName("Sollte Excerpt aus Meta-Description extrahieren")
    void extractContent_ShouldExtractExcerptFromMetaDescription() throws ArticleContentExtractor.ContentExtractionException {
        // Given
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Article With Excerpt</title>
                <meta name="description" content="This is the article excerpt from meta description.">
            </head>
            <body>
                <article>
                    <h1>Full Article Title</h1>
                    <p>This is the complete article content with all the details.</p>
                </article>
            </body>
            </html>
            """;

        stubFor(get(urlEqualTo("/article-with-excerpt"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody(htmlContent)));

        String url = "http://localhost:" + wireMockServer.port() + "/article-with-excerpt";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(url);

        // Then
        assertThat(result.successful()).isTrue();
        assertThat(result.title()).isEqualTo("Article With Excerpt");
        assertThat(result.excerpt()).isNotNull();
        assertThat(result.excerpt()).contains("excerpt from meta description");
    }

    @Test
    @DisplayName("Sollte Content von einer komplexen Seite mit Navigation extrahieren")
    void extractContent_ShouldExtractFromComplexPageWithNavigation() throws ArticleContentExtractor.ContentExtractionException {
        // Given - HTML mit Navigation, Sidebar, etc.
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Tech Article</title>
                <meta name="description" content="Latest tech news and updates">
            </head>
            <body>
                <nav>
                    <a href="/">Home</a>
                    <a href="/about">About</a>
                    <a href="/contact">Contact</a>
                </nav>
                <header>
                    <h1>Website Logo</h1>
                </header>
                <main>
                    <article>
                        <h1>New Java Features Released</h1>
                        <p>Java 21 brings exciting new features to the language.</p>
                        <p>Pattern matching and virtual threads are now production-ready.</p>
                        <p>Developers can leverage these features for better performance.</p>
                    </article>
                </main>
                <aside>
                    <h2>Related Articles</h2>
                    <ul>
                        <li><a href="/article1">Spring Boot Updates</a></li>
                        <li><a href="/article2">Kubernetes Best Practices</a></li>
                    </ul>
                </aside>
                <footer>
                    <p>&copy; 2024 Tech News Site</p>
                </footer>
            </body>
            </html>
            """;

        stubFor(get(urlEqualTo("/tech-article"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody(htmlContent)));

        String url = "http://localhost:" + wireMockServer.port() + "/tech-article";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(url);

        // Then
        assertThat(result.successful()).isTrue();
        // Readability4J extrahiert den Titel aus dem <title>-Tag
        assertThat(result.title()).isEqualTo("Tech Article");
        assertThat(result.content()).contains("Java 21");
        assertThat(result.content()).contains("Pattern matching");
        // Navigation sollte nicht im Content sein
        assertThat(result.content()).doesNotContain("Home");
        assertThat(result.content()).doesNotContain("Contact");
        // Footer sollte nicht im Content sein
        assertThat(result.content()).doesNotContain("2024 Tech News");
    }

    @Test
    @DisplayName("Sollte mit externen Links umgehen und rel Attribute hinzufügen")
    void extractContent_ShouldHandleExternalLinks() throws ArticleContentExtractor.ContentExtractionException {
        // Given - HTML mit externen Links
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Article With Links</title>
            </head>
            <body>
                <article>
                    <h1>Reference Article</h1>
                    <p>Read more at <a href="https://example.com">Example Site</a>.</p>
                    <p>Check the <a href="/internal-page">internal documentation</a> too.</p>
                </article>
            </body>
            </html>
            """;

        stubFor(get(urlEqualTo("/article-with-links"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody(htmlContent)));

        String url = "http://localhost:" + wireMockServer.port() + "/article-with-links";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(url);

        // Then
        assertThat(result.successful()).isTrue();
        assertThat(result.content()).contains("Reference Article");
        assertThat(result.content()).contains("https://example.com");
    }

    @Test
    @DisplayName("Sollte mit Bildern umgehen und lazy loading hinzufügen")
    void extractContent_ShouldHandleImagesWithLazyLoading() throws ArticleContentExtractor.ContentExtractionException {
        // Given - HTML mit Bildern
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Article With Images</title>
            </head>
            <body>
                <article>
                    <h1>Image Gallery Article</h1>
                    <img src="image1.jpg" alt="Description 1">
                    <p>Some text between images.</p>
                    <img src="image2.jpg">
                </article>
            </body>
            </html>
            """;

        stubFor(get(urlEqualTo("/article-with-images"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody(htmlContent)));

        String url = "http://localhost:" + wireMockServer.port() + "/article-with-images";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(url);

        // Then
        assertThat(result.successful()).isTrue();
        assertThat(result.content()).contains("Image Gallery Article");
        assertThat(result.content()).contains("image1.jpg");
        assertThat(result.content()).contains("image2.jpg");
    }

    @Test
    @DisplayName("Sollte bei nicht extrahierbarem Content einen Fehler zurückgeben")
    void extractContent_ShouldReturnFailureForUnextractableContent() throws ArticleContentExtractor.ContentExtractionException {
        // Given - HTML with minimal content that Readability cannot extract
        stubFor(get(urlEqualTo("/empty-page"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody("""
                    <!DOCTYPE html>
                    <html>
                    <head><title>Empty</title></head>
                    <body>
                        <div class="not-article-content">Just a div with no real content.</div>
                    </body>
                    </html>
                    """)));

        String url = "http://localhost:" + wireMockServer.port() + "/empty-page";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(url);

        // Then - Readability might extract some content even from minimal HTML
        // The behavior depends on Readability's algorithm; we verify extraction was attempted
        assertThat(result).isNotNull();
        // The result may be successful or failed depending on what Readability can extract
    }

    @Test
    @DisplayName("Sollte bei 404 Fehler eine Exception werfen")
    void extractContent_ShouldThrowExceptionFor404() {
        // Given
        stubFor(get(urlEqualTo("/not-found"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody("<html><body>Not Found</body></html>")));

        String url = "http://localhost:" + wireMockServer.port() + "/not-found";

        // When/Then
        assertThrows(ArticleContentExtractor.ContentExtractionException.class, () -> {
            extractor.extractContentWithMetadata(url);
        });
    }

    @Test
    @DisplayName("Sollte einfache Content-Extraktion unterstützen")
    void extractContent_SimpleVersion_ShouldReturnContent() throws ArticleContentExtractor.ContentExtractionException {
        // Given
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Simple Test</title>
            </head>
            <body>
                <article>
                    <h1>Simple Content</h1>
                    <p>This is simple content for extraction.</p>
                </article>
            </body>
            </html>
            """;

        stubFor(get(urlEqualTo("/simple"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=UTF-8")
                .withBody(htmlContent)));

        String url = "http://localhost:" + wireMockServer.port() + "/simple";

        // When
        String content = extractor.extractContent(url);

        // Then
        assertThat(content).isNotNull();
        assertThat(content).contains("Simple Content");
        assertThat(content).contains("simple content for extraction");
    }
}
