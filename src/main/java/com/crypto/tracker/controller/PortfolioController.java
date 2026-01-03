package com.crypto.tracker.controller;

import com.crypto.tracker.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "http://localhost:5173")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getPortfolio(@PathVariable Long userId) {
        
        List<Map<String, Object>> portfolio = portfolioService.getPortfolioHoldings(userId);
        return ResponseEntity.ok(portfolio);
    }
}