package com.newsaggregator.infrastructure.adapter.web;

import com.newsaggregator.application.dto.DashboardStatsDto;
import com.newsaggregator.application.service.DashboardStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://mac-mini.local:5173"})
public class DashboardStatsController {

    private final DashboardStatsService dashboardStatsService;

    public DashboardStatsController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(dashboardStatsService.getDashboardStats());
    }
}
