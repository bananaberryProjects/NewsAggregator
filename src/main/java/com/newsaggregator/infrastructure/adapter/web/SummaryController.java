package com.newsaggregator.infrastructure.adapter.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;
import java.util.Map;

@RestController
public class SummaryController {
    @GetMapping("/summary")
    public Map<String, String> getSummary() {
        // Placeholder summary – in a real implementation this would call an LLM with recent articles
        String summary = "Heute gibt es 5 neue Artikel zu Technologie, Sport und Wirtschaft. Highlights: ...";
        return Collections.singletonMap("summary", summary);
    }
}
