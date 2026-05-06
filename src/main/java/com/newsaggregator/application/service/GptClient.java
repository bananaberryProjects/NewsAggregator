package com.newsaggregator.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generischer Ollama-Client für KI-Aufrufe.
 * Wiederverwendbar für verschiedene Services (AiSummary, MarketInsight, Trending).
 */
@Component
public class GptClient {

    private static final Logger log = LoggerFactory.getLogger(GptClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url:http://host.docker.internal:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:gpt-oss:120b-cloud}")
    private String ollamaModel;

    public GptClient() {
        this.restTemplate = createRestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(120000); // 2 Min für Term-Filterung reichen
        return new RestTemplate(factory);
    }

    /**
     * Filtert eine Liste von Trending-Termen via Ollama.
     * Entfernt Noise-Wörter, gruppiert Synonyme und liefert die Top relevaten Begriffe.
     */
    public List<String> filterTrendingTerms(List<String> terms) throws Exception {
        if (terms == null || terms.isEmpty()) {
            return List.of();
        }

        // Health check
        try {
            restTemplate.getForEntity(ollamaBaseUrl + "/api/tags", String.class);
        } catch (Exception ex) {
            throw new RuntimeException("Ollama not reachable at " + ollamaBaseUrl);
        }

        String termsString = terms.stream()
                .limit(30)
                .collect(Collectors.joining(", "));

        String prompt = """
            Du bist ein Nachrichten-Analyst. Analysiere die folgenden Begriffe, die häufig in Artikeltiteln vorkommen.

            Aufgabe:
            1. Entferne reine Noise-Wörter (z.B. "Update", "Neu", "Heute", "Bericht", "Analyse", allgemeine Zeitungsfloskeln).
            2. Gruppiere offensichtliche Synonyme (z.B. "Bitcoin" + "BTC" = "Bitcoin"; "KI" + "Künstliche Intelligenz" = "KI").
            3. Liefere nur die 10 relevantesten Themen-Begriffe zurück.
            4. Bevorzuge konkrete Eigennamen, Firmen, Technologien, politische Ereignisse.

            Begriffe: %s

            Antworte AUSSCHLIESSLICH mit gültigem JSON (kein Markdown, keine Erklärungen):
            {
              "filteredTerms": ["Begriff1", "Begriff2", ...]
            }
            """.formatted(termsString);

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.3);
        options.put("num_predict", 512);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("format", "json");
        requestBody.put("options", options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = ollamaBaseUrl + "/api/generate";
        String response = restTemplate.postForObject(url, request, String.class);

        JsonNode jsonNode = objectMapper.readTree(response);
        String rawResponse = jsonNode.get("response").asText();

        // Clean markdown wrappers
        rawResponse = rawResponse.trim();
        if (rawResponse.startsWith("```json")) rawResponse = rawResponse.substring(7);
        if (rawResponse.startsWith("```")) rawResponse = rawResponse.substring(3);
        if (rawResponse.endsWith("```")) rawResponse = rawResponse.substring(0, rawResponse.length() - 3);
        rawResponse = rawResponse.trim();

        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode filtered = root.path("filteredTerms");

        List<String> result = new ArrayList<>();
        if (filtered.isArray()) {
            for (JsonNode node : filtered) {
                String term = node.asText().trim();
                if (!term.isEmpty()) {
                    result.add(term);
                }
            }
        }

        log.debug("KI filtered {} terms to {}", terms.size(), result.size());
        return result.isEmpty() ? terms : result;
    }

    /**
     * Generische Methode für strukturierte JSON-Antworten.
     */
    public String generateStructuredJson(String systemPrompt, String userPrompt) throws Exception {
        try {
            restTemplate.getForEntity(ollamaBaseUrl + "/api/tags", String.class);
        } catch (Exception ex) {
            throw new RuntimeException("Ollama not reachable at " + ollamaBaseUrl);
        }

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.5);
        options.put("num_predict", 4096);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("system", systemPrompt);
        requestBody.put("prompt", userPrompt);
        requestBody.put("stream", false);
        requestBody.put("format", "json");
        requestBody.put("options", options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String response = restTemplate.postForObject(ollamaBaseUrl + "/api/generate", request, String.class);
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("response").asText();
    }
}
