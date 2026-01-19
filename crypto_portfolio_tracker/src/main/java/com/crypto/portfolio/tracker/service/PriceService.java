package com.crypto.portfolio.tracker.service;

import com.crypto.portfolio.tracker.entity.PriceSnapshot;
import com.crypto.portfolio.tracker.repository.PriceSnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceService {

    private final PriceSnapshotRepository repository;

    public PriceService(PriceSnapshotRepository repository) {
        this.repository = repository;
    }

    // Save price
    public PriceSnapshot savePrice(String coinName, String symbol, double price) {
        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setCoinName(coinName);
        snapshot.setSymbol(symbol);
        snapshot.setPrice(price);
        snapshot.setTimestamp(LocalDateTime.now());
        return repository.save(snapshot);
    }

    // Get all prices
    public List<PriceSnapshot> getAllPrices() {
        return repository.findAll();
    }

    // Get price history
    public List<PriceSnapshot> getPriceHistory(String symbol) {
        return repository.findBySymbolOrderByTimestampAsc(symbol);
    }

    // Get latest price
    public PriceSnapshot getLatestPrice(String symbol) {
        return repository.findTopBySymbolOrderByTimestampDesc(symbol);
    }
}