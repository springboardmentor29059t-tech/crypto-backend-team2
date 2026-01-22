package com.crypto.portfolio.dto;

import java.time.LocalDateTime;

public class TaxReportItemDTO {
    private String symbol;
    private double quantity;
    private double buyPrice;
    private double sellPrice;
    private double gainLoss;
    private String termType; // "Short-Term" or "Long-Term"
    private LocalDateTime buyDate;
    private LocalDateTime sellDate;

    public TaxReportItemDTO(String symbol, double quantity, double buyPrice, double sellPrice, double gainLoss,
            String termType, LocalDateTime buyDate, LocalDateTime sellDate) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.gainLoss = gainLoss;
        this.termType = termType;
        this.buyDate = buyDate;
        this.sellDate = sellDate;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public double getGainLoss() {
        return gainLoss;
    }

    public String getTermType() {
        return termType;
    }

    public LocalDateTime getBuyDate() {
        return buyDate;
    }

    public LocalDateTime getSellDate() {
        return sellDate;
    }
}
