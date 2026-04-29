package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.AiSummaryDto;
import com.newsaggregator.application.service.AiSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller für strukturierte KI-Zusammenfassung v2.
 */
@RestController
public class AiSummaryController {

    private final AiSummaryService aiSummaryService;

    public AiSummaryController(AiSummaryService aiSummaryService) {
        this.aiSummaryService = aiSummaryService;
    }

    @GetMapping("/api/summary/v2")
    public AiSummaryDto getStructuredSummary() {
        return aiSummaryService.generateStructuredSummary();
    }
}
