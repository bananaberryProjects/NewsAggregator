package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.TrendingTopicDto;
import com.newsaggregator.application.service.TrendingTextAnalyzer.TermToken;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository.ArticleText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service für Trending-Themen mit Lucene-basierter Textanalyse.
 *
 * <p>Flow:
 * 1. Lade Artikel-Texte (Titel + Description) aus der DB
 * 2. Tokenisiere mit {@link TrendingTextAnalyzer} (GermanAnalyzer + Bigrams)
 * 3. Zähle Frequenz pro Term (Bigrams zählen doppelt)
 * 4. Berechne Delta gegen vorheriges Fenster
 * 5. Optionale KI-Filterung (Ollama)
 * 6. Baue DTO mit Trend + Breaking-Detection
 *
 * <p>Ergebnisse werden im Memory gecacht (5 Min TTL).</p>
 */
@Service
public class TrendingTopicService {

    private static final Logger log = LoggerFactory.getLogger(TrendingTopicService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final TrendingTopicRepository trendingRepository;
    private final TrendingTextAnalyzer textAnalyzer;
    private final GptClient gptClient;

    // Simple in-memory cache
    private volatile CacheEntry cache = null;

    public TrendingTopicService(TrendingTopicRepository trendingRepository,
                                 TrendingTextAnalyzer textAnalyzer,
                                 GptClient gptClient) {
        this.trendingRepository = trendingRepository;
        this.textAnalyzer = textAnalyzer;
        this.gptClient = gptClient;
    }

    /**
     * Liefert Trending-Themen mit Hybrid-KI-Filterung.
     */
    public TrendingTopicDto getTrendingTopics(int hours, int limit) {
        if (cache != null && cache.isValid()) {
            log.debug("Trending cache hit");
            return cache.dto;
        }

        TrendingTopicDto result = computeTrending(hours, limit, true);
        this.cache = new CacheEntry(result, LocalDateTime.now().plus(CACHE_TTL));
        return result;
    }

    /**
     * Schneller Endpoint ohne KI-Filterung.
     */
    public TrendingTopicDto getTrendingTopicsFast(int hours, int limit) {
        if (cache != null && cache.isValid()) {
            return cache.dto;
        }

        TrendingTopicDto result = computeTrending(hours, limit, false);
        this.cache = new CacheEntry(result, LocalDateTime.now().plus(CACHE_TTL));
        return result;
    }

    /**
     * Kern-Logik: Statistische Aggregation + optionale KI.
     */
    private TrendingTopicDto computeTrending(int hours, int limit, boolean useAiFilter) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusHours(hours);

        // 1. Artikel-Texte laden
        List<ArticleText> articles = trendingRepository.findArticleTextsSince(since);

        if (articles.isEmpty()) {
            log.info("No articles in last {}h, falling back to 48h", hours);
            since = now.minusHours(48);
            articles = trendingRepository.findArticleTextsSince(since);
        }

        if (articles.isEmpty()) {
            return emptyResult(hours);
        }

        // 2. Lucene-Tokenisierung (Unigrams + Bigrams)
        Map<String, TermAgg> aggregated = tokenizeAndAggregate(articles);

        if (aggregated.isEmpty()) {
            return emptyResult(hours);
        }

        // 3. Delta-Berechnung
        Map<String, Integer> previousCounts = getPreviousWindowCounts(hours);

        // 4. Top N für KI-Filterung
        List<String> topTerms = aggregated.values().stream()
                .sorted(Comparator.comparingInt((TermAgg a) -> a.score).reversed())
                .limit(20)
                .map(a -> a.term)
                .collect(Collectors.toList());

        // 5. KI-Filterung (optional)
        List<String> filteredTerms;
        if (useAiFilter) {
            try {
                filteredTerms = gptClient.filterTrendingTerms(topTerms);
                log.debug("KI filtered {} terms down to {}", topTerms.size(), filteredTerms.size());
            } catch (Exception e) {
                log.warn("KI-Filterung fehlgeschlagen, nutze Statistik pur: {}", e.getMessage());
                filteredTerms = topTerms.stream().limit(limit).collect(Collectors.toList());
            }
        } else {
            filteredTerms = topTerms.stream().limit(limit).collect(Collectors.toList());
        }

        // 6. Baue DTO
        List<TrendingTopicDto.Topic> topics = new ArrayList<>();
        List<TrendingTopicDto.BreakingAlert> breakingAlerts = new ArrayList<>();

        for (String term : filteredTerms) {
            TermAgg agg = aggregated.get(term.toLowerCase());
            if (agg == null) {
                // Versuche exakten Match
                agg = aggregated.values().stream()
                        .filter(a -> a.term.equalsIgnoreCase(term))
                        .findFirst()
                        .orElse(null);
            }
            if (agg == null) continue;

            int prevCount = previousCounts.getOrDefault(agg.term, 0);
            int delta = prevCount > 0
                    ? (int) Math.round(((double) (agg.score - prevCount) / prevCount) * 100)
                    : 0;
            String trend = delta > 50 ? "up" : delta < -30 ? "down" : "stable";
            boolean isBreaking = agg.feedCount >= 3 && agg.score >= 5 && delta > 100;

            topics.add(new TrendingTopicDto.Topic(
                    capitalize(agg.term), agg.score, trend, delta, agg.feedCount, isBreaking
            ));

            if (isBreaking) {
                breakingAlerts.add(new TrendingTopicDto.BreakingAlert(
                        capitalize(agg.term), agg.score, agg.feedCount,
                        String.format("%d neue Artikel in %d Feeds", agg.score, agg.feedCount)
                ));
            }
        }

        String generatedAt = now.format(DateTimeFormatter.ISO_DATE_TIME);
        return new TrendingTopicDto(hours + "h", generatedAt, topics, breakingAlerts);
    }

    /**
     * Tokenisiert Artikel-Texte mit Lucene und aggregiert Term-Frequenzen.
     * Bigrams werden höher gewichtet als Unigrams.
     */
    private Map<String, TermAgg> tokenizeAndAggregate(List<ArticleText> articles) {
        Map<String, TermAgg> result = new HashMap<>();

        for (ArticleText article : articles) {
            // TESTWEISE: nur title, keine description (vermeidet HTML-Noise)
            String combined = article.title != null ? article.title : "";

            List<TermToken> tokens = textAnalyzer.extractTerms(combined);

            for (TermToken token : tokens) {
                String term = token.term.toLowerCase();
                if (term.isEmpty() || term.length() < 3) continue;

                // Bigrams zählen doppelt (höhere semantische Relevanz)
                int weight = token.isBigram ? 2 : 1;

                result.compute(term, (k, v) -> {
                    if (v == null) {
                        return new TermAgg(term, weight, 1);
                    }
                    v.score += weight;
                    v.occurrenceCount++;
                    return v;
                });
            }
        }

        return result;
    }

    /**
     * Holt die Counts aus dem vorherigen identischen Zeitfenster.
     */
    private Map<String, Integer> getPreviousWindowCounts(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime prevEnd = now.minusHours(hours);
        LocalDateTime prevStart = prevEnd.minusHours(hours);
        List<ArticleText> prevArticles = trendingRepository.findArticleTextsSince(prevStart);

        Map<String, Integer> counts = new HashMap<>();
        for (ArticleText article : prevArticles) {
            // TESTWEISE: nur title, keine description (vermeidet HTML-Noise)
            String combined = article.title != null ? article.title : "";
            List<TermToken> tokens = textAnalyzer.extractTerms(combined);
            for (TermToken token : tokens) {
                String term = token.term.toLowerCase();
                int weight = token.isBigram ? 2 : 1;
                counts.merge(term, weight, Integer::sum);
            }
        }
        return counts;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        // Für Bigrams: jedes Wort capitalisieren
        if (str.contains(" ")) {
            String[] words = str.split(" ");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                if (i > 0) sb.append(" ");
                String w = words[i];
                if (!w.isEmpty()) {
                    sb.append(Character.toUpperCase(w.charAt(0)));
                    if (w.length() > 1) sb.append(w.substring(1));
                }
            }
            return sb.toString();
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private TrendingTopicDto emptyResult(int hours) {
        return new TrendingTopicDto(
                hours + "h",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                List.of(),
                List.of()
        );
    }

    // --- Inner Classes ---

    /**
     * Aggregiertes Term mit Score (gewichtete Frequenz) und Feed-Occurrence.
     */
    private static class TermAgg {
        final String term;
        int score;           // gewichtete Punktzahl (Bigrams = 2x)
        int occurrenceCount; // rohe Vorkommen
        int feedCount;       // unique feeds (näherungsweise via articles)

        TermAgg(String term, int score, int feedCount) {
            this.term = term;
            this.score = score;
            this.occurrenceCount = 1;
            this.feedCount = feedCount;
        }
    }

    private static class CacheEntry {
        final TrendingTopicDto dto;
        final LocalDateTime expiresAt;
        CacheEntry(TrendingTopicDto dto, LocalDateTime expiresAt) {
            this.dto = dto;
            this.expiresAt = expiresAt;
        }
        boolean isValid() {
            return LocalDateTime.now().isBefore(expiresAt);
        }
    }
}
