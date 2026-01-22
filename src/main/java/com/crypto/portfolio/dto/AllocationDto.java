package com.crypto.portfolio.dto;

import java.math.BigDecimal;

public class AllocationDto {
    private String symbol;
    private BigDecimal currentValue;
    private BigDecimal percentage;

    public AllocationDto() {
    }

    public AllocationDto(String symbol, BigDecimal currentValue, BigDecimal percentage) {
        this.symbol = symbol;
        this.currentValue = currentValue;
        this.percentage = percentage;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}
