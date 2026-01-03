package com.crypto.tracker.service;

import com.crypto.tracker.model.Holding;
import com.crypto.tracker.repository.HoldingRepository;
import com.crypto.tracker.service.CryptoPriceService.MarketData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DashboardService {

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private CryptoPriceService cryptoPriceService;

    private final RestTemplate restTemplate = new RestTemplate();
    
    
    private final Map<String, CachedChartData> chartCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 60 * 60 * 1000; 

    private static class CachedChartData {
        List<Map<String, Object>> data;
        long timestamp;

        CachedChartData(List<Map<String, Object>> data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }

    // --- 1. Portfolio Summary (Totals) ---
    public Map<String, Object> getPortfolioSummary(Long userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        
        // Group aggregate quantities
        Map<String, Double> assetQuantities = new HashMap<>();
        for (Holding h : holdings) {
            if (h.getQuantity() > 0) {
                assetQuantities.put(h.getSymbol().toUpperCase(), h.getQuantity());
            }
        }

        // Batch fetch current prices
        List<String> symbolsToFetch = new ArrayList<>(assetQuantities.keySet());
        Map<String, MarketData> marketDataMap = cryptoPriceService.getBatchMarketData(new HashSet<>(symbolsToFetch));

        double totalBalance = 0.0;
        double totalChange24h = 0.0;

        for (Map.Entry<String, Double> entry : assetQuantities.entrySet()) {
            String symbol = entry.getKey();
            double quantity = entry.getValue();
            
            MarketData data = marketDataMap.getOrDefault(symbol, new MarketData(0,0,0,0));
            
            double value = quantity * data.price;
            totalBalance += value;

            if (data.change24h != 0) {
                double startValue = value / (1 + (data.change24h / 100.0));
                totalChange24h += (value - startValue);
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBalance", totalBalance);
        summary.put("totalChange24h", totalChange24h);
        summary.put("totalChangePercent", totalBalance > 0 ? (totalChange24h / totalBalance) * 100 : 0);
        
        return summary;
    }

    // --- 2. Portfolio Value Chart (7 Days, All Assets) ---
    public List<Map<String, Object>> getPortfolioChart(Long userId) {
        List<Holding> holdingsList = holdingRepository.findByUserId(userId);
        if (holdingsList.isEmpty()) return new ArrayList<>();

        // Use TreeMap with ISO Dates to sum values Day-by-Day automatically
        Map<String, Double> dailyTotals = new TreeMap<>(); 

        // Iterate through ALL holdings
        for (Holding h : holdingsList) {
            String symbol = h.getSymbol();
            double qty = h.getQuantity();
            
            // Skip dust (tiny amounts) to save API calls
            if (qty * h.getAvgCost() < 1.0 && qty < 0.1) continue; 

            // Fetch 7-day history for this specific coin
            List<Map<String, Object>> coinHistory = fetchCoinHistory(symbol, 7);
            
            for (Map<String, Object> point : coinHistory) {
                String isoDate = (String) point.get("date"); // e.g., "2025-12-16"
                double price = (Double) point.get("value");
                
                // Formula: Portfolio(T) += Quantity * Price(T)
                dailyTotals.put(isoDate, dailyTotals.getOrDefault(isoDate, 0.0) + (price * qty));
            }
        }

        // Format for Frontend
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM dd"); // "Dec 16"

        for (Map.Entry<String, Double> entry : dailyTotals.entrySet()) {
            Map<String, Object> point = new HashMap<>();
            try {
                LocalDate date = LocalDate.parse(entry.getKey(), isoFormatter);
                point.put("date", date.format(displayFormatter));
            } catch (Exception e) {
                point.put("date", entry.getKey());
            }
            point.put("value", entry.getValue());
            result.add(point);
        }
        return result;
    }

    // Helper to fetch history for N days
    private List<Map<String, Object>> fetchCoinHistory(String symbol, int days) {
        String coinId = cryptoPriceService.getNameForSymbol(symbol).toLowerCase();
        
        // Manual override for common ID mismatches
        if (symbol.equalsIgnoreCase("BTC")) coinId = "bitcoin";
        if (symbol.equalsIgnoreCase("ETH")) coinId = "ethereum";
        if (symbol.equalsIgnoreCase("BNB")) coinId = "binancecoin";
        if (symbol.equalsIgnoreCase("USDT")) coinId = "tether";
        if (symbol.equalsIgnoreCase("USDC")) coinId = "usd-coin";
        
        String cacheKey = symbol + "-" + days;

        if (chartCache.containsKey(cacheKey)) {
            CachedChartData cached = chartCache.get(cacheKey);
            if (System.currentTimeMillis() - cached.timestamp < CACHE_DURATION_MS) {
                return cached.data;
            }
        }

        String url = "https://api.coingecko.com/api/v3/coins/" + coinId + "/market_chart?vs_currency=usd&days=" + days;
        List<Map<String, Object>> history = new ArrayList<>();

        try {
            
            Thread.sleep(400); 
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            JSONArray prices = json.getJSONArray("prices");

            int step = 24; 
            if (prices.length() < 48) step = 1; 

            for (int i = 0; i < prices.length(); i += step) { 
                JSONArray point = prices.getJSONArray(i);
                long timestamp = point.getLong(0);
                double price = point.getDouble(1);

                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", convertToIsoDate(timestamp));
                dataPoint.put("value", price);
                history.add(dataPoint);
            }

            chartCache.put(cacheKey, new CachedChartData(history, System.currentTimeMillis()));

        } catch (Exception e) {
            System.err.println("Chart Fetch Error for " + symbol + ": " + e.getMessage());
            if (chartCache.containsKey(cacheKey)) return chartCache.get(cacheKey).data;
        }

        return history;
    }

    private String convertToIsoDate(long timestamp) {
        LocalDate date = LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}