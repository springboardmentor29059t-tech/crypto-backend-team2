package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.MarketOverviewDto;
import com.crypto.portfolio.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    @Autowired
    private MarketService marketService;

    @GetMapping("/overview")
    public ResponseEntity<MarketOverviewDto> getMarketOverview() {
        return ResponseEntity.ok(marketService.getMarketOverview());
    }

    @GetMapping("/top-coins")
    public ResponseEntity<?> getTopCoins(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "50") int perPage) {
        return ResponseEntity.ok(marketService.getTopCoins(page, perPage));
    }

    @GetMapping("/volatility")
    public ResponseEntity<java.util.Map<String, Object>> getVolatility() {
        com.crypto.portfolio.dto.MarketOverviewDto overview = marketService.getMarketOverview();
        double change = Math.abs(overview.getMarketCapChangePercentage24h());
        String status = change > 5 ? "Highly Volatile" : (change > 2 ? "Moderate" : "Stable");
        return ResponseEntity.ok(java.util.Map.of(
                "marketChange24h", overview.getMarketCapChangePercentage24h(),
                "status", status));
    }

    @GetMapping("/status")
    public ResponseEntity<java.util.Map<String, Object>> getMarketStatus() {
        return ResponseEntity.ok(java.util.Map.of(
                "lastUpdated", marketService.getLastUpdated().toString()));
    }
}
