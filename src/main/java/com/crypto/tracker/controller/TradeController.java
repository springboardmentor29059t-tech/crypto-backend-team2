package com.crypto.tracker.controller;

import com.crypto.tracker.dto.TradeRequest;
import com.crypto.tracker.model.Trade;
import com.crypto.tracker.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "http://localhost:5173")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @GetMapping("/{userId}")
    public List<Trade> getUserTrades(@PathVariable Long userId) {
        return tradeService.getUserTrades(userId);
    }

    // This handles POST /api/trades AND /api/trades/add to be safe
    @PostMapping(value = {"", "/add"}) 
    public ResponseEntity<Trade> addTrade(@RequestBody TradeRequest request) {
        Trade trade = tradeService.addManualTrade(request);
        return ResponseEntity.ok(trade);
    }
}