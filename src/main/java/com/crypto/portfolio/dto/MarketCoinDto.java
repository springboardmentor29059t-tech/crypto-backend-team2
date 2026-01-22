package com.crypto.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketCoinDto {
    private String id;
    private String symbol;
    private String name;
    private String image;

    @JsonProperty("current_price")
    private double currentPrice;

    private double currentPriceInr;

    @JsonProperty("market_cap")
    private double marketCap;

    @JsonProperty("price_change_percentage_24h")
    private double priceChangePercentage24h;

    public MarketCoinDto() {
    }

    public MarketCoinDto(String id, String symbol, String name, String image, double currentPrice,
            double currentPriceInr, double marketCap,
            double priceChangePercentage24h) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.currentPriceInr = currentPriceInr;
        this.marketCap = marketCap;
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getCurrentPriceInr() {
        return currentPriceInr;
    }

    public void setCurrentPriceInr(double currentPriceInr) {
        this.currentPriceInr = currentPriceInr;
    }

    public double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(double marketCap) {
        this.marketCap = marketCap;
    }

    public double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public void setPriceChangePercentage24h(double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }
}
