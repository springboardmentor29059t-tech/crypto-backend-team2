package com.crypto.tracker.service;

import com.crypto.tracker.model.Holding;
import com.crypto.tracker.model.RiskAlert;
import com.crypto.tracker.model.ScamToken;
import com.crypto.tracker.repository.HoldingRepository;
import com.crypto.tracker.repository.RiskAlertRepository;
import com.crypto.tracker.repository.ScamTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RiskAnalysisService {

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private RiskAlertRepository riskAlertRepository;

    @Autowired
    private ScamTokenRepository scamTokenRepository;

    public List<RiskAlert> getUserAlerts(Long userId) {
        return riskAlertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<ScamToken> getScamDatabase() {
        return scamTokenRepository.findAll();
    }

    public void analyzePortfolio(Long userId) {
        System.out.println("Starting Risk Analysis for User: " + userId);
        List<Holding> holdings = holdingRepository.findByUserId(userId);

        for (Holding holding : holdings) {
            String symbol = holding.getAssetSymbol();
            
            // 1. Check for SCAM Token
            if ("SCAM".equalsIgnoreCase(symbol) || "PEPE".equalsIgnoreCase(symbol)) {
                createAlert(userId, symbol, "high", "rugpull_warning", 
                    "This token has been flagged as high risk.");
            }

            // 2. Check for ETH Price Drop
            if ("ETH".equalsIgnoreCase(symbol) && holding.getAvgCost() > 3500) {
                createAlert(userId, symbol, "medium", "price_threshold", 
                    "ETH is trading significantly below your average buy price.");
            }
        }
    }

    private void createAlert(Long userId, String symbol, String severity, String type, String details) {
        RiskAlert alert = new RiskAlert();
        alert.setUserId(userId);
        alert.setAssetSymbol(symbol);
        alert.setSeverity(severity);
        alert.setAlertType(type);
        alert.setDetails(details);
        alert.setCreatedAt(LocalDateTime.now());
        riskAlertRepository.save(alert);
    }
}