package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.GlobalMarketResponse;
import com.crypto.portfolio.service.GlobalMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
@CrossOrigin(origins = "http://localhost:3000")
public class GlobalMarketController {

    @Autowired
    private GlobalMarketService marketService;

    @GetMapping("/global")
    public ResponseEntity<GlobalMarketResponse> getGlobalMarketData() {
        GlobalMarketResponse marketData = marketService.getGlobalMarketData();
        return ResponseEntity.ok(marketData);
    }
}
