package com.crypto.tracker.service;

import com.crypto.tracker.model.Holding;
import com.crypto.tracker.model.Trade;
import com.crypto.tracker.repository.ExchangeRepository; 
import com.crypto.tracker.repository.HoldingRepository;
import com.crypto.tracker.repository.TradeRepository;
import com.crypto.tracker.service.CryptoPriceService.MarketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PortfolioService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private CryptoPriceService cryptoPriceService;

    // --- Trade Logic ---
    public Trade addTrade(Trade trade) {
        if (trade.getExecutedAt() == null) trade.setExecutedAt(LocalDateTime.now());
        if (trade.getAssetSymbol() != null) trade.setAssetSymbol(trade.getAssetSymbol().toUpperCase());
        if (trade.getSide() != null) trade.setSide(trade.getSide().toUpperCase());
        return tradeRepository.save(trade);
    }

    public List<Trade> getUserTrades(Long userId) {
        return tradeRepository.findByUserIdOrderByExecutedAtDesc(userId);
    }

    public void deleteTrade(Long tradeId) {
        tradeRepository.deleteById(tradeId);
    }

    public Trade updateTrade(Long tradeId, Trade updatedTrade) {
        return tradeRepository.findById(tradeId).map(trade -> {
            
            if (updatedTrade.getAssetSymbol() != null) trade.setAssetSymbol(updatedTrade.getAssetSymbol().toUpperCase());
            if (updatedTrade.getSide() != null) trade.setSide(updatedTrade.getSide().toUpperCase());
            
            trade.setQuantity(updatedTrade.getQuantity());
            trade.setPrice(updatedTrade.getPrice());
            trade.setFee(updatedTrade.getFee());
            trade.setExchangeName(updatedTrade.getExchangeName());
            trade.setStorageType(updatedTrade.getStorageType());
            
            return tradeRepository.save(trade);
        }).orElseThrow(() -> new RuntimeException("Trade not found"));
    }

    // --- Portfolio Logic ---
    public List<Map<String, Object>> getPortfolio(Long userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        
        if (holdings.isEmpty()) return new ArrayList<>();

        // 1. Prepare Batch Fetch
        Set<String> symbols = new HashSet<>();
        for (Holding h : holdings) {
            symbols.add(h.getSymbol().toUpperCase());
        }

        Map<String, MarketData> marketDataMap = cryptoPriceService.getBatchMarketData(symbols);
        List<Map<String, Object>> portfolio = new ArrayList<>();
        
        Map<Long, String> exchangeNames = new HashMap<>();

        // 2. Build Response
        for (Holding h : holdings) {
            double qty = h.getQuantity();
            if (qty <= 0.000001) continue;

            String sym = h.getSymbol().toUpperCase();
            MarketData md = marketDataMap.getOrDefault(sym, new MarketData(0.0, 0.0, 0.0, 0.0));
            
            double currentPrice = (md.price > 0) ? md.price : h.getAvgCost();
            double value = qty * currentPrice;
            double totalSpent = qty * h.getAvgCost();

            Map<String, Object> item = new HashMap<>();
            item.put("id", h.getId());
            item.put("symbol", sym);
            item.put("name", cryptoPriceService.getNameForSymbol(sym));
            item.put("quantity", qty);
            item.put("avgBuyPrice", h.getAvgCost());
            item.put("currentPrice", currentPrice);
            item.put("value", value);
            item.put("pl", value - totalSpent);
            item.put("plPercent", (totalSpent > 0) ? ((value - totalSpent) / totalSpent) * 100 : 0.0);
            item.put("change24hPercent", md.change24h);
            
            Long exId = h.getExchangeId();
            if (exId != null) {
                if (!exchangeNames.containsKey(exId)) {
                    exchangeRepository.findById(exId).ifPresent(ex -> exchangeNames.put(exId, ex.getName()));
                }
                item.put("exchange", exchangeNames.getOrDefault(exId, "Unknown Exchange"));
                item.put("walletType", "exchange");
            } else {
                item.put("exchange", "Manual / Wallet");
                item.put("walletType", h.getWalletType());
            }

            portfolio.add(item);
        }
        
        return portfolio;
    }
    
    public List<Map<String, Object>> getPortfolioHoldings(Long userId) {
        return getPortfolio(userId);
    }
}