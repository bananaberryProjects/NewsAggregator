package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.ReadingStatisticsDto;
import com.newsaggregator.application.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = {"http://localhost:5173", "http://mac-mini.local:5173"})
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<ReadingStatisticsDto> getStatistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
