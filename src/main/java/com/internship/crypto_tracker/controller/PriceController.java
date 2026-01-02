package com.internship.crypto_tracker.controller;

import com.internship.crypto_tracker.model.PriceSnapshot;
import com.internship.crypto_tracker.service.CoinGeckoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    @Autowired
    private CoinGeckoService coinGeckoService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshPrices() {
        coinGeckoService.fetchAndSavePrices();
        return ResponseEntity.ok("Latest prices fetched and saved to database.");
    }

    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<PriceSnapshot>> getCoinHistory(@PathVariable String symbol) {
        List<PriceSnapshot> history = coinGeckoService.getHistoryForCoin(symbol.toUpperCase());
        return ResponseEntity.ok(history);
    }
}