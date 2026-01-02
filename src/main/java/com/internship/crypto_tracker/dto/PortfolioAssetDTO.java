package com.internship.crypto_tracker.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PortfolioAssetDTO {
    private String assetSymbol;    // "BTC"
    private BigDecimal quantity;   // 0.5
    private BigDecimal currentPrice; // $50,000
    private BigDecimal totalValue;   // $25,000 (Calculated)
    private BigDecimal profitLoss;   // +$500 (Calculated)
    
    // Constructor to do the math automatically
    public PortfolioAssetDTO(String symbol, BigDecimal qty, BigDecimal avgCost, BigDecimal livePrice) {
        this.assetSymbol = symbol;
        this.quantity = qty;
        this.currentPrice = livePrice;
        
        // Calculate Total Value (Qty * Current Price)
        this.totalValue = qty.multiply(livePrice);

        // Calculate P&L: (Current Price - Avg Cost) * Quantity
        if (avgCost != null && avgCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceDiff = livePrice.subtract(avgCost);
            this.profitLoss = priceDiff.multiply(qty);
        } else {
            this.profitLoss = BigDecimal.ZERO;
        }
    }
}