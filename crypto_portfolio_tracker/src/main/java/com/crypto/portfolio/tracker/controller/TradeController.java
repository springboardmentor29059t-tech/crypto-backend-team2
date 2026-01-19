package com.crypto.portfolio.tracker.controller;

import com.crypto.portfolio.tracker.entity.Trade;
import com.crypto.portfolio.tracker.service.TradeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    // Add new trade
    @PostMapping("/add")
    public Trade addTrade(@RequestBody Trade trade) {
        return tradeService.addTrade(trade);
    }

    // Get all trades
    @GetMapping("/all")
    public List<Trade> getAllTrades() {
        return tradeService.getAllTrades();
    }

    // Get trades by username
    @GetMapping("/user/{username}")
    public List<Trade> getTradesByUsername(@PathVariable String username) {
        return tradeService.getByUsername(username);
    }

    // Update a trade
    @PutMapping("/edit/{id}")
    public Trade updateTrade(@PathVariable Long id, @RequestBody Trade trade) {
        return tradeService.updateTrade(id, trade);
    }

    // Delete a trade
    @DeleteMapping("/delete/{id}")
    public void deleteTrade(@PathVariable Long id) {
        tradeService.deleteTrade(id);
    }
}