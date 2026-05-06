package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.TrendingTopicDto;
import com.newsaggregator.application.service.TrendingTopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller für Trending-Themen.
 *
 * Endpoints:
 *   GET /api/trending?hours=24&limit=20   → Hybrid KI + Statistik
 *   GET /api/trending/fast?hours=24&limit=20 → Statistik nur (schneller)
 */
@RestController
@RequestMapping("/api/trending")
public class TrendingTopicController {

    private final TrendingTopicService trendingTopicService;

    public TrendingTopicController(TrendingTopicService trendingTopicService) {
        this.trendingTopicService = trendingTopicService;
    }

    /**
     * Liefert Trending-Themen mit Hybrid KI-Filterung.
     *
     * @param hours Zeitraum in Stunden (default: 24)
     * @param limit Maximale Anzahl Topics (default: 20)
     */
    @GetMapping
    public ResponseEntity<TrendingTopicDto> getTrendingTopics(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "20") int limit) {
        TrendingTopicDto result = trendingTopicService.getTrendingTopics(hours, limit);
        return ResponseEntity.ok(result);
    }

    /**
     * Schneller Endpoint ohne KI-Filterung (nur Statistik).
     * Nützlich für häufige Polls oder wenn Ollama nicht erreichbar ist.
     */
    @GetMapping("/fast")
    public ResponseEntity<TrendingTopicDto> getTrendingTopicsFast(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "20") int limit) {
        TrendingTopicDto result = trendingTopicService.getTrendingTopicsFast(hours, limit);
        return ResponseEntity.ok(result);
    }
}
