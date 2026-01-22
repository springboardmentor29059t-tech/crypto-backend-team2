package com.crypto.portfolio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/market/sentiment")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class SentimentController {

    @Autowired
    private com.crypto.portfolio.service.MarketService marketService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSentiment() {
        // Simple logic for sentiment
        // Fetch global market data from cache via service if possible, or just raw calc
        // Since getTopCoins is public, we can use it to trend

        // This is a simplified "dummy" logic based on available data,
        // ideally we'd have a specific endpoint in MarketService for global metrics.
        // Assuming we can get some market-wide change or BTC change as proxy.
        // Let's use getTopCoins(1, 1) to get BTC and use its change as major indicator

        double change = 0.0;
        try {
            var coins = marketService.getTopCoins(1, 1);
            if (coins != null && !coins.isEmpty()) {
                Object chg = coins.get(0).get("price_change_percentage_24h");
                if (chg instanceof Number) {
                    change = ((Number) chg).doubleValue();
                }
            }
        } catch (Exception e) {
            // ignore
        }

        String sentiment = "Neutral";
        String confidence = "Medium";
        if (change > 2.0) {
            sentiment = "Bullish";
            confidence = change > 5.0 ? "High" : "Medium";
        } else if (change < -2.0) {
            sentiment = "Bearish";
            confidence = change < -5.0 ? "High" : "Medium";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sentiment", sentiment);
        response.put("marketChange", change);
        response.put("confidence", confidence);
        response.put("lastUpdated", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
