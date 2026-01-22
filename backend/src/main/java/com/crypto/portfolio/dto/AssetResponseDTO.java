package com.crypto.portfolio.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class AssetResponseDTO {

    private Long id;
    private String symbol;
    private String name;
    private BigDecimal amount;
    private BigDecimal avgBuyPrice;
    private BigDecimal currentValue; // Calculated
    private BigDecimal totalValue; // Calculated
    private BigDecimal profitLoss; // Calculated
    private BigDecimal profitLossPercentage; // Calculated
    private String source;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // Constructors
    public AssetResponseDTO() {
    }

    public AssetResponseDTO(Long id, String symbol, String name, BigDecimal amount, BigDecimal avgBuyPrice,
            BigDecimal currentValue, BigDecimal totalValue, BigDecimal profitLoss,
            BigDecimal profitLossPercentage, String source, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.amount = amount;
        this.avgBuyPrice = avgBuyPrice;
        this.currentValue = currentValue;
        this.totalValue = totalValue;
        this.profitLoss = profitLoss;
        this.profitLossPercentage = profitLossPercentage;
        this.source = source;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(BigDecimal avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(BigDecimal profitLoss) {
        this.profitLoss = profitLoss;
    }

    public BigDecimal getProfitLossPercentage() {
        return profitLossPercentage;
    }

    public void setProfitLossPercentage(BigDecimal profitLossPercentage) {
        this.profitLossPercentage = profitLossPercentage;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
