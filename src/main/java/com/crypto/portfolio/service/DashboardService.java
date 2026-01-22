package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.AssetDetailDto;
import com.crypto.portfolio.dto.PortfolioSummaryDto;
import com.crypto.portfolio.model.Asset;
import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.AssetRepository;
import com.crypto.portfolio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public PortfolioSummaryDto getPortfolioSummary(String userEmail) {
        // Get user from database
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's assets from database
        List<Asset> assets = assetRepository.findByUserId(user.getId());

        if (assets.isEmpty()) {
            return new PortfolioSummaryDto(0.0, new ArrayList<>(), LocalDateTime.now());
        }

        // Extract unique crypto symbols (using symbols for simple price API, though IDs
        // are better)
        Set<String> cryptoSymbols = new HashSet<>();
        for (Asset asset : assets) {
            cryptoSymbols.add(asset.getSymbol().toLowerCase());
        }

        // Fetch live prices from CoinGecko for the user's holdings
        Map<String, Double> livePrices = fetchLivePricesFromCoinGecko(cryptoSymbols);

        // Calculate portfolio summary
        List<AssetDetailDto> assetDetails = new ArrayList<>();
        double totalPortfolioValue = 0.0;

        for (Asset asset : assets) {
            String symbol = asset.getSymbol().toLowerCase();
            double livePrice = livePrices.getOrDefault(symbol, 0.0);

            double amount = asset.getAmount().doubleValue();
            double avgBuyPrice = asset.getAvgBuyPrice().doubleValue();
            double currentValue = amount * livePrice;
            double profitLoss = currentValue - (amount * avgBuyPrice);

            AssetDetailDto assetDetail = new AssetDetailDto();
            assetDetail.setSymbol(asset.getSymbol());
            assetDetail.setQuantity(amount);
            assetDetail.setAvgPrice(avgBuyPrice);
            assetDetail.setLivePrice(livePrice);
            assetDetail.setCurrentValue(currentValue);
            assetDetail.setProfitLoss(profitLoss);

            assetDetails.add(assetDetail);
            totalPortfolioValue += currentValue;
        }

        return new PortfolioSummaryDto(totalPortfolioValue, assetDetails, LocalDateTime.now());
    }

    private Map<String, Double> fetchLivePricesFromCoinGecko(Set<String> cryptoIds) {
        try {
            String ids = String.join(",", cryptoIds);
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + ids + "&vs_currencies=usd";

            // Using RestTemplate to fetch live prices from CoinGecko
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Map<String, Double>> response = restTemplate.getForObject(url, Map.class);

            Map<String, Double> prices = new HashMap<>();
            if (response != null) {
                for (Map.Entry<String, Map<String, Double>> entry : response.entrySet()) {
                    String cryptoId = entry.getKey();
                    Map<String, Double> priceData = entry.getValue();
                    Double usdPrice = priceData.get("usd");
                    if (usdPrice != null) {
                        prices.put(cryptoId, usdPrice);
                    }
                }
            }

            return prices;
        } catch (Exception e) {
            System.err.println("Error fetching live prices from CoinGecko: " + e.getMessage());
            // Return empty map or default values in case of error
            return new HashMap<>();
        }
    }
}