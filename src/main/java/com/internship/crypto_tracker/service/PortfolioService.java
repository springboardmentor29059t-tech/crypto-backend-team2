package com.internship.crypto_tracker.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.internship.crypto_tracker.dto.PortfolioAssetDTO;
import com.internship.crypto_tracker.model.Holding;
import com.internship.crypto_tracker.model.PriceSnapshot;
import com.internship.crypto_tracker.repository.HoldingRepository;
import com.internship.crypto_tracker.repository.PriceSnapshotRepository;

@Service
public class PortfolioService {

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;

    public List<PortfolioAssetDTO> getPortfolioSummary(Long userId) {
        List<PortfolioAssetDTO> summary = new ArrayList<>();

        List<Holding> holdings = holdingRepository.findByUserId(userId);

        for (Holding holding : holdings) {
            String symbol = holding.getAssetSymbol();
            
            PriceSnapshot latestPrice = priceSnapshotRepository.findTopByAssetSymbolOrderByCapturedAtDesc(symbol);
            BigDecimal currentPrice = (latestPrice != null) ? latestPrice.getPriceUsd() : BigDecimal.ZERO;

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