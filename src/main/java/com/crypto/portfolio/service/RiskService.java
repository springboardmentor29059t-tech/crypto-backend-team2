package com.crypto.portfolio.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RiskService {

    // Simulating a scam/risk database
    private static final Map<String, String> RISK_REGISTRY = new HashMap<>();

    static {
        RISK_REGISTRY.put("SAFEMOON", "HIGH (Potential Liquidity Lock Issue)");
        RISK_REGISTRY.put("BITCONNECT", "HIGH (Confirmed Scam)");
        RISK_REGISTRY.put("SQUID", "HIGH (Rugpull History)");
    }

    public Map<String, Object> analyzeToken(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        String riskLevel = "LOW";
        String reason = "Legit Market Presence";

        if (RISK_REGISTRY.containsKey(upperSymbol)) {
            riskLevel = "HIGH";
            reason = RISK_REGISTRY.get(upperSymbol);
        } else if (upperSymbol.contains("MOVE") || upperSymbol.contains("ELON")) {
            riskLevel = "MEDIUM";
            reason = "High Volatility / Meme Category";
        }

        return Map.of(
                "symbol", upperSymbol,
                "riskLevel", riskLevel,
                "reason", reason);
    }
}
