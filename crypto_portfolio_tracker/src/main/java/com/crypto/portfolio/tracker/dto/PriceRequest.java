package com.crypto.portfolio.tracker.dto;

public class PriceRequest {

    private String coinName;
    private String symbol;
    private double price;

    // Getters & Setters
    public String getCoinName() { return coinName; }
    public void setCoinName(String coinName) { this.coinName = coinName; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}