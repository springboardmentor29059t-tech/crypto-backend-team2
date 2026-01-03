package com.crypto.tracker.controller;

import com.crypto.tracker.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // 1. Get Portfolio Summary (Total Balance, 24h Change)
    @GetMapping("/{userId}")
    public Map<String, Object> getDashboard(@PathVariable Long userId) {
        // FIX: Method name matches DashboardService.java
        return dashboardService.getPortfolioSummary(userId);
    }

    // 2. Get Chart Data (History)
    @GetMapping("/chart/{userId}")
    public List<Map<String, Object>> getPortfolioChart(@PathVariable Long userId) {
        return dashboardService.getPortfolioChart(userId);
    }
}