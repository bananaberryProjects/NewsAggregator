package com.newsaggregator.application.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.application.dto.AiSummaryDto;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.model.Category;
import com.newsaggregator.domain.model.CategoryId;
import com.newsaggregator.domain.model.Feed;
import com.newsaggregator.domain.port.out.ArticleRepository;
import com.newsaggregator.domain.port.out.CategoryRepository;
import com.newsaggregator.infrastructure.adapter.persistence.entity.AiSummaryCacheJpaEntity;
import com.newsaggregator.infrastructure.adapter.persistence.repository.AiSummaryCacheJpaRepository;

/**
 * Service fuer strukturierte KI-Tageszusammenfassung v2.
 * Liefert kategorisierte Uebersichten mit Sentiment und Top-Themen.
 *
 * <p>Caching: Ergebnisse werden in ai_summary_cache gespeichert.
 * Bei Cache-Miss wird Ollama aufgerufen und das Ergebnis gecacht.
 * Der Cache wird nach jedem Feed-Import invalidiert.</p>
 */
@Service
public class AiSummaryService {

    private static final Logger log = LoggerFactory.getLogger(AiSummaryService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(90); // 1,5 Stunden statt 1 Stunde (etwas Puffer)

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final AiSummaryCacheJpaRepository cacheRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url:http://host.docker.internal:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:gpt-oss:120b-cloud}")
    private String ollamaModel;

    public AiSummaryService(ArticleRepository articleRepository,
                            CategoryRepository categoryRepository,
                            AiSummaryCacheJpaRepository cacheRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.cacheRepository = cacheRepository;
        this.restTemplate = createRestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10s connect
        factory.setReadTimeout(300000);  // 5mins read (Ollama braucht viel Zeit)
        return new RestTemplate(factory);
    }

    /**
     * Generiert eine strukturierte Zusammenfassung der Artikel von heute.
     * Prueft zuerst den Cache, bei Cache-Miss wird Ollama aufgerufen.
     */
    public AiSummaryDto generateStructuredSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 1. Cache pruefen
        Optional<AiSummaryCacheJpaEntity> cached = cacheRepository.findByForDate(today);
        if (cached.isPresent()) {
            AiSummaryCacheJpaEntity entry = cached.get();
            if (entry.getExpiresAt().isAfter(now)) {
                log.debug("Cache-Hit fuer {}", today);
                try {
                    return objectMapper.readValue(entry.getSummaryJson(), AiSummaryDto.class);
                } catch (Exception e) {
                    log.warn("Cache-Inhalt kaputt, wird neu generiert: {}", e.getMessage());
                }
            } else {
                log.debug("Cache abgelaufen fuer {}", today);
            }
        }

        // 2. Artikel laden
        LocalDateTime todayStart = today.atStartOfDay();
        List<Article> articles = articleRepository.findByPublishedAtAfter(todayStart);

        // Fallback: wenn keine heutigen Artikel, nimm die neuesten 20
        if (articles.isEmpty()) {
            articles = articleRepository.findAll().stream()
                    .sorted(Comparator.comparing(Article::getPublishedAt).reversed())
                    .limit(20)
                    .toList();
        }

        if (articles.isEmpty()) {
            return emptySummary();
        }

        // 3. Ollama aufrufen (oder Fallback)
        AiSummaryDto result;
        boolean isFallback = false;
        try {
            List<Category> allCategories = categoryRepository.findAll();
            String categoryNames = allCategories.stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));
            String jsonResponse = callOllamaStructured(articles, categoryNames);
            result = parseAiResponse(jsonResponse, articles);
        } catch (Exception e) {
            log.warn("Strukturierte Zusammenfassung fehlgeschlagen, Fallback wird verwendet: {}", e.getMessage());
            result = fallbackSummary(articles);
            isFallback = true;
        }

        // 4. Ergebnis cachen
        saveToCache(today, result, isFallback);

        return result;
    }

    /**
     * Speichert ein generiertes AiSummaryDto in den Cache.
     */
    private void saveToCache(LocalDate forDate, AiSummaryDto summary, boolean isFallback) {
        try {
            String json = objectMapper.writeValueAsString(summary);
            AiSummaryCacheJpaEntity entry = new AiSummaryCacheJpaEntity();
            entry.setForDate(forDate);
            entry.setSummaryJson(json);
            entry.setFallback(isFallback);
            entry.setCreatedAt(LocalDateTime.now());
            entry.setExpiresAt(LocalDateTime.now().plus(CACHE_TTL));

            cacheRepository.findByForDate(forDate).ifPresent(existing -> {
                entry.setId(existing.getId());
            });

            cacheRepository.save(entry);
            if (isFallback) {
                log.debug("Fallback-Summary fuer {} gecacht", forDate);
            } else {
                log.info("KI-Summary fuer {} gecacht (TTL: {} Min)", forDate, CACHE_TTL.toMinutes());
            }
        } catch (Exception e) {
            log.warn("Konnte Summary nicht cachen: {}", e.getMessage());
        }
    }

    /**
     * Invalidiert den Cache fuer einen bestimmten Tag.
     * Wird nach Feed-Import aufgerufen.
     */
    public void invalidateCache(LocalDate date) {
        try {
            cacheRepository.deleteByForDate(date);
            log.info("KI-Summary-Cache fuer {} invalidiert", date);
        } catch (Exception e) {
            log.warn("Cache-Invalidierung fehlgeschlagen fuer {}: {}", date, e.getMessage());
        }
    }

    /**
     * Invalidiert den Cache fuer heute.
     */
    public void invalidateTodayCache() {
        invalidateCache(LocalDate.now());
    }

    private String callOllamaStructured(List<Article> articles, String categoryNames) throws Exception {
        // Quick sanity check: is Ollama reachable?
        try {
            restTemplate.getForEntity(ollamaBaseUrl + "/api/tags", String.class);
        } catch (Exception ex) {
            throw new RuntimeException("Ollama not reachable at " + ollamaBaseUrl + ": " + ex.getMessage());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        StringBuilder prompt = new StringBuilder();
        prompt.append("Du bist ein Nachrichten-Assistent. Analysiere die folgenden Artikel und erstelle eine strukturierte Zusammenfassung im JSON-Format.\n\n");
        prompt.append("Verfuegbare Kategorien: ").append(categoryNames.isEmpty() ? "Allgemein" : categoryNames).append("\n\n");
        prompt.append("Artikel (Titel + Kurzbeschreibung):\n");

        for (Article article : articles.stream().limit(15).toList()) {
            prompt.append("- ").append(article.getTitle());
            if (article.getDescription() != null && !article.getDescription().isEmpty()) {
                String desc = article.getDescription().substring(0, Math.min(article.getDescription().length(), 150));
                prompt.append(" | ").append(desc);
            }
            if (article.getFeed() != null && article.getFeed().getName() != null) {
                prompt.append(" [Feed: ").append(article.getFeed().getName()).append("]");
            }
            prompt.append("\n");
        }

        prompt.append("\n---\n");
        prompt.append("Antworte AUSSCHLIESSLICH mit gueltigem JSON (kein Markdown, keine Erklaerungen). Das Format:\n");
        prompt.append("{\n");
        prompt.append("  \"categories\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"name\": \"Kategorie-Name\",\n");
        prompt.append("      \"summary\": \"2-3 Saetze Zusammenfassung\",\n");
        prompt.append("      \"articleCount\": 5,\n");
        prompt.append("      \"sentiment\": \"positive\" // oder \"neutral\" oder \"negative\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"topTopics\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"name\": \"Thema in 2 bis 3 Worten\",\n");
        prompt.append("      \"articleCount\": 3,\n");
        prompt.append("      \"trending\": true // oder false\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("Waehle 3-5 Kategorien und 3 Top-Themen. Nutze ausschließlich die deutsche Sprache. 'trending' = true wenn das Thema besonders viele Artikel hat oder kontrovers ist.\n");

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.5);
        options.put("num_predict", 4096);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("prompt", prompt.toString());
        requestBody.put("stream", false);
        requestBody.put("format", "json"); // enforce JSON output (Ollama >= 0.1.24)
        requestBody.put("options", options);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String url = ollamaBaseUrl + "/api/generate";
        String response = restTemplate.postForObject(url, request, String.class);

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("response").asText();
    }

    /**
     * Parst die JSON-Antwort von Ollama und baut das DTO.
     */
    private AiSummaryDto parseAiResponse(String jsonResponse, List<Article> articles) throws Exception {
        int totalArticles = articles.size();
        // Ollama's `format: "json"` guarantees valid JSON, but may wrap in markdown
        jsonResponse = jsonResponse.trim();
        if (jsonResponse.startsWith("```json")) {
            jsonResponse = jsonResponse.substring(7);
        }
        if (jsonResponse.startsWith("```")) {
            jsonResponse = jsonResponse.substring(3);
        }
        if (jsonResponse.endsWith("```")) {
            jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
        }
        jsonResponse = jsonResponse.trim();

        JsonNode root = objectMapper.readTree(jsonResponse);

        List<AiSummaryDto.AiCategory> categories = new ArrayList<>();
        JsonNode cats = root.path("categories");
        if (cats.isArray()) {
            for (JsonNode cat : cats) {
                String name = cat.path("name").asText("Allgemein");
                String summary = cat.path("summary").asText("Keine Zusammenfassung verfuegbar.");
                int count = cat.path("articleCount").asInt(0);
                String sentiment = cat.path("sentiment").asText("neutral");
                categories.add(new AiSummaryDto.AiCategory(name, summary, count, sentiment));
            }
        }

        // Wenn keine Kategorien zurueckkamen -> wirf Exception fuer Fallback
        if (categories.isEmpty() && totalArticles > 0) {
            throw new RuntimeException("No categories in AI response");
        }

        List<AiSummaryDto.AiTopic> topics = new ArrayList<>();
        JsonNode tops = root.path("topTopics");
        if (tops.isArray()) {
            for (JsonNode top : tops) {
                String name = top.path("name").asText("Unbekannt");
                int count = top.path("articleCount").asInt(0);
                boolean trending = top.path("trending").asBoolean(false);
                topics.add(new AiSummaryDto.AiTopic(name, count, trending));
            }
        }

        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new AiSummaryDto(categories, topics, generatedAt);
    }

    /**
     * Fallback wenn Ollama nicht antwortet.
     * Gruppiert Artikel nach Feed-Kategorien und extrahiert Themen lokal.
     */
    private AiSummaryDto fallbackSummary(List<Article> articles) {
        // Lade Kategorien-Mapping
        Map<CategoryId, Category> categoryById = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(
                        c -> new CategoryId(c.getId().getValue()),
                        c -> c
                ));

        // Gruppiere Artikel nach Kategorie
        Map<String, List<Article>> articlesByCategory = new LinkedHashMap<>();
        for (Article a : articles) {
            Feed feed = a.getFeed();
            List<CategoryId> catIds = feed.getCategoryIds();
            if (catIds == null || catIds.isEmpty()) {
                articlesByCategory.computeIfAbsent("Allgemein", k -> new ArrayList<>()).add(a);
            } else {
                for (CategoryId cid : catIds) {
                    Category cat = categoryById.get(cid);
                    String catName = (cat != null) ? cat.getName() : "Allgemein";
                    articlesByCategory.computeIfAbsent(catName, k -> new ArrayList<>()).add(a);
                }
            }
        }

        // Baue Kategorien-Liste (max 5, absteigend nach Artikel-Count)
        List<AiSummaryDto.AiCategory> categories = articlesByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, List<Article>>comparingByValue(
                        Comparator.comparingInt(List::size)).reversed())
                .limit(5)
                .map(entry -> {
                    String name = entry.getKey();
                    int count = entry.getValue().size();
                    String sentiment = computeSentiment(entry.getValue());
                    String summary = count + " Artikel" + (count == 1 ? "" : "") + " in dieser Kategorie.";
                    return new AiSummaryDto.AiCategory(name, summary, count, sentiment);
                })
                .collect(Collectors.toList());

        // Wenn keine Kategorien mit Artikeln, "Allgemein" als Fallback
        if (categories.isEmpty() && !articles.isEmpty()) {
            categories = List.of(new AiSummaryDto.AiCategory(
                    "Allgemein",
                    articles.size() + " Artikel vorhanden.",
                    articles.size(),
                    "neutral"
            ));
        }

        // Extrahiere Top-Themen (verbessert: 2-3 Wort-Phrasen)
        List<AiSummaryDto.AiTopic> topics = extractTopicsFromTitles(articles);

        String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new AiSummaryDto(categories, topics, generatedAt);
    }

    /**
     * Simple Sentiment-Analyse ueber Wort-Listen.
     */
    private String computeSentiment(List<Article> articles) {
        Set<String> positive = Set.of("gut", "erfolg", "gewinn", "wachstum", "stark", "positiv", "boom",
                "rekord", "plus", "steigt", "rallye", "bullish", "optimismus", "fortschritt",
                "success", "growth", "strong", "gain", "surge", "rally", "boost", "breakthrough");
        Set<String> negative = Set.of("schlecht", "verlust", "krise", "crash", "schwach", "negativ",
                "minus", "faellt", "panik", "bearish", "rezession", "problem", "warnung", "gefahr",
                "loss", "crisis", "weak", "decline", "fall", "recession", "fear");

        int pos = 0, neg = 0;
        for (Article a : articles) {
            if (a.getTitle() == null) continue;
            String[] words = a.getTitle().toLowerCase().split("\\s+");
            for (String w : words) {
                w = w.replaceAll("[^a-zaeoeueß]", "");
                if (positive.contains(w)) pos++;
                if (negative.contains(w)) neg++;
            }
        }
        if (pos > neg) return "positive";
        if (neg > pos) return "negative";
        return "neutral";
    }

    /**
     * Extrahiert Topics aus Artikel-Titeln (einfache Word-Frequency-Heuristik).
     */
    private List<AiSummaryDto.AiTopic> extractTopicsFromTitles(List<Article> articles) {
        Map<String, Integer> wordFreq = new HashMap<>();
        Set<String> stopWords = new HashSet<>(Set.of(
            "der", "die", "das", "ein", "eine", "und", "oder", "mit", "fuer",
            "von", "in", "zu", "auf", "ist", "sind", "war", "wurde", "wird", "nicht",
            "bei", "nach", "wie", "als", "um", "ueber", "aus", "an", "durch", "vor", "zum", "zur",
            "the", "a", "to", "of", "and", "for", "on", "is", "are", "new", "more",
            "this", "that", "it", "with", "at", "from", "by"
        ));

        for (Article a : articles) {
            if (a.getTitle() == null) continue;
            String[] words = a.getTitle().toLowerCase()
                    .replaceAll("[^a-zaeoeueß0-9\\s]", " ")
                    .split("\\s+");
            for (String word : words) {
                if (word.length() < 3 || stopWords.contains(word)) continue;
                wordFreq.merge(word, 1, Integer::sum);
            }
        }

        return wordFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(e -> new AiSummaryDto.AiTopic(
                        capitalize(e.getKey()),
                        e.getValue(),
                        e.getValue() >= 3
                ))
                .collect(Collectors.toList());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private AiSummaryDto emptySummary() {
        return new AiSummaryDto(
            List.of(new AiSummaryDto.AiCategory("Allgemein", "Keine neuen Artikel vorhanden.", 0, "neutral")),
            List.of(),
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
