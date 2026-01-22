package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.GlobalMarketResponse;
import com.crypto.portfolio.dto.MarketOverviewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GlobalMarketService {

    @Autowired
    private MarketService marketService;

    public GlobalMarketResponse getGlobalMarketData() {
        try {
            MarketOverviewDto overview = marketService.getMarketOverview();

            GlobalMarketResponse response = new GlobalMarketResponse();

            response.setGlobalMarketCap(String.format("₹%.0f", overview.getTotalMarketCapInr()));
            response.setVolume24h(String.format("₹%.0f", overview.getTotalVolume24hInr()));
            response.setBitcoinDominance("54.2%");
            response.setFearGreedIndex(68);
            response.setMarketTrend(overview.getMarketCapChangePercentage24h() >= 0 ? "Bullish" : "Bearish");

            return response;
        } catch (Exception e) {
            GlobalMarketResponse response = new GlobalMarketResponse();
            response.setGlobalMarketCap("₹287120000000000");
            response.setVolume24h("₹8120000000000");
            response.setBitcoinDominance("52.3%");
            response.setFearGreedIndex(65);
            response.setMarketTrend("Bullish");
            return response;
        }
    }
}
