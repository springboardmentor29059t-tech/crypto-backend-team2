package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.AllocationDto;
import com.crypto.portfolio.model.Asset;
import com.crypto.portfolio.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final AssetRepository assetRepository;
    private final MarketService marketService;

    public AnalyticsService(AssetRepository assetRepository, MarketService marketService) {
        this.assetRepository = assetRepository;
        this.marketService = marketService;
    }

    public int getPortfolioHealthScore(Long userId) {
        List<Asset> assets = assetRepository.findByUserId(userId);
        if (assets.isEmpty())
            return 0;

        // Simple logic: Diversification (number of assets)
        int baseScore = 60;
        // Diversification bonus
        baseScore += Math.min(assets.size() * 5, 40);

        return Math.min(Math.max(baseScore, 0), 100);
    }

    public List<AllocationDto> getAllocation(Long userId) {
        List<Asset> assets = assetRepository.findByUserId(userId);

        BigDecimal totalValue = BigDecimal.ZERO;
        List<AssetAllocationTemp> tempHoldings = new ArrayList<>();

        for (Asset asset : assets) {
            double livePrice = marketService.getPriceBySymbol(asset.getSymbol());
            if (livePrice == 0)
                livePrice = asset.getAvgBuyPrice().doubleValue();

            BigDecimal currentValue = asset.getAmount().multiply(BigDecimal.valueOf(livePrice));
            totalValue = totalValue.add(currentValue);
            tempHoldings.add(new AssetAllocationTemp(asset.getSymbol(), currentValue));
        }

        final BigDecimal finalTotalValue = totalValue;
        return tempHoldings.stream().map(h -> {
            BigDecimal percentage = finalTotalValue.compareTo(BigDecimal.ZERO) > 0
                    ? h.value.divide(finalTotalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
            return new AllocationDto(h.symbol, h.value, percentage);
        }).collect(Collectors.toList());
    }

    private static class AssetAllocationTemp {
        String symbol;
        BigDecimal value;

        AssetAllocationTemp(String s, BigDecimal v) {
            this.symbol = s;
            this.value = v;
        }
    }
}
