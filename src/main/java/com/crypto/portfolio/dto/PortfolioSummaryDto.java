package com.crypto.portfolio.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PortfolioSummaryDto {
    private double totalPortfolioValue;
    private List<AssetDetailDto> assets;
    private LocalDateTime lastUpdated;

    public PortfolioSummaryDto() {}
    
    public PortfolioSummaryDto(double totalPortfolioValue, List<AssetDetailDto> assets, LocalDateTime lastUpdated) {
        this.totalPortfolioValue = totalPortfolioValue;
        this.assets = assets;
        this.lastUpdated = lastUpdated;
    }
    
        

    // Getters and Setters
    public double getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public void setTotalPortfolioValue(double totalPortfolioValue) {
        this.totalPortfolioValue = totalPortfolioValue;
    }

    public List<AssetDetailDto> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetDetailDto> assets) {
        this.assets = assets;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}