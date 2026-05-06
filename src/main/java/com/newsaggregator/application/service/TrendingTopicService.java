package com.newsaggregator.application.service;

import com.newsaggregator.application.dto.TrendingTopicDto;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository.ArticleText;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository.TermStats;
import com.newsaggregator.infrastructure.adapter.persistence.repository.TrendingTopicRepository.TitleFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service für Trending-Themen mit Hybrid-Ansatz:
 * 1. Statistische Aggregation aus der DB (schnell)
 * 2. KI-Filterung der Top-Terme via Ollama (qualitativ)
 *
 * <p>Ergebnisse werden im Memory gecacht (5 Min TTL).</p>
 */
@Service
public class TrendingTopicService {

    private static final Logger log = LoggerFactory.getLogger(TrendingTopicService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final TrendingTopicRepository trendingRepository;
    private final GptClient gptClient;  // reused from AiSummaryService / MarketInsightService

    // Simple in-memory cache
    private volatile CacheEntry cache = null;

    // Stopwords (deutsch + englisch + domain-noise)
    private static final Set<String> STOPWORDS = Set.of(
        "der", "die", "das", "ein", "eine", "und", "oder", "mit", "für", "von", "in", "zu",
        "auf", "ist", "sind", "war", "wurde", "wird", "nicht", "bei", "nach", "wie", "als",
        "um", "über", "aus", "an", "durch", "vor", "zum", "zur", "am", "im", "es", "er",
        "sie", "ihn", "ihr", "uns", "du", "ich", "man", "was", "wer", "wo", "wann", "warum",
        "wenn", "dann", "so", "auch", "nur", "noch", "schon", "immer", "hier", "da", "jetzt",
        "neu", "neue", "neuer", "neues", "heute", "gestern", "morgen", "erste", "zweite",
        "dritte", "mehr", "weniger", "viel", "alle", "jede", "jeder", "jedes", "kein",
        "the", "a", "an", "to", "of", "and", "for", "on", "is", "are", "was", "were", "be",
        "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "could",
        "should", "may", "might", "must", "shall", "can", "need", "dare", "ought", "used",
        "this", "that", "these", "those", "i", "you", "he", "she", "it", "we", "they",
        "me", "him", "her", "us", "them", "my", "your", "his", "its", "our", "their",
        "what", "which", "who", "when", "where", "why", "how", "all", "each", "every",
        "both", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only",
        "own", "same", "so", "than", "too", "very", "just", "now", "then", "here", "there",
        "up", "out", "if", "about", "into", "through", "during", "before", "after", "above",
        "below", "between", "under", "again", "further", "once", "from", "down", "off", "over",
        "at", "by", "with", "against", "because", "until", "while", "since", "until"
    );

    public TrendingTopicService(TrendingTopicRepository trendingRepository, GptClient gptClient) {
        this.trendingRepository = trendingRepository;
        this.gptClient = gptClient;
    }

    /**
     * Liefert Trending-Themen mit Hybrid-KI-Filterung.
     *
     * @param hours Zeitraum (default 24)
     * @param limit Maximale Anzahl Topics (default 20)
     */
    public TrendingTopicDto getTrendingTopics(int hours, int limit) {
        // 1. Cache check
        if (cache != null && cache.isValid()) {
            log.debug("Trending cache hit");
            return cache.dto;
        }

        // 2. Statistische Aggregation
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusHours(hours);
        List<TermStats> currentStats = trendingRepository.findTopTermsInWindow(since, now, 2, limit * 2);

        if (currentStats.isEmpty()) {
            // Fallback: letzte 48h
            log.info("No articles in last {}h, falling back to 48h", hours);
            since = now.minusHours(48);
            currentStats = trendingRepository.findTopTermsInWindow(since, now, 1, limit * 2);
        }

        if (currentStats.isEmpty()) {
            return emptyResult(hours);
        }

        // 3. Token-basierte Feinfilterung (Java-seitig, lemmatisiert nicht)
        Map<String, TermAgg> aggregated = aggregateTerms(currentStats);

        // 4. Delta-Berechnung (vergleiche mit gleichem Fenster vorher)
        Map<String, Integer> previousCounts = getPreviousWindowCounts(hours);

        // 5. Top N für KI-Filterung
        List<String> topTerms = aggregated.values().stream()
                .sorted(Comparator.comparingInt((TermAgg a) -> a.count).reversed())
                .limit(15)
                .map(a -> a.term)
                .collect(Collectors.toList());

        // 6. KI-Filterung (Ollama) – entfernt Noise-Wörter, gruppiert Synonyme
        List<String> filteredTerms;
        try {
            filteredTerms = gptClient.filterTrendingTerms(topTerms);
            log.debug("KI filtered {} terms down to {}", topTerms.size(), filteredTerms.size());
        } catch (Exception e) {
            log.warn("KI-Filterung fehlgeschlagen, nutze Statistik pur: {}", e.getMessage());
            filteredTerms = topTerms.stream().limit(limit).collect(Collectors.toList());
        }

        // 7. Baue DTO
        List<TrendingTopicDto.Topic> topics = new ArrayList<>();
        List<TrendingTopicDto.BreakingAlert> breakingAlerts = new ArrayList<>();

        for (String term : filteredTerms) {
            TermAgg agg = aggregated.get(term);
            if (agg == null) continue;

            int prevCount = previousCounts.getOrDefault(term, 0);
            int delta = prevCount > 0 ? (int) Math.round(((double)(agg.count - prevCount) / prevCount) * 100) : 0;
            String trend = delta > 50 ? "up" : delta < -30 ? "down" : "stable";
            boolean isBreaking = agg.feedCount >= 3 && agg.count >= 5 && delta > 100;

            topics.add(new TrendingTopicDto.Topic(
                    capitalize(term), agg.count, trend, delta, agg.feedCount, isBreaking
            ));

            if (isBreaking) {
                breakingAlerts.add(new TrendingTopicDto.BreakingAlert(
                        capitalize(term), agg.count, agg.feedCount,
                        String.format("%d neue Artikel in %d Feeds", agg.count, agg.feedCount)
                ));
            }
        }

        String generatedAt = now.format(DateTimeFormatter.ISO_DATE_TIME);
        TrendingTopicDto result = new TrendingTopicDto(hours + "h", generatedAt, topics, breakingAlerts);

        // 8. Cache
        this.cache = new CacheEntry(result, now.plus(CACHE_TTL));
        return result;
    }

    /**
     * Native-Statistik-Modus: Nur Statistik, keine KI (schneller, weniger Abhängigkeiten).
     */
    public TrendingTopicDto getTrendingTopicsFast(int hours, int limit) {
        if (cache != null && cache.isValid()) {
            return cache.dto;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusHours(hours);
        List<TermStats> stats = trendingRepository.findTopTermsInWindow(since, now, 2, limit * 2);

        if (stats.isEmpty()) {
            since = now.minusHours(48);
            stats = trendingRepository.findTopTermsInWindow(since, now, 1, limit * 2);
        }

        if (stats.isEmpty()) {
            return emptyResult(hours);
        }

        Map<String, TermAgg> aggregated = aggregateTerms(stats);
        Map<String, Integer> previousCounts = getPreviousWindowCounts(hours);

        List<TrendingTopicDto.Topic> topics = aggregated.values().stream()
                .sorted(Comparator.comparingInt((TermAgg a) -> a.count).reversed())
                .limit(limit)
                .map(agg -> {
                    int prev = previousCounts.getOrDefault(agg.term, 0);
                    int delta = prev > 0 ? (int) Math.round(((double)(agg.count - prev) / prev) * 100) : 0;
                    String trend = delta > 50 ? "up" : delta < -30 ? "down" : "stable";
                    boolean breaking = agg.feedCount >= 3 && agg.count >= 5 && delta > 100;
                    return new TrendingTopicDto.Topic(
                            capitalize(agg.term), agg.count, trend, delta, agg.feedCount, breaking
                    );
                })
                .collect(Collectors.toList());

        List<TrendingTopicDto.BreakingAlert> breakingAlerts = topics.stream()
                .filter(TrendingTopicDto.Topic::isBreaking)
                .map(t -> new TrendingTopicDto.BreakingAlert(
                        t.getTerm(), t.getCount(), t.getFeeds(),
                        String.format("%d Artikel in %d Feeds", t.getCount(), t.getFeeds())
                ))
                .collect(Collectors.toList());

        TrendingTopicDto result = new TrendingTopicDto(hours + "h", now.format(DateTimeFormatter.ISO_DATE_TIME), topics, breakingAlerts);
        this.cache = new CacheEntry(result, now.plus(CACHE_TTL));
        return result;
    }

    /**
     * Aggregiert TermStats zu konsolidierten TermAgg (stopword-filter + merge).
     */
    private Map<String, TermAgg> aggregateTerms(List<TermStats> stats) {
        Map<String, TermAgg> result = new HashMap<>();
        for (TermStats ts : stats) {
            if (ts.term == null) continue;
            String[] words = ts.term.split("\\s+");
            for (String rawWord : words) {
                String word = rawWord.trim();
                if (word.length() < 3 || STOPWORDS.contains(word)) continue;
                final String w = word;
                final int ac = ts.articleCount;
                final int fc = ts.feedCount;
                result.compute(w, (k, v) -> {
                    if (v == null) return new TermAgg(w, ac, fc);
                    v.count += ac;
                    v.feedCount = Math.max(v.feedCount, fc);
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
        List<TermStats> prevStats = trendingRepository.findTopTermsInWindow(prevStart, prevEnd, 1, 100);

        Map<String, Integer> result = new HashMap<>();
        for (TermStats ts : prevStats) {
            if (ts.term == null) continue;
            String[] words = ts.term.split("\\s+");
            for (String word : words) {
                word = word.trim();
                if (word.length() < 3 || STOPWORDS.contains(word)) continue;
                result.merge(word, ts.articleCount, Integer::sum);
            }
        }
        return result;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
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

    private static class TermAgg {
        final String term;
        int count;
        int feedCount;
        TermAgg(String term, int count, int feedCount) {
            this.term = term;
            this.count = count;
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
