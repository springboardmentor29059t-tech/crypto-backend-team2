package com.crypto.tracker.service;

import com.crypto.tracker.dto.TradeRequest;
import com.crypto.tracker.model.Holding;
import com.crypto.tracker.model.Trade;
import com.crypto.tracker.repository.HoldingRepository;
import com.crypto.tracker.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private HoldingRepository holdingRepository;
    
    // INJECT RISK SERVICE
    @Autowired
    private RiskAnalysisService riskAnalysisService; 

    public List<Trade> getUserTrades(Long userId) {
        return tradeRepository.findByUserIdOrderByExecutedAtDesc(userId);
    }

    @Transactional
    public Trade addManualTrade(TradeRequest request) {
        Trade trade = new Trade();
        trade.setUserId(request.getUserId());
        trade.setAssetSymbol(request.getSymbol().toUpperCase());
        trade.setQuantity(request.getQuantity());
        trade.setPrice(request.getPrice());
        trade.setSide(request.getType().toUpperCase());
        
        String ex = request.getExchange();
        trade.setExchangeName(ex != null ? ex : "Manual");
        
        trade.setStorageType("Manual");
        trade.setFee(0.0);

        try {
            if (request.getDate() != null) {
                trade.setExecutedAt(LocalDateTime.parse(request.getDate(), DateTimeFormatter.ISO_DATE_TIME));
            } else {
                trade.setExecutedAt(LocalDateTime.now());
            }
        } catch (Exception e) {
            trade.setExecutedAt(LocalDateTime.now()); 
        }

        Trade savedTrade = tradeRepository.save(trade);
        
        // 1. Update Holdings
        updateHoldingFromTrade(savedTrade);
        
        // 2. TRIGGER RISK ANALYSIS IMMEDIATELY
        riskAnalysisService.analyzePortfolio(request.getUserId());

        return savedTrade;
    }

    private void updateHoldingFromTrade(Trade trade) {
        String symbol = trade.getAssetSymbol();
        Long userId = trade.getUserId();

        Optional<Holding> existing = holdingRepository.findByUserIdAndAssetSymbol(userId, symbol);

        Holding holding;
        if (existing.isPresent()) {
            holding = existing.get();
        } else {
            holding = new Holding();
            holding.setUserId(userId);
            holding.setAssetSymbol(symbol);
            holding.setQuantity(0.0);
            holding.setAvgCost(0.0);
            holding.setWalletType("manual");
            holding.setUpdatedAt(LocalDateTime.now());
        }

        if ("BUY".equalsIgnoreCase(trade.getSide())) {
            double totalValue = (holding.getQuantity() * holding.getAvgCost()) + (trade.getQuantity() * trade.getPrice());
            double newQuantity = holding.getQuantity() + trade.getQuantity();
            holding.setQuantity(newQuantity);
            if (newQuantity > 0) {
                holding.setAvgCost(totalValue / newQuantity);
            }
        } else if ("SELL".equalsIgnoreCase(trade.getSide())) {
            double newQuantity = Math.max(0, holding.getQuantity() - trade.getQuantity());
            holding.setQuantity(newQuantity);
        }

        holding.setUpdatedAt(LocalDateTime.now());
        holdingRepository.save(holding);
    }
}