package com.internship.crypto_tracker.service;

import com.internship.crypto_tracker.dto.PortfolioAssetDTO;
import com.internship.crypto_tracker.model.Holding;
import com.internship.crypto_tracker.model.PriceSnapshot;
import com.internship.crypto_tracker.repository.HoldingRepository;
import com.internship.crypto_tracker.repository.PriceSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioService {

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;

    public List<PortfolioAssetDTO> getPortfolioSummary(Long userId) {
        List<PortfolioAssetDTO> summary = new ArrayList<>();

        // 1. Get all holdings for the user
        List<Holding> holdings = holdingRepository.findByUserId(userId);

        // 2. Loop through each holding and find its current price
        for (Holding holding : holdings) {
            String symbol = holding.getAssetSymbol();
            
            // Get latest price from DB (or default to 0 if missing)
            PriceSnapshot latestPrice = priceSnapshotRepository.findTopByAssetSymbolOrderByCapturedAtDesc(symbol);
            BigDecimal currentPrice = (latestPrice != null) ? latestPrice.getPriceUsd() : BigDecimal.ZERO;

            // 3. Create the DTO (Math happens inside the DTO constructor)
            PortfolioAssetDTO dto = new PortfolioAssetDTO(
                symbol,
                holding.getQuantity(),
                holding.getAvgCost(),
                currentPrice
            );

            summary.add(dto);
        }

        return summary;
    }
}