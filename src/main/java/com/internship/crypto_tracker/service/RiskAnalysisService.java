package com.internship.crypto_tracker.service;

import com.internship.crypto_tracker.model.Holding;
import com.internship.crypto_tracker.model.RiskAlert;
import com.internship.crypto_tracker.model.ScamToken;
import com.internship.crypto_tracker.model.User;
import com.internship.crypto_tracker.repository.HoldingRepository;
import com.internship.crypto_tracker.repository.RiskAlertRepository;
import com.internship.crypto_tracker.repository.ScamTokenRepository;
import com.internship.crypto_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RiskAnalysisService {

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private ScamTokenRepository scamTokenRepository;

    @Autowired
    private RiskAlertRepository riskAlertRepository;

    @Autowired
    private UserRepository userRepository;

   
    @Scheduled(cron = "0 0 * * * *") 
    public void scheduleRiskAnalysis() {
        System.out.println("🛡️ Security Scan Started...");
        analyzePortfolio(1L); 
    }

    public void analyzePortfolio(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 1. Get all assets the user owns
        List<Holding> userHoldings = holdingRepository.findByUserId(userId);

        for (Holding holding : userHoldings) {
            String assetSymbol = holding.getAssetSymbol();

            // 2. Check if this asset is in our Blacklist
            // (For MVP, we match by Symbol. In production, we'd use Contract Address)
            Optional<ScamToken> scamMatch = scamTokenRepository.findByContractAddress(assetSymbol);

            if (scamMatch.isPresent()) {
                ScamToken scam = scamMatch.get();
                
                createRiskAlert(user, assetSymbol, scam);
            }
        }
    }

    private void createRiskAlert(User user, String symbol, ScamToken scam) {
        // Avoid duplicate alerts (Optional: check if alert already exists today)
        
        RiskAlert alert = new RiskAlert();
        alert.setUser(user);
        alert.setAssetSymbol(symbol);
        alert.setAlertType(RiskAlert.AlertType.RUGPULL_WARNING);
        alert.setDetails("DANGER: " + symbol + " is flagged as " + scam.getRiskLevel() + " risk. Reason: " + scam.getSource());
        alert.setCreatedAt(LocalDateTime.now());

        riskAlertRepository.save(alert);
        System.out.println("⚠️ RISK DETECTED: Created alert for " + symbol);
    }
}