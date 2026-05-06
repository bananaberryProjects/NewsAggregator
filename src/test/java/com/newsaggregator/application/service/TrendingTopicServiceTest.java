package com.newsaggregator.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.newsaggregator.application.dto.TrendingTopicDto;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository.ArticleText;

/**
 * Unit-Test für TrendingTopicService mit Lucene-Analyse.
 */
@ExtendWith(MockitoExtension.class)
class TrendingTopicServiceTest {

    @Mock
    private TrendingTopicRepository trendingRepository;

    @Mock
    private GptClient gptClient;

    private TrendingTextAnalyzer textAnalyzer;
    private TrendingTopicService service;

    @BeforeEach
    void setUp() {
        // Echter Lucene-Analyzer (kein Mock — will seine Tokenisierung testen)
        textAnalyzer = new TrendingTextAnalyzer();
        service = new TrendingTopicService(trendingRepository, textAnalyzer, gptClient);
    }

    @Test
    void getTrendingTopicsFast_WithArticles_ShouldReturnTopics() {
        // Given
        List<ArticleText> articles = List.of(
                new ArticleText("Bitcoin ETF Zulassung in den USA", "Die SEC hat den Bitcoin ETF endlich zugelassen."),
                new ArticleText("DAX erreicht neues Rekordhoch", "Der DAX klettert auf 18.000 Punkte."),
                new ArticleText("KI Regulierung in der EU", "Die EU arbeitet an neuen KI Regeln."),
                new ArticleText("Bitcoin Kurs explodiert", "Nach der ETF Zulassung steigt Bitcoin stark."),
                new ArticleText("Ethereum Update steht bevor", "Das nächste Ethereum Upgrade kommt bald.")
        );

        when(trendingRepository.findArticleTextsSince(any(LocalDateTime.class)))
                .thenReturn(articles);

        // When
        TrendingTopicDto result = service.getTrendingTopicsFast(24, 10);

        // Then
        assertThat(result.getWindow()).isEqualTo("24h");
        assertThat(result.getTopics()).isNotEmpty();

        // "bitcoin" sollte durch Lucene gestemmt auftauchen
        List<String> terms = result.getTopics().stream()
                .map(TrendingTopicDto.Topic::getTerm)
                .map(String::toLowerCase)
                .toList();

        assertThat(terms).contains("Bitcoin");

        // "Bitcoin ETF" als Bigram sollte auch auftauchen
        assertThat(terms).anyMatch(t -> t.contains(" ") && t.toLowerCase().contains("bitcoin"));
    }

    @Test
    void getTrendingTopicsFast_NoArticles_ShouldReturnEmpty() {
        // Given
        when(trendingRepository.findArticleTextsSince(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // When
        TrendingTopicDto result = service.getTrendingTopicsFast(24, 10);

        // Then
        assertThat(result.getWindow()).isEqualTo("24h");
        assertThat(result.getTopics()).isEmpty();
        assertThat(result.getBreakingAlerts()).isEmpty();
    }

    @Test
    void getTrendingTopicsFast_WithHtmlAndUrls_ShouldNotContainHtmlTokens() {
        // Given — Artikel mit HTML-Markup und URLs in der Description
        List<ArticleText> articles = List.of(
                new ArticleText(
                        "Bitcoin ETF Zulassung",
                        "<div class='news'><span>Die SEC hat den Bitcoin ETF zugelassen.</span>"
                                + " <a href='https://example.com/article/123'>Mehr lesen</a>"
                                + " Bildbreite: 800px. Quelle: https://www.btc-news.de/update?id=42&amp;ref=top"
                ),
                new ArticleText(
                        "Bitcoin Kurs",
                        "Nach der Zulassung steigt Bitcoin. \u003cscript>alert('x')\u003c/script>"
                                + " Mehr unter www.bitcoin.de und http://crypto.org/net"
                )
        );

        when(trendingRepository.findArticleTextsSince(any(LocalDateTime.class)))
                .thenReturn(articles);

        // When
        TrendingTopicDto result = service.getTrendingTopicsFast(24, 10);

        // Then — HTML/CSS/URL-Reste dürfen nicht als Topics auftauchen
        List<String> terms = result.getTopics().stream()
                .map(TrendingTopicDto.Topic::getTerm)
                .map(String::toLowerCase)
                .toList();

        assertThat(terms)
                .noneMatch(t -> t.contains("div") || t.contains("span") || t.contains("script")
                        || t.contains("style") || t.contains("width") || t.contains("px")
                        || t.contains("href") || t.contains("class") || t.contains("src"))
                .noneMatch(t -> t.startsWith("http") || t.contains("www.")
                        || t.contains(".com") || t.contains(".de") || t.contains(".org"));

        // Stattdessen sollte "Bitcoin" und "Bitcoin ETF" auftauchen
        assertThat(terms).anyMatch(t -> t.toLowerCase().contains("bitcoin"));
    }

    @Test
    void getTrendingTopicsFast_StopwordsShouldBeFiltered() {
        // Given — Titel mit vielen Stopwords
        List<ArticleText> articles = List.of(
                new ArticleText("Der die das ein neue Update", "Heute und morgen ist es sehr wichtig.")
        );

        when(trendingRepository.findArticleTextsSince(any(LocalDateTime.class)))
                .thenReturn(articles);

        // When
        TrendingTopicDto result = service.getTrendingTopicsFast(24, 10);

        // Then — reine Stopwords sollten nicht als Topics auftauchen
        List<String> terms = result.getTopics().stream()
                .map(TrendingTopicDto.Topic::getTerm)
                .map(String::toLowerCase)
                .toList();

        assertThat(terms).noneMatch(t -> t.equals("Der") || t.equals("Die") || t.equals("Das")
                || t.equals("Ein") || t.equals("Und") || t.equals("Heute"));
    }

    @Test
    void getTrendingTopicsFast_Cache_ShouldReturnCachedResult() {
        // Given
        List<ArticleText> articles = List.of(
                new ArticleText("Test Artikel", "Beschreibung")
        );
        when(trendingRepository.findArticleTextsSince(any(LocalDateTime.class)))
                .thenReturn(articles);

        // First call
        TrendingTopicDto first = service.getTrendingTopicsFast(24, 10);

        // Second call (should hit cache)
        TrendingTopicDto second = service.getTrendingTopicsFast(24, 10);

        // Then
        assertThat(second.getTopics()).hasSameSizeAs(first.getTopics());
    }

    @Test
    void getTrendingTopics_WithAiFilter_ShouldFilterTerms() throws Exception {
        // Given
        List<ArticleText> articles = List.of(
                new ArticleText("Bitcoin ETF Zulassung", "Bitcoin ETF News"),
                new ArticleText("Bitcoin Kurs", "Bitcoin Preis"),
                new ArticleText("DAX Rekord", "DAX steigt"),
                new ArticleText("DAX Analyse", "DAX Heute")
        );

        when(trendingRepository.findArticleTextsSince(any(LocalDateTime.class)))
                .thenReturn(articles);
        when(gptClient.filterTrendingTerms(any()))
                .thenReturn(List.of("Bitcoin", "DAX"));

        // When
        TrendingTopicDto result = service.getTrendingTopics(24, 10);

        // Then
        assertThat(result.getTopics()).hasSize(2);
        assertThat(result.getTopics().get(0).getTerm()).isEqualTo("Bitcoin");
        assertThat(result.getTopics().get(1).getTerm()).isEqualTo("DAX");
    }
}
