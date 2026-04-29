package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.AiSummaryDto;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.ArticleId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.model.FeedId;
import com.newsaggregator.domain.model.FeedStatus;
import com.newsaggregator.domain.model.Category;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.domain.port.out.CategoryRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.AiSummaryCacheJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für den strukturierten KI-Zusammenfassungs-Service.
 * Testet die Fallback-Logik und Topic-Extraktion (ohne Ollama-Request).
 */
@ExtendWith(MockitoExtension.class)
class AiSummaryServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AiSummaryCacheJpaRepository cacheRepository;

    private AiSummaryService aiSummaryService;

    @BeforeEach
    void setup() {
        aiSummaryService = new AiSummaryService(articleRepository, categoryRepository, cacheRepository);
    }

    @Test
    void shouldReturnEmptySummaryWhenNoArticles() {
        when(articleRepository.findByPublishedAtAfter(any())).thenReturn(List.of());
        when(articleRepository.findAll()).thenReturn(List.of());
        // categoryRepository.findAll() wird gar nicht aufgerufen bei leeren Artikeln

        AiSummaryDto result = aiSummaryService.generateStructuredSummary();

        assertThat(result.getCategories()).hasSize(1);
        assertThat(result.getCategories().get(0).getName()).isEqualTo("Allgemein");
        assertThat(result.getCategories().get(0).getSummary()).contains("Keine neuen Artikel");
        assertThat(result.getTopTopics()).isEmpty();
    }

    @Test
    void shouldReturnFallbackSummaryWithTopics() {
        Feed feed = Feed.of(
            FeedId.of(1L), "Tech-News", "https://example.com", "Tech Beschreibung",
            LocalDateTime.now(), LocalDateTime.now(), FeedStatus.ACTIVE
        );

        Article a1 = Article.of(
            ArticleId.of(1L), "Bitcoin ETF Zulassung", "Beschreibung...", "https://x.com",
            LocalDateTime.now(), feed, LocalDateTime.now()
        );
        Article a2 = Article.of(
            ArticleId.of(2L), "Apple Vision Pro Test", "Beschreibung...", "https://y.com",
            LocalDateTime.now(), feed, LocalDateTime.now()
        );
        Article a3 = Article.of(
            ArticleId.of(3L), "Bitcoin Mining Update", "Beschreibung...", "https://z.com",
            LocalDateTime.now(), feed, LocalDateTime.now()
        );

        when(articleRepository.findByPublishedAtAfter(any())).thenReturn(List.of(a1, a2, a3));
        when(categoryRepository.findAll()).thenReturn(List.of(
            Category.create("Technologie", "#42A5F5", "computer")
        ));

        // Mock Ollama-Fehler damit Fallback greift
        // Der Service nutzt @Value für ollamaBaseUrl — aber der RestTemplate.postForObject wird fehlschlagen,
        // weil ollamaBaseUrl keinen echten Server hat. Das Exception-Handling sorgt dann für den Fallback.
        AiSummaryDto result = aiSummaryService.generateStructuredSummary();

        assertThat(result.getCategories()).isNotEmpty();
        assertThat(result.getTopTopics()).isNotEmpty();

        // "Bitcoin" kommt in 2 Titeln vor → sollte als Topic auftauchen
        assertThat(result.getTopTopics())
            .extracting(AiSummaryDto.AiTopic::getName)
            .anyMatch(name -> name.toLowerCase().contains("bitcoin"));
    }

    @Test
    void shouldUseFallbackWhenOllamaFails() {
        Feed feed = Feed.of(
            FeedId.of(1L), "News", "https://example.com", "Beschreibung",
            LocalDateTime.now(), LocalDateTime.now(), FeedStatus.ACTIVE
        );
        Article article = Article.of(
            ArticleId.of(1L), "Test Artikel", "Desc", "https://example.com",
            LocalDateTime.now(), feed, LocalDateTime.now()
        );

        when(articleRepository.findByPublishedAtAfter(any())).thenReturn(List.of(article));
        when(categoryRepository.findAll()).thenReturn(List.of());

        // Ollama ist nicht erreichbar → Exception → Fallback
        AiSummaryDto result = aiSummaryService.generateStructuredSummary();

        assertThat(result.getCategories()).isNotEmpty();
        assertThat(result.getCategories().get(0).getArticleCount()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldUseRecentArticlesWhenTodayHasNone() {
        Feed feed = Feed.of(
            FeedId.of(1L), "News", "https://example.com", "Beschreibung",
            LocalDateTime.now(), LocalDateTime.now(), FeedStatus.ACTIVE
        );
        Article oldArticle = Article.of(
            ArticleId.of(1L), "Alter Artikel", "Desc", "https://old.com",
            LocalDateTime.now().minusDays(2), feed, LocalDateTime.now()
        );

        when(articleRepository.findByPublishedAtAfter(any())).thenReturn(List.of());
        when(articleRepository.findAll()).thenReturn(List.of(oldArticle));
        when(categoryRepository.findAll()).thenReturn(List.of());

        AiSummaryDto result = aiSummaryService.generateStructuredSummary();

        // Fallback Summary sollte den einen Artikel erwähnen
        assertThat(result.getCategories()).isNotEmpty();
        assertThat(result.getCategories().get(0).getArticleCount()).isEqualTo(1);
    }
}
