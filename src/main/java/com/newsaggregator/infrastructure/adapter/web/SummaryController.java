package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.service.SummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/api/summary")
    public Map<String, String> getSummary() {
        String summary = summaryService.generateSummary();
        return Map.of("summary", summary);
    }
}
