package com.crypto.tracker.service;

import com.crypto.tracker.model.Holding;
import com.crypto.tracker.model.Trade;
import com.crypto.tracker.repository.HoldingRepository;
import com.crypto.tracker.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private CryptoPriceService cryptoPriceService;

    // 1. Portfolio Summary Calculation
    public Map<String, Object> getPnLSummary(Long userId) {
        List<Trade> trades = tradeRepository.findByUserIdOrderByExecutedAtDesc(userId);
        
        double totalInvested = 0.0;
        double totalSold = 0.0;
        double totalFees = 0.0;

        for (Trade t : trades) {
            double amount = t.getQuantity() * t.getPrice();
            if ("BUY".equalsIgnoreCase(t.getSide())) {
                totalInvested += amount;
            } else if ("SELL".equalsIgnoreCase(t.getSide())) {
                totalSold += amount;
            }
            if (t.getFee() != null) totalFees += t.getFee();
        }

        // Calculate Current Value from Holdings
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        double currentValue = 0.0;
        
        for (Holding h : holdings) {
            double price = cryptoPriceService.getCurrentPrice(h.getAssetSymbol());
            currentValue += (h.getQuantity() * price);
        }

        double netCost = totalInvested - totalSold;
        double totalPnL = currentValue - netCost;
        double pnlPercent = (netCost == 0) ? 0 : (totalPnL / netCost) * 100;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalInvested", totalInvested);
        summary.put("totalSold", totalSold);
        summary.put("totalFees", totalFees);
        summary.put("currentValue", currentValue);
        summary.put("netCost", netCost);
        summary.put("totalPnL", totalPnL);
        summary.put("pnlPercent", pnlPercent);

        return summary;
    }

    // 2. Per-Asset P&L Calculation
    public List<Map<String, Object>> getAssetPnL(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        List<Trade> trades = tradeRepository.findByUserIdOrderByExecutedAtDesc(userId);

        Map<String, List<Trade>> tradesBySymbol = trades.stream()
                .collect(Collectors.groupingBy(Trade::getAssetSymbol));

        for (String symbol : tradesBySymbol.keySet()) {
            double realized = 0.0;
            double costBasis = 0.0;
            double soldVolume = 0.0;
            double buyVolume = 0.0;

            for (Trade t : tradesBySymbol.get(symbol)) {
                if ("SELL".equalsIgnoreCase(t.getSide())) {
                    realized += (t.getQuantity() * t.getPrice());
                    soldVolume += t.getQuantity();
                } else {
                    costBasis += (t.getQuantity() * t.getPrice());
                    buyVolume += t.getQuantity();
                }
            }
            
            double avgBuyPrice = (buyVolume > 0) ? costBasis / buyVolume : 0;
            double realizedPnL = realized - (soldVolume * avgBuyPrice);

            double unrealizedPnL = 0.0;
            Holding h = holdings.stream()
                    .filter(hold -> hold.getAssetSymbol().equals(symbol))
                    .findFirst()
                    .orElse(null);
            
            if (h != null) {
                double currentPrice = cryptoPriceService.getCurrentPrice(symbol);
                double currentValue = h.getQuantity() * currentPrice;
                // Cost of remaining coins = Quantity * Avg Buy Price
                double currentCost = h.getQuantity() * avgBuyPrice; 
                unrealizedPnL = currentValue - currentCost;
            }

            if (realizedPnL != 0 || unrealizedPnL != 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("symbol", symbol);
                data.put("realized", realizedPnL);
                data.put("unrealized", unrealizedPnL);
                data.put("total", realizedPnL + unrealizedPnL);
                result.add(data);
            }
        }
        return result;
    }

    public Map<String, Object> getTaxEstimates(Long userId) {
        List<Trade> trades = tradeRepository.findByUserIdOrderByExecutedAtDesc(userId);
        
        double shortTermGains = 0.0;
        double longTermGains = 0.0;
        double totalFees = 0.0;
        
        
        Map<String, LocalDateTime> firstBuyDates = new HashMap<>();

        // 1. First Pass: Find the earliest BUY date for each asset
        for (Trade t : trades) {
            if ("BUY".equalsIgnoreCase(t.getSide())) {
                firstBuyDates.putIfAbsent(t.getAssetSymbol(), t.getExecutedAt());
    
                if (firstBuyDates.get(t.getAssetSymbol()).isAfter(t.getExecutedAt())) {
                    firstBuyDates.put(t.getAssetSymbol(), t.getExecutedAt());
                }
            }
            if (t.getFee() != null) totalFees += t.getFee();
        }

        // 2. Second Pass: Calculate Gains on SELLS
        for (Trade t : trades) {
            if ("SELL".equalsIgnoreCase(t.getSide())) {
                
                LocalDateTime buyDate = firstBuyDates.get(t.getAssetSymbol());
                boolean isLongTerm = false;
                
                if (buyDate != null) {
                    long daysHeld = java.time.temporal.ChronoUnit.DAYS.between(buyDate, t.getExecutedAt());
                    if (daysHeld > 365) isLongTerm = true;
                }

                double tradeValue = t.getQuantity() * t.getPrice();
                
                double estimatedGain = tradeValue * 0.15; 

                if (isLongTerm) {
                    longTermGains += estimatedGain;
                } else {
                    shortTermGains += estimatedGain;
                }
            }
        }

        Map<String, Object> taxMap = new HashMap<>();
        taxMap.put("shortTerm", shortTermGains);
        taxMap.put("longTerm", longTermGains);
        taxMap.put("fees", totalFees);
        taxMap.put("estimatedTaxable", (shortTermGains + longTermGains) - totalFees);
        
        return taxMap;
    }
}