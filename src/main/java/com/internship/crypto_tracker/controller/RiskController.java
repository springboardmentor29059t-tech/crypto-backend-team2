package com.internship.crypto_tracker.controller;

import com.internship.crypto_tracker.model.RiskAlert;
import com.internship.crypto_tracker.model.ScamToken;
import com.internship.crypto_tracker.model.User;
import com.internship.crypto_tracker.repository.RiskAlertRepository;
import com.internship.crypto_tracker.repository.ScamTokenRepository;
import com.internship.crypto_tracker.repository.UserRepository;
import com.internship.crypto_tracker.service.RiskAnalysisService;
import com.internship.crypto_tracker.service.ScamImportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/risk")
public class RiskController { // Extends BaseController to get Auth User

    @Autowired
    private RiskAnalysisService riskAnalysisService;

    @Autowired
    private ScamImportService scamImportService;

    @Autowired
    private RiskAlertRepository riskAlertRepository;

    @Autowired
    private ScamTokenRepository scamTokenRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. DEV TOOL: Manually add a token to the blacklist
    @PostMapping("/blacklist/add")
    public ResponseEntity<?> addToBlacklist(@RequestParam String symbol, @RequestParam String riskLevel) {
        ScamToken token = new ScamToken();
        token.setContractAddress(symbol); // Using symbol as address for MVP simplicity
        token.setChain("ETH");
        token.setRiskLevel(ScamToken.RiskLevel.valueOf(riskLevel));
        token.setSource("Manual Entry");
        token.setLastSeen(LocalDateTime.now());
        
        scamTokenRepository.save(token);
        return ResponseEntity.ok("🚨 Added " + symbol + " to global blacklist.");
    }

    // 2. TRIGGER: Check my portfolio now!
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeMyPortfolio() {
        User user = getCurrentUser();
        riskAnalysisService.analyzePortfolio(user.getId());
        return ResponseEntity.ok("Risk analysis complete. Check alerts.");
    }

    // 3. VIEW: Show me my alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<RiskAlert>> getMyAlerts() {
        User user = getCurrentUser();
        List<RiskAlert> alerts = riskAlertRepository.findByUserId(user.getId());
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/blacklist/sync")
    public ResponseEntity<?> syncBlacklistFromWeb() {
        String result = scamImportService.syncScamList();
        return ResponseEntity.ok(result);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}