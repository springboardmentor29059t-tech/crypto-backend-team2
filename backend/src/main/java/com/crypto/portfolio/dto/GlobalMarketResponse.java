package com.crypto.portfolio.dto;

public class GlobalMarketResponse {
    private String globalMarketCap;
    private String volume24h;
    private String bitcoinDominance;
    private int fearGreedIndex;
    private String marketTrend;

    public GlobalMarketResponse() {
    }

    public GlobalMarketResponse(String globalMarketCap, String volume24h, String bitcoinDominance, int fearGreedIndex,
            String marketTrend) {
        this.globalMarketCap = globalMarketCap;
        this.volume24h = volume24h;
        this.bitcoinDominance = bitcoinDominance;
        this.fearGreedIndex = fearGreedIndex;
        this.marketTrend = marketTrend;
    }

    public String getGlobalMarketCap() {
        return globalMarketCap;
    }

    public void setGlobalMarketCap(String globalMarketCap) {
        this.globalMarketCap = globalMarketCap;
    }

    public String getVolume24h() {
        return volume24h;
    }

    public void setVolume24h(String volume24h) {
        this.volume24h = volume24h;
    }

    public String getBitcoinDominance() {
        return bitcoinDominance;
    }

    public void setBitcoinDominance(String bitcoinDominance) {
        this.bitcoinDominance = bitcoinDominance;
    }

    public int getFearGreedIndex() {
        return fearGreedIndex;
    }

    public void setFearGreedIndex(int fearGreedIndex) {
        this.fearGreedIndex = fearGreedIndex;
    }

    public String getMarketTrend() {
        return marketTrend;
    }

    public void setMarketTrend(String marketTrend) {
        this.marketTrend = marketTrend;
    }
}
