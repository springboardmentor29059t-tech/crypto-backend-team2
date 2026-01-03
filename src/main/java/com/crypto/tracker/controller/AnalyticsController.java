package com.crypto.tracker.controller;

import com.crypto.tracker.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List; 
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:5173")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/summary/{userId}")
    public Map<String, Object> getSummary(@PathVariable Long userId) {
        return analyticsService.getPnLSummary(userId);
    }

    @GetMapping("/pnl-details/{userId}")
    public List<Map<String, Object>> getPnLDetails(@PathVariable Long userId) {
        return analyticsService.getAssetPnL(userId);
    }

    @GetMapping("/tax-summary/{userId}")
    public Map<String, Object> getTaxSummary(@PathVariable Long userId) {
        return analyticsService.getTaxEstimates(userId);
    }
}