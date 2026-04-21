package com.newsaggregator.application.service;

import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.in.BulkExtractArticleContentUseCase;
import com.newsaggregator.domain.port.out.ArticleContentExtractor;
import com.newsaggregator.domain.port.out.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für BulkExtractArticleContentService.
 */
@ExtendWith(MockitoExtension.class)
class BulkExtractArticleContentServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleContentExtractor contentExtractor;

    private BulkExtractArticleContentService service;

    @BeforeEach
    void setUp() {
        service = new BulkExtractArticleContentService(articleRepository, contentExtractor);
    }

    private Feed createTestFeed() {
        return Feed.createNew("Test Feed", "https://example.com/feed", "Description");
    }

    private Article createTestArticle(Long id, String title, String link) {
        Feed feed = createTestFeed();
        return Article.of(
                ArticleId.of(id),
                title,
                "Description",
                link,
                null,
                null,
                false,
                LocalDateTime.now(),
                feed,
                LocalDateTime.now()
        );
    }

    @Test
    void extractContentForArticlesWithoutContent_ShouldReturnEmptyResult_WhenNoArticles() {
        // Given
        when(articleRepository.findByContentHtmlIsNull(anyInt())).thenReturn(List.of());

        // When
        BulkExtractArticleContentUseCase.ExtractionResult result =
                service.extractContentForArticlesWithoutContent(10, 100);

        // Then
        assertThat(result.processedCount()).isZero();
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isZero();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void extractContentForArticlesWithoutContent_ShouldExtractContentSuccessfully() throws Exception {
        // Given
        Article article = createTestArticle(1L, "Test Article", "https://example.com/article");
        when(articleRepository.findByContentHtmlIsNull(anyInt())).thenReturn(List.of(article));
        when(contentExtractor.canExtract(any())).thenReturn(true);
        when(contentExtractor.extractContent(any())).thenReturn("<p>Extracted content</p>");

        Article articleWithContent = article.withExtractedContent("<p>Extracted content</p>");
        when(articleRepository.save(any())).thenReturn(articleWithContent);

        // When
        BulkExtractArticleContentUseCase.ExtractionResult result =
                service.extractContentForArticlesWithoutContent(10, 0);

        // Then
        assertThat(result.processedCount()).isEqualTo(1);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failedCount()).isZero();
        verify(contentExtractor).extractContent("https://example.com/article");
        verify(articleRepository).save(any());
    }

    @Test
    void extractContentForArticlesWithoutContent_ShouldHandleExtractionFailure() throws Exception {
        // Given
        Article article = createTestArticle(1L, "Test Article", "https://example.com/article");
        when(articleRepository.findByContentHtmlIsNull(anyInt())).thenReturn(List.of(article));
        when(contentExtractor.canExtract(any())).thenReturn(true);
        when(contentExtractor.extractContent(any())).thenThrow(
                new ArticleContentExtractor.ContentExtractionException("https://example.com/article", "Failed")
        );

        Article failedArticle = article.withExtractionFailed();
        when(articleRepository.save(any())).thenReturn(failedArticle);

        // When
        BulkExtractArticleContentUseCase.ExtractionResult result =
                service.extractContentForArticlesWithoutContent(10, 0);

        // Then
        assertThat(result.processedCount()).isEqualTo(1);
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
    }

    @Test
    void extractContentForArticlesWithoutContent_ShouldSkipWhenCannotExtract() throws Exception {
        // Given
        Article article = createTestArticle(1L, "Test Article", "https://example.com/article");
        when(articleRepository.findByContentHtmlIsNull(anyInt())).thenReturn(List.of(article));
        when(contentExtractor.canExtract(any())).thenReturn(false);

        // When
        BulkExtractArticleContentUseCase.ExtractionResult result =
                service.extractContentForArticlesWithoutContent(10, 0);

        // Then
        assertThat(result.processedCount()).isEqualTo(1);
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isEqualTo(1); // Counts as failed because no content was extracted
        verify(contentExtractor, never()).extractContent(any());
    }

    @Test
    void extractContentForArticlesWithoutContent_ShouldHandleEmptyExtractionResult() throws Exception {
        // Given
        Article article = createTestArticle(1L, "Test Article", "https://example.com/article");
        when(articleRepository.findByContentHtmlIsNull(anyInt())).thenReturn(List.of(article));
        when(contentExtractor.canExtract(any())).thenReturn(true);
        when(contentExtractor.extractContent(any())).thenReturn(null);

        Article failedArticle = article.withExtractionFailed();
        when(articleRepository.save(any())).thenReturn(failedArticle);

        // When
        BulkExtractArticleContentUseCase.ExtractionResult result =
                service.extractContentForArticlesWithoutContent(10, 0);

        // Then
        assertThat(result.processedCount()).isEqualTo(1);
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isEqualTo(1);
    }

    @Test
    void extractContentForArticlesWithoutContent_ShouldRespectLimit() throws Exception {
        // Given
        Article article1 = createTestArticle(1L, "Article 1", "https://example.com/1");
        Article article2 = createTestArticle(2L, "Article 2", "https://example.com/2");
        when(articleRepository.findByContentHtmlIsNull(1)).thenReturn(List.of(article1));
        when(contentExtractor.canExtract(any())).thenReturn(true);
        when(contentExtractor.extractContent(any())).thenReturn("<p>Content</p>");
        when(articleRepository.save(any())).thenReturn(article1.withExtractedContent("<p>Content</p>"));

        // When
        BulkExtractArticleContentUseCase.ExtractionResult result =
                service.extractContentForArticlesWithoutContent(1, 0);

        // Then
        assertThat(result.processedCount()).isEqualTo(1);
        verify(articleRepository).findByContentHtmlIsNull(1);
    }

    @Test
    void extractContentForArticlesWithoutContent_ShouldHandleMultipleArticles() throws Exception {
        // Given
        Article article1 = createTestArticle(1L, "Article 1", "https://example.com/1");
        Article article2 = createTestArticle(2L, "Article 2", "https://example.com/2");
        when(articleRepository.findByContentHtmlIsNull(anyInt())).thenReturn(List.of(article1, article2));
        when(contentExtractor.canExtract(any())).thenReturn(true);
        when(contentExtractor.extractContent(any())).thenReturn("<p>Content</p>");
        when(articleRepository.save(any())).thenAnswer(inv -> {
            Article arg = inv.getArgument(0);
            return arg.withExtractedContent("<p>Content</p>");
        });

        // When
        BulkExtractArticleContentUseCase.ExtractionResult result =
                service.extractContentForArticlesWithoutContent(10, 0);

        // Then
        assertThat(result.processedCount()).isEqualTo(2);
        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.failedCount()).isZero();
        verify(articleRepository).save(argThat(a -> a.getId().equals(article1.getId())));
        verify(articleRepository).save(argThat(a -> a.getId().equals(article2.getId())));
    }

    @Test
    void countArticlesWithoutContent_ShouldReturnCount() {
        // Given
        when(articleRepository.countByContentHtmlIsNull()).thenReturn(42L);

        // When
        long count = service.countArticlesWithoutContent();

        // Then
        assertThat(count).isEqualTo(42L);
    }
}
