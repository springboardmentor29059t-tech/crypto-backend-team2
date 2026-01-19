package com.crypto.portfolio.tracker.service.impl;

import com.crypto.portfolio.tracker.entity.ProfitLoss;
import com.crypto.portfolio.tracker.entity.Trade;
import com.crypto.portfolio.tracker.entity.PriceSnapshot;
import com.crypto.portfolio.tracker.repository.ProfitLossRepository;
import com.crypto.portfolio.tracker.repository.TradeRepository;
import com.crypto.portfolio.tracker.repository.PriceSnapshotRepository;
import com.crypto.portfolio.tracker.service.ProfitLossService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfitLossServiceImpl implements ProfitLossService {

    private final ProfitLossRepository plRepo;
    private final TradeRepository tradeRepo;
    private final PriceSnapshotRepository priceRepo;

    public ProfitLossServiceImpl(ProfitLossRepository plRepo, TradeRepository tradeRepo, PriceSnapshotRepository priceRepo) {
        this.plRepo = plRepo;
        this.tradeRepo = tradeRepo;
        this.priceRepo = priceRepo;
    }

    @Override
    public ProfitLoss addProfitLoss(ProfitLoss pl) {
        return plRepo.save(pl);
    }

    @Override
    public List<ProfitLoss> getAllProfitLoss() {
        return plRepo.findAll();
    }

    @Override
    public List<ProfitLoss> getByUsername(String username) {
        return plRepo.findByUsername(username);
    }

    @Override
    public ProfitLoss calculateProfitLoss(String symbol) {
        // Get latest price
        PriceSnapshot latestPrice = priceRepo.findTopBySymbolOrderByTimestampDesc(symbol);

        // Get all trades for this symbol
        List<Trade> trades = tradeRepo.findBySymbol(symbol);

        double realizedPL = 0;
        double unrealizedPL = 0;
        double totalQuantity = 0;
        double totalCost = 0;

        for (Trade t : trades) {
            if (t.getType().equalsIgnoreCase("BUY")) {
                totalQuantity += t.getQuantity();
                totalCost += t.getQuantity() * t.getPrice();
            } else {
                // SELL
                double avgPrice = totalCost / totalQuantity;
                realizedPL += t.getQuantity() * (t.getPrice() - avgPrice);
                totalQuantity -= t.getQuantity();
                totalCost -= avgPrice * t.getQuantity();
            }
        }

        if (totalQuantity > 0) {
            double avgPrice = totalCost / totalQuantity;
            unrealizedPL = totalQuantity * (latestPrice.getPrice() - avgPrice);
        }

        ProfitLoss pl = new ProfitLoss();
        pl.setSymbol(symbol);
        pl.setRealizedPL(realizedPL);
        pl.setUnrealizedPL(unrealizedPL);

        return plRepo.save(pl);
    }
}