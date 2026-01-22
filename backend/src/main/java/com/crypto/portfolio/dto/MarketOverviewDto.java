package com.crypto.portfolio.dto;

import java.util.List;

public class MarketOverviewDto {
    private double totalMarketCap;
    private double totalMarketCapInr;
    private double totalVolume24h;
    private double totalVolume24hInr;
    private double marketCapChangePercentage24h;
    private List<MarketCoinDto> topGainers;
    private List<MarketCoinDto> topLosers;
    private List<MarketCoinDto> trending;

    public MarketOverviewDto() {
    }

    public MarketOverviewDto(double totalMarketCap, double totalMarketCapInr, double totalVolume24h,
            double totalVolume24hInr, double marketCapChangePercentage24h,
            List<MarketCoinDto> topGainers, List<MarketCoinDto> topLosers, List<MarketCoinDto> trending) {
        this.totalMarketCap = totalMarketCap;
        this.totalMarketCapInr = totalMarketCapInr;
        this.totalVolume24h = totalVolume24h;
        this.totalVolume24hInr = totalVolume24hInr;
        this.marketCapChangePercentage24h = marketCapChangePercentage24h;
        this.topGainers = topGainers;
        this.topLosers = topLosers;
        this.trending = trending;
    }

    public double getTotalMarketCap() {
        return totalMarketCap;
    }

    public void setTotalMarketCap(double totalMarketCap) {
        this.totalMarketCap = totalMarketCap;
    }

    public double getTotalMarketCapInr() {
        return totalMarketCapInr;
    }

    public void setTotalMarketCapInr(double totalMarketCapInr) {
        this.totalMarketCapInr = totalMarketCapInr;
    }

    public double getTotalVolume24h() {
        return totalVolume24h;
    }

    public void setTotalVolume24h(double totalVolume24h) {
        this.totalVolume24h = totalVolume24h;
    }

    public double getTotalVolume24hInr() {
        return totalVolume24hInr;
    }

    public void setTotalVolume24hInr(double totalVolume24hInr) {
        this.totalVolume24hInr = totalVolume24hInr;
    }

    public double getMarketCapChangePercentage24h() {
        return marketCapChangePercentage24h;
    }

    public void setMarketCapChangePercentage24h(double marketCapChangePercentage24h) {
        this.marketCapChangePercentage24h = marketCapChangePercentage24h;
    }

    public List<MarketCoinDto> getTopGainers() {
        return topGainers;
    }

    public void setTopGainers(List<MarketCoinDto> topGainers) {
        this.topGainers = topGainers;
    }

    public List<MarketCoinDto> getTopLosers() {
        return topLosers;
    }

    public void setTopLosers(List<MarketCoinDto> topLosers) {
        this.topLosers = topLosers;
    }

    public List<MarketCoinDto> getTrending() {
        return trending;
    }

    public void setTrending(List<MarketCoinDto> trending) {
        this.trending = trending;
    }
}
