package com.crypto.tracker.controller;

import com.crypto.tracker.service.CryptoPriceService;
import com.crypto.tracker.service.CryptoPriceService.MarketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/pricing")
@CrossOrigin(origins = "http://localhost:5173")
public class PricingController {

    @Autowired
    private CryptoPriceService cryptoPriceService;

    public record AssetDto(
        String symbol,
        String name,
        double price,
        double change24h,
        double marketCap,
        double volume24h
    ) {}

    public record ChartPoint(String date, double price) {}

    // 1. Get All Assets (Live Data)
    @GetMapping("/assets")
    public ResponseEntity<List<AssetDto>> getAssets() {
        Set<String> symbols = cryptoPriceService.getSupportedSymbols();
        Map<String, MarketData> dataMap = cryptoPriceService.getBatchMarketData(symbols);
        
        List<AssetDto> assets = new ArrayList<>();
        
        for (String sym : symbols) {
            MarketData md = dataMap.get(sym);
            // Default to 0 if data isn't ready yet
            double price = md != null ? md.price : 0.0;
            double change = md != null ? md.change24h : 0.0;
            double cap = md != null ? md.marketCap : 0.0;
            double vol = md != null ? md.volume24h : 0.0;

            if (price > 0) { 
                assets.add(new AssetDto(
                    sym,
                    cryptoPriceService.getNameForSymbol(sym),
                    price,
                    change,
                    cap,
                    vol
                ));
            }
        }
        
        // Sort by Market Cap (descending)
        assets.sort((a, b) -> Double.compare(b.marketCap(), a.marketCap()));
        
        return ResponseEntity.ok(assets);
    }

    // 2. Get Historical Chart Data
    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<ChartPoint>> getHistory(
            @PathVariable String symbol, 
            @RequestParam(defaultValue = "30") int days) {
        
        List<double[]> history = cryptoPriceService.getHistoricalPrices(symbol, days);
        List<ChartPoint> chartPoints = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (double[] point : history) {
            long timestamp = (long) point[0];
            double price = point[1];
            
            String dateStr = java.time.Instant.ofEpochMilli(timestamp)
                            .atZone(ZoneId.systemDefault())
                            .format(formatter);
            
            chartPoints.add(new ChartPoint(dateStr, price));
        }

        return ResponseEntity.ok(chartPoints);
    }
}