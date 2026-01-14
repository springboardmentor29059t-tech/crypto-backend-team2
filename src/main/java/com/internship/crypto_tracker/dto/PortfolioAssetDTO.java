package com.internship.crypto_tracker.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PortfolioAssetDTO {
    private String assetSymbol;   
    private BigDecimal quantity;   
    private BigDecimal currentPrice; 
    private BigDecimal totalValue;   
    private BigDecimal profitLoss;  
    
    public PortfolioAssetDTO(String symbol, BigDecimal qty, BigDecimal avgCost, BigDecimal livePrice) {
        this.assetSymbol = symbol;
        this.quantity = qty;
        this.currentPrice = livePrice;
        
        this.totalValue = qty.multiply(livePrice);

        if (avgCost != null && avgCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceDiff = livePrice.subtract(avgCost);
            this.profitLoss = priceDiff.multiply(qty);
        } else {
            this.profitLoss = BigDecimal.ZERO;
        }
    }
}