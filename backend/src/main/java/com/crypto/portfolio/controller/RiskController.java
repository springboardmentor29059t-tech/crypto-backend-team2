package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.MarketOverviewDto;
import com.crypto.portfolio.model.Holding;
import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.HoldingRepository;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/risk")
public class RiskController {

    @Autowired
    private MarketService marketService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.crypto.portfolio.repository.AssetRepository assetRepository;

    @GetMapping("/global")
    public ResponseEntity<?> getGlobalRisk() {
        MarketOverviewDto overview = marketService.getMarketOverview();
        List<Map<String, Object>> topCoins = marketService.getTopCoins(1, 20); // Top 20 for analysis

        double totalMarketCap = overview.getTotalMarketCap();
        if (totalMarketCap == 0)
            totalMarketCap = 1; // Avoid div by zero

        // Calculate Global Metrics similar to Personal Node
        double hhi = 0;
        double maxAllocation = 0;
        String concentratedAsset = "BTC"; // Default
        List<Map<String, Object>> distribution = new java.util.ArrayList<>();
        double stablecoinCap = 0;

        for (Map<String, Object> coin : topCoins) {
            String symbol = ((String) coin.get("symbol")).toUpperCase();
            double cap = ((Number) coin.get("market_cap")).doubleValue();

            double share = (cap / totalMarketCap) * 100;
            hhi += (share * share);

            if (share > maxAllocation) {
                maxAllocation = share;
                concentratedAsset = symbol;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("symbol", symbol);
            item.put("percentage", share);
            item.put("value", cap); // Passing market cap as value
            distribution.add(item);

            if (List.of("USDT", "USDC", "DAI", "FDUSD", "TUSD").contains(symbol)) {
                stablecoinCap += cap;
            }
        }

        // Normalize HHI (Global market is naturally concentrated by BTC/ETH)
        // BTC ~50% -> HHI 2500 -> Score ~25.
        // We invert for "Risk": High Concentration = High Risk?
        // Actually, Global Market "Risk" is usually Volatility.
        // But user wants "details as per personal node", so we show Concentration.

        double concentrationScore = Math.min(hhi / 100, 100);

        // Market Volatility Risk (Original Logic)
        double marketChange = overview.getMarketCapChangePercentage24h();
        String riskLevel = "LOW";
        double riskScore = 20;

        if (Math.abs(marketChange) > 5) {
            riskLevel = "HIGH";
            riskScore = 80;
        } else if (Math.abs(marketChange) > 2) {
            riskLevel = "MEDIUM";
            riskScore = 50;
        }

        double stablecoinRatio = (stablecoinCap / totalMarketCap) * 100;

        Map<String, Object> response = new HashMap<>();
        response.put("riskLevel", riskLevel);
        response.put("riskScore", riskScore); // Market Volatility Score
        response.put("marketTrend", marketChange >= 0 ? "BULLISH" : "BEARISH");

        // Personal Node Parity Fields
        response.put("diversityScore", 100 - concentrationScore); // Market Diversity
        response.put("maxAllocationAsset", concentratedAsset);
        response.put("maxAllocationPct", maxAllocation);
        response.put("portfolioDistribution", distribution);
        response.put("stablecoinRatio", stablecoinRatio);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/personal")
    public ResponseEntity<?> getPersonalRisk(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<com.crypto.portfolio.model.Asset> assets = assetRepository.findByUserId(user.getId());

        double totalValue = 0;
        Map<String, Double> values = new HashMap<>();

        for (com.crypto.portfolio.model.Asset asset : assets) {
            double price = marketService.getPriceBySymbol(asset.getSymbol());
            // Fallback to average buy price if market price is 0
            if (price <= 0) {
                price = asset.getAvgBuyPrice().doubleValue();
            }
            double val = asset.getAmount().doubleValue() * price;
            values.put(asset.getSymbol(), val);
            totalValue += val;
        }

        // Calculate Concentration Score (HHI)
        double hhi = 0;
        double maxAllocation = 0;
        String concentratedAsset = "None";
        List<Map<String, Object>> distribution = new java.util.ArrayList<>();
        double stablecoinValue = 0;

        if (totalValue > 0) {
            for (Map.Entry<String, Double> entry : values.entrySet()) {
                double share = (entry.getValue() / totalValue) * 100;
                hhi += (share * share);

                if (share > maxAllocation) {
                    maxAllocation = share;
                    concentratedAsset = entry.getKey();
                }

                // Distribution Item
                Map<String, Object> item = new HashMap<>();
                item.put("symbol", entry.getKey());
                item.put("value", entry.getValue());
                item.put("percentage", share);
                distribution.add(item);

                // Stablecoin Check
                if (List.of("USDT", "USDC", "DAI", "BUSD").contains(entry.getKey().toUpperCase())) {
                    stablecoinValue += entry.getValue();
                }
            }
        }

        // Sort distribution by % desc
        distribution.sort((a, b) -> Double.compare((double) b.get("percentage"), (double) a.get("percentage")));

        // Normalize HHI to 0-100 score (10000 is max HHI)
        double concentrationScore = Math.min(hhi / 100, 100);

        String riskLevel = "LOW";
        if (concentrationScore > 60 || maxAllocation > 50)
            riskLevel = "HIGH";
        else if (concentrationScore > 30 || maxAllocation > 25)
            riskLevel = "MEDIUM";

        double stablecoinRatio = totalValue > 0 ? (stablecoinValue / totalValue) * 100 : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("riskLevel", riskLevel);
        response.put("riskScore", concentrationScore);
        response.put("diversityScore", 100 - concentrationScore);
        response.put("maxAllocationAsset", concentratedAsset);
        response.put("maxAllocationPct", maxAllocation);
        response.put("totalAssets", assets.size());
        response.put("portfolioDistribution", distribution);
        response.put("stablecoinRatio", stablecoinRatio);

        return ResponseEntity.ok(response);
    }
}
