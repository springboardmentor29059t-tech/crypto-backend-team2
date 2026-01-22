package com.crypto.portfolio.dto;

public class AssetDetailDto {
    private Long id;
    private String symbol;
    private String name;
    private Double quantity;
    private Double avgPrice;
    private Double livePrice;
    private Double currentValue;
    private Double profitLoss;
    private String riskLevel;
    private Double marketCap;
    private Double change24h;

    public AssetDetailDto() {
    }

    public AssetDetailDto(Long id, String symbol, String name, Double quantity, Double avgPrice, Double livePrice,
            Double currentValue, Double profitLoss) {
        this(id, symbol, name, quantity, avgPrice, livePrice, currentValue, profitLoss, "LOW", 0.0, 0.0);
    }

    public AssetDetailDto(Long id, String symbol, String name, Double quantity, Double avgPrice, Double livePrice,
            Double currentValue, Double profitLoss, String riskLevel, Double marketCap, Double change24h) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.livePrice = livePrice;
        this.currentValue = currentValue;
        this.profitLoss = profitLoss;
        this.riskLevel = riskLevel;
        this.marketCap = marketCap;
        this.change24h = change24h;
    }

    // Getters and Setters
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

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(Double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public Double getLivePrice() {
        return livePrice;
    }

    public void setLivePrice(Double livePrice) {
        this.livePrice = livePrice;
    }

    public Double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }

    public Double getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(Double profitLoss) {
        this.profitLoss = profitLoss;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }

    public Double getChange24h() {
        return change24h;
    }

    public void setChange24h(Double change24h) {
        this.change24h = change24h;
    }
}