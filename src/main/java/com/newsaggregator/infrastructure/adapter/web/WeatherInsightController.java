package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.WeatherInsightDto;
import com.newsaggregator.application.service.WeatherInsightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller fuer KI-Wetter-Insights.
 */
@RestController
public class WeatherInsightController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherInsightController.class);

    private final WeatherInsightService weatherInsightService;

    public WeatherInsightController(WeatherInsightService weatherInsightService) {
        this.weatherInsightService = weatherInsightService;
    }

    @GetMapping("/api/weather/insight")
    public ResponseEntity<WeatherInsightDto> getWeatherInsight(
            @RequestParam(name = "lat", defaultValue = "52.52") double lat,
            @RequestParam(name = "lon", defaultValue = "13.41") double lon,
            @RequestParam(name = "city", defaultValue = "Berlin") String city) {
        logger.info("Wetter-Insight angefragt: {} ({}, {})", city, lat, lon);
        try {
            WeatherInsightDto insight = weatherInsightService.generateInsight(lat, lon, city);
            return ResponseEntity.ok(insight);
        } catch (Exception e) {
            logger.error("Wetter-Insight fehlgeschlagen: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
