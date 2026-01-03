package com.crypto.tracker.controller;

import com.crypto.tracker.model.RiskAlert;
import com.crypto.tracker.model.ScamToken;
import com.crypto.tracker.service.RiskAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "http://localhost:5173")
public class RiskController {

    @Autowired
    private RiskAnalysisService riskService;

    @GetMapping("/alerts/{userId}")
    public List<RiskAlert> getUserAlerts(@PathVariable Long userId) {
        return riskService.getUserAlerts(userId);
    }

    @GetMapping("/scam-tokens")
    public List<ScamToken> getScamTokens() {
        return riskService.getScamDatabase();
    }

    @PostMapping("/scan/{userId}")
    public ResponseEntity<String> forceScan(@PathVariable Long userId) {
        riskService.analyzePortfolio(userId);
        return ResponseEntity.ok("Scan triggered");
    }
}