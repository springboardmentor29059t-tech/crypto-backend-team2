package com.crypto.portfolio.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ScamService {

    private final MarketService marketService;
    private static final Set<String> KNOWN_SCAMS = new HashSet<>(
            Arrays.asList("BITCONNECT", "SAFEMOON", "SQUID", "LUNA-CLASSIC"));

    public ScamService(MarketService marketService) {
        this.marketService = marketService;
    }

    public Map<String, Object> checkToken(String symbolOrName) {
        String input = symbolOrName.toUpperCase();

        // Try to find in market data
        List<Map<String, Object>> topCoins = marketService.getTopCoins(1, 250);
        Map<String, Object> foundCoin = null;

        for (Map<String, Object> coin : topCoins) {
            String coinSymbol = ((String) coin.get("symbol")).toUpperCase();
            String coinName = ((String) coin.get("name")).toUpperCase();
            if (coinSymbol.equals(input) || coinName.contains(input)) {
                foundCoin = coin;
                break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("query", symbolOrName);

        if (foundCoin != null) {
            String symbol = ((String) foundCoin.get("symbol")).toUpperCase();
            result.put("symbol", symbol);
            result.put("name", foundCoin.get("name"));
            result.put("price", foundCoin.get("current_price"));
            result.put("marketCap", foundCoin.get("market_cap"));

            if (KNOWN_SCAMS.contains(symbol)) {
                result.put("scamLevel", "100%");
                result.put("status", "Scam");
                result.put("signal", "Red");
            } else {
                result.put("scamLevel", "0%");
                result.put("status", "Safe");
                result.put("signal", "Green");
            }
        } else {
            // Not found in top 250
            result.put("scamLevel", "100%");
            result.put("status", "Scam / Not Found");
            result.put("signal", "Red");
            result.put("reason", "Token not recognized by major exchanges or flagged as potential risk.");
        }

        return result;
    }
}
