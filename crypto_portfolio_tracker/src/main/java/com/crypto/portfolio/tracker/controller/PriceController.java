package com.crypto.portfolio.tracker.controller;

import com.crypto.portfolio.tracker.dto.PriceRequest;
import com.crypto.portfolio.tracker.entity.PriceSnapshot;
import com.crypto.portfolio.tracker.service.PriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    // POST: Save price
    @PostMapping("/save")
    public PriceSnapshot savePrice(@RequestBody PriceRequest request) {
        return priceService.savePrice(request.getCoinName(), request.getSymbol(), request.getPrice());
    }

    // GET: All prices
    @GetMapping("/all")
    public List<PriceSnapshot> getAllPrices() {
        return priceService.getAllPrices();
    }

    // GET: Price history by symbol
    @GetMapping("/history/{symbol}")
    public List<PriceSnapshot> getPriceHistory(@PathVariable String symbol) {
        return priceService.getPriceHistory(symbol);
    }
}