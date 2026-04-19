package com.newsaggregator.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsaggregator.domain.model.Article;
import com.newsaggregator.domain.port.out.ArticleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SummaryService {

    private final ArticleRepository articleRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url:http://host.docker.internal:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:kimi-k2.5:cloud}")
    private String ollamaModel;

    public SummaryService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String generateSummary() {
        // Get articles from last 24 hours
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        List<Article> recentArticles = articleRepository.findByPublishedAtAfter(yesterday);

        if (recentArticles.isEmpty()) {
            return "Keine neuen Artikel in den letzten 24 Stunden.";
        }

        // Build prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("Erstelle eine kurze, informative Zusammenfassung (max. 3 Sätze) der folgenden News-Artikel:\n\n");
        
        recentArticles.stream()
            .limit(10) // Limit to 10 most recent
            .forEach(article -> {
                prompt.append("- ").append(article.getTitle());
                if (article.getDescription() != null && !article.getDescription().isEmpty()) {
                    prompt.append(": ").append(article.getDescription().substring(0, 
                        Math.min(article.getDescription().length(), 100)));
                }
                prompt.append("\n");
            });

        prompt.append("\nZusammenfassung:");

        // Call Ollama
        try {
            String summary = callOllama(prompt.toString());
            return summary != null ? summary : "Zusammenfassung konnte nicht erstellt werden.";
        } catch (Exception e) {
            // Fallback: return simple summary
            return String.format("Heute gibt es %d neue Artikel. Die wichtigsten Themen: %s",
                recentArticles.size(),
                recentArticles.stream()
                    .limit(3)
                    .map(Article::getTitle)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Keine Artikel"));
        }
    }

    private String callOllama(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "model", ollamaModel,
            "prompt", prompt,
            "stream", false,
            "options", Map.of(
                "temperature", 0.7,
                "num_predict", 150
            )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        String url = ollamaBaseUrl + "/api/generate";
        String response = restTemplate.postForObject(url, request, String.class);
        
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("response").asText();
    }
}
