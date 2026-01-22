package com.crypto.portfolio.scheduler;

import com.crypto.portfolio.model.PriceSnapshot;
import com.crypto.portfolio.repository.PriceSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PriceUpdateScheduler {
    
    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;
    
    // Simulated price data for demo purposes
    private static final Map<String, Double> CRYPTO_PRICES = new HashMap<>();
    
    static {
        CRYPTO_PRICES.put("BTC", 45000.0);
        CRYPTO_PRICES.put("ETH", 2500.0);
        CRYPTO_PRICES.put("SOL", 100.0);
        CRYPTO_PRICES.put("ADA", 0.5);
        CRYPTO_PRICES.put("DOT", 7.0);
    }
    
    @Scheduled(fixedRate = 300000) // Update every 5 minutes
    public void updatePrices() {
        System.out.println("Updating cryptocurrency prices...");
        
        for (Map.Entry<String, Double> entry : CRYPTO_PRICES.entrySet()) {
            String symbol = entry.getKey();
            String name = getCryptoName(symbol);
            double currentPrice = entry.getValue();
            
            // Add some random fluctuation for demo purposes
            double fluctuation = (Math.random() - 0.5) * 0.05; // ±2.5% fluctuation
            double newPrice = currentPrice * (1 + fluctuation);
            
            // Update the price in our map
            CRYPTO_PRICES.put(symbol, newPrice);
            
            // Save the price snapshot to the database
            PriceSnapshot snapshot = new PriceSnapshot(symbol, name, newPrice);
            priceSnapshotRepository.save(snapshot);
            
            System.out.println("Updated " + symbol + " price to " + newPrice);
        }
    }
    
    private String getCryptoName(String symbol) {
        switch (symbol) {
            case "BTC": return "Bitcoin";
            case "ETH": return "Ethereum";
            case "SOL": return "Solana";
            case "ADA": return "Cardano";
            case "DOT": return "Polkadot";
            default: return symbol;
        }
    }
}