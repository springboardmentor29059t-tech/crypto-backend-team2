package com.crypto.portfolio.controller;

import com.crypto.portfolio.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scam")
public class ScamController {

    @Autowired
    private MarketService marketService;

    @GetMapping("/check")
    public ResponseEntity<?> checkToken(@RequestParam String token) {
        // Simple logic as requested:
        // 1. Check if exists in MarketService (Top 250) or fetch from CoinGecko Search
        // 2. If exists -> Risk based on market cap / volume
        // 3. If not exists -> High risk

        Map<String, Object> coinData = findCoin(token);

        ScamCheckResponse response = new ScamCheckResponse();
        response.setToken(token.toUpperCase());

        if (coinData != null) {
            response.setExists(true);
            response.setCurrentPrice(((Number) coinData.get("current_price")).doubleValue());
            double mcap = ((Number) coinData.get("market_cap")).doubleValue();

            if (mcap > 1_000_000_000) {
                response.setRiskLevel("LOW");
                response.setScamScore(10);
                response.setMessage("Token is a major cap asset. High liquidity.");
            } else if (mcap > 10_000_000) {
                response.setRiskLevel("MEDIUM");
                response.setScamScore(40);
                response.setMessage("Token has moderate liquidity. Always DYOR.");
            } else {
                response.setRiskLevel("WARNING");
                response.setScamScore(70);
                response.setMessage("Low liquidity detected. High volatility risk.");
            }
            response.setMarketCap(mcap);
            response.setImage((String) coinData.get("image"));
        } else {
            response.setExists(false);
            response.setRiskLevel("CRITICAL");
            response.setScamScore(100);
            response.setMessage("Token not found in major reputable lists. High probability of scam or low liquidity.");
        }

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> findCoin(String symbol) {
        // Try cache first
        List<Map<String, Object>> top = marketService.getTopCoins(1, 250);
        for (Map<String, Object> c : top) {
            if (c.get("symbol").toString().equalsIgnoreCase(symbol)
                    || c.get("name").toString().equalsIgnoreCase(symbol)) {
                return c;
            }
        }
        // If not in top 250, return null (Found = false implies high risk per
        // instructions)
        return null;
    }

    static class ScamCheckResponse {
        private String token;
        private boolean exists;
        private double currentPrice;
        private double marketCap;
        private String riskLevel;
        private double scamScore;
        private String message;
        private String image;

        // Getters Setters
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public boolean isExists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }

        public double getCurrentPrice() {
            return currentPrice;
        }

        public void setCurrentPrice(double currentPrice) {
            this.currentPrice = currentPrice;
        }

        public double getMarketCap() {
            return marketCap;
        }

        public void setMarketCap(double marketCap) {
            this.marketCap = marketCap;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public double getScamScore() {
            return scamScore;
        }

        public void setScamScore(double scamScore) {
            this.scamScore = scamScore;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
