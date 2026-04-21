package com.newsaggregator.infrastructure.adapter.content;

import com.newsaggregator.domain.port.out.ArticleContentExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für den ReadabilityContentExtractorAdapter.
 *
 * <p>Hinweis: Tests für die tatsächliche Content-Extraktion über HTTP
 * sind Integrationstests und verwenden einen Mock-Server oder sind deaktiviert.</p>
 */
class ReadabilityContentExtractorAdapterTest {

    private ReadabilityContentExtractorAdapter extractor;

    @BeforeEach
    void setUp() {
        extractor = new ReadabilityContentExtractorAdapter();
    }

    @Test
    void canExtract_ShouldReturnTrue_ForHttpUrl() {
        assertTrue(extractor.canExtract("http://example.com/article"));
    }

    @Test
    void canExtract_ShouldReturnTrue_ForHttpsUrl() {
        assertTrue(extractor.canExtract("https://example.com/article"));
    }

    @Test
    void canExtract_ShouldReturnFalse_ForFtpUrl() {
        assertFalse(extractor.canExtract("ftp://example.com/file"));
    }

    @Test
    void canExtract_ShouldReturnFalse_ForNullUrl() {
        assertFalse(extractor.canExtract(null));
    }

    @Test
    void canExtract_ShouldReturnFalse_ForEmptyUrl() {
        assertFalse(extractor.canExtract(""));
    }

    @Test
    void canExtract_ShouldReturnFalse_ForBlankUrl() {
        assertFalse(extractor.canExtract("   "));
    }

    @Test
    void canExtract_ShouldReturnFalse_ForMailtoUrl() {
        assertFalse(extractor.canExtract("mailto:test@example.com"));
    }

    @Test
    void canExtract_ShouldReturnTrue_ForHttpUrl_Uppercase() {
        assertTrue(extractor.canExtract("HTTP://example.com/article"));
    }

    @Test
    void extractContent_WithMetadata_ShouldReturnFailure_ForInvalidUrl() {
        // Given
        String invalidUrl = "not-a-valid-url";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(invalidUrl);

        // Then
        assertNotNull(result);
        assertFalse(result.successful());
        assertNull(result.content());
    }

    @Test
    void extractContent_ShouldReturnFailureResult_ForUnsupportedProtocol() {
        // Given
        String unsupportedUrl = "ftp://example.com/file.txt";

        // When
        ArticleContentExtractor.ExtractionResult result = extractor.extractContentWithMetadata(unsupportedUrl);

        // Then
        assertNotNull(result);
        assertFalse(result.successful());
        assertNull(result.content());
    }
}
