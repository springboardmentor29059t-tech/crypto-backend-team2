package com.internship.crypto_tracker.service;

import com.internship.crypto_tracker.model.PriceSnapshot;
import com.internship.crypto_tracker.repository.PriceSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CoinGeckoService {

    // CoinGecko Free API Endpoint
    private static final String COINGECKO_API_URL = "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd&include_market_cap=true";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;

    @Scheduled(cron = "0 */5 * * * *")
    public void fetchAndSavePrices() {
        // 1. Define the coins we want to track (CoinGecko IDs)
        // Note: CoinGecko uses IDs like 'bitcoin', not symbols like 'BTC'
        List<String> coinIds = List.of("bitcoin", "ethereum", "binancecoin", "solana", "cardano");
        
        String idsParam = String.join(",", coinIds);
        String finalUrl = String.format(COINGECKO_API_URL, idsParam);

        try {
            // 2. Call the API
            // Response format: { "bitcoin": { "usd": 50000.00, "usd_market_cap": 900000000 } }
            ResponseEntity<Map<String, Map<String, Object>>> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {}
            );

            Map<String, Map<String, Object>> body = response.getBody();
            if (body == null) return;

            // 3. Loop through results and save Snapshots
            for (String coinId : coinIds) {
                if (body.containsKey(coinId)) {
                    Map<String, Object> data = body.get(coinId);
                    
                    // Convert raw numbers to BigDecimal safely
                    BigDecimal price = new BigDecimal(data.get("usd").toString());
                    BigDecimal marketCap = new BigDecimal(data.get("usd_market_cap").toString());

                    // Create & Save Snapshot
                    PriceSnapshot snapshot = new PriceSnapshot();
                    snapshot.setAssetSymbol(convertIdToSymbol(coinId)); // Helper method
                    snapshot.setPriceUsd(price);
                    snapshot.setMarketCap(marketCap);
                    snapshot.setSource("CoinGecko");
                    snapshot.setCapturedAt(LocalDateTime.now());

                    priceSnapshotRepository.save(snapshot);
                    System.out.println("✅ Saved price for " + coinId + ": $" + price);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to fetch prices: " + e.getMessage());
        }
    }

    public List<PriceSnapshot> getHistoryForCoin(String symbol) {
        
        return priceSnapshotRepository.findByAssetSymbolOrderByCapturedAtAsc(symbol);
    }

    private String convertIdToSymbol(String id) {
        switch (id) {
            case "bitcoin": return "BTC";
            case "ethereum": return "ETH";
            case "binancecoin": return "BNB";
            case "solana": return "SOL";
            case "cardano": return "ADA";
            default: return id.toUpperCase();
        }
    }
}