package com.crypto.portfolio.tracker.service;

import com.crypto.portfolio.tracker.entity.Trade;
import java.util.List;

public interface TradeService {
    Trade addTrade(Trade trade);
    List<Trade> getAllTrades();
    List<Trade> getByUsername(String username);
    Trade updateTrade(Long id, Trade trade);
    void deleteTrade(Long id);
}