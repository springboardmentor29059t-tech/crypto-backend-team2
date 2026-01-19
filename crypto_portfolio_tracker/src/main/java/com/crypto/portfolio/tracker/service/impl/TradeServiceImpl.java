package com.crypto.portfolio.tracker.service.impl;

import com.crypto.portfolio.tracker.entity.Trade;
import com.crypto.portfolio.tracker.repository.TradeRepository;
import com.crypto.portfolio.tracker.service.TradeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepository repository;

    public TradeServiceImpl(TradeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Trade addTrade(Trade trade) {
        return repository.save(trade);
    }

    @Override
    public List<Trade> getAllTrades() {
        return repository.findAll();
    }

    @Override
    public List<Trade> getByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public Trade updateTrade(Long id, Trade trade) {
        Optional<Trade> existing = repository.findById(id);
        if (existing.isPresent()) {
            Trade t = existing.get();
            t.setCoinName(trade.getCoinName());
            t.setSymbol(trade.getSymbol());
            t.setQuantity(trade.getQuantity());
            t.setPrice(trade.getPrice());
            t.setType(trade.getType());
            t.setUsername(trade.getUsername());
            return repository.save(t);
        } else {
            return null; // or throw exception
        }
    }

    @Override
    public void deleteTrade(Long id) {
        repository.deleteById(id);
    }

}