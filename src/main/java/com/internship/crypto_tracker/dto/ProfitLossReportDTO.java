package com.internship.crypto_tracker.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProfitLossReportDTO {
    private String assetSymbol;
    private BigDecimal quantity;
    private BigDecimal averageCost;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal unrealizedProfit;
    private BigDecimal realizedProfit;

    
    public ProfitLossReportDTO(String assetSymbol, BigDecimal quantity, BigDecimal averageCost, 
                            BigDecimal currentPrice, BigDecimal realizedProfit) {
        this.assetSymbol = assetSymbol;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.currentPrice = currentPrice;
        this.realizedProfit = realizedProfit;

        this.currentValue = currentPrice.multiply(quantity);

        this.unrealizedProfit = (currentPrice.subtract(averageCost)).multiply(quantity);
    }
}