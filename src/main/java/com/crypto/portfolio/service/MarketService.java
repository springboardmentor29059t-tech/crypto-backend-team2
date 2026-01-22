package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.MarketCoinDto;
import com.crypto.portfolio.dto.MarketOverviewDto;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.cache.annotation.Cacheable;
import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MarketService {

        private final String COINGECKO_API_URL = "https://api.coingecko.com/api/v3";
        private final RestTemplate restTemplate = new RestTemplate();

        private MarketOverviewDto cachedOverview = new MarketOverviewDto(0, 0, 0, 0, 0, new ArrayList<>(),
                        new ArrayList<>(), new ArrayList<>());
        private List<Map<String, Object>> cachedTopCoins = new ArrayList<>();
        private java.time.LocalDateTime lastUpdated = java.time.LocalDateTime.now();
        private double impliedInrRate = 84.0;

        @PostConstruct
        @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)

        public void refreshMarketData() {
                System.out.println("Scheduled Refresh: Fetching Market Data...");
                try {
                        MarketOverviewDto newOverview = fetchMarketOverviewFromApi();
                        if (newOverview != null && newOverview.getTotalMarketCap() > 0) {
                                this.cachedOverview = newOverview;
                        }

                        // Reduce to 50 to avoid timeouts/rate limits and because we only use top 50 in
                        // Global Node
                        List<Map<String, Object>> newTopCoins = fetchTopCoinsFromApi(1, 50);
                        if (newTopCoins != null && !newTopCoins.isEmpty()) {
                                this.cachedTopCoins = newTopCoins;
                        }

                        this.lastUpdated = java.time.LocalDateTime.now();
                } catch (Exception e) {
                        System.err.println("Scheduled Refresh Error: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        public List<Map<String, Object>> getTopCoins(int page, int perPage) {
                // Simple pagination for cached data
                int start = (page - 1) * perPage;
                if (start >= cachedTopCoins.size())
                        return new ArrayList<>();
                int end = Math.min(start + perPage, cachedTopCoins.size());
                return cachedTopCoins.subList(start, end);
        }

        public MarketOverviewDto getMarketOverview() {
                return cachedOverview;
        }

        public java.time.LocalDateTime getLastUpdated() {
                return lastUpdated;
        }

        public double getUsdInrRate() {
                return impliedInrRate;
        }

        private List<Map<String, Object>> fetchTopCoinsFromApi(int page, int perPage) {
                try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("User-Agent", "CryptoPortfolioTracker/1.0");
                        headers.set("Accept", "application/json");
                        HttpEntity<String> entity = new HttpEntity<>(headers);

                        // Disabled sparkline to reduce payload size
                        String url = String.format(
                                        "%s/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=%d&page=%d&sparkline=true&price_change_percentage=24h",
                                        COINGECKO_API_URL, perPage, page);

                        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                                        url,
                                        org.springframework.http.HttpMethod.GET,
                                        entity,
                                        new ParameterizedTypeReference<List<Map<String, Object>>>() {
                                        });

                        return response.getBody();
                } catch (Exception e) {
                        System.err.println("Error fetching top coins: " + e.getMessage());
                        return null;
                }
        }

        private MarketOverviewDto fetchMarketOverviewFromApi() {
                try {
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("User-Agent", "CryptoPortfolioTracker/1.0");
                        headers.set("Accept", "application/json");
                        HttpEntity<String> entity = new HttpEntity<>(headers);

                        ResponseEntity<Map<String, Object>> globalResponse = restTemplate.exchange(
                                        COINGECKO_API_URL + "/global",
                                        org.springframework.http.HttpMethod.GET,
                                        entity,
                                        new ParameterizedTypeReference<Map<String, Object>>() {
                                        });

                        Map<String, Object> globalData = globalResponse.getBody();
                        if (globalData == null || !globalData.containsKey("data"))
                                return null;

                        Map<String, Object> data = (Map<String, Object>) globalData.get("data");
                        Map<String, Object> totalMarketCapMap = (Map<String, Object>) data.get("total_market_cap");
                        Map<String, Object> totalVolumeMap = (Map<String, Object>) data.get("total_volume");

                        double totalMarketCapUsd = ((Number) totalMarketCapMap.get("usd")).doubleValue();
                        double totalMarketCapInr = ((Number) totalMarketCapMap.get("inr")).doubleValue();
                        double totalVolume24hUsd = ((Number) totalVolumeMap.get("usd")).doubleValue();
                        double totalVolume24hInr = ((Number) totalVolumeMap.get("inr")).doubleValue();
                        double marketCapChange24h = ((Number) data.get("market_cap_change_percentage_24h_usd"))
                                        .doubleValue();

                        this.impliedInrRate = totalMarketCapUsd > 0 ? (totalMarketCapInr / totalMarketCapUsd) : 84.0;

                        ResponseEntity<MarketCoinDto[]> coinsResponse = restTemplate.exchange(
                                        COINGECKO_API_URL
                                                        + "/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=50&page=1&sparkline=false",
                                        org.springframework.http.HttpMethod.GET,
                                        entity,
                                        MarketCoinDto[].class);

                        MarketCoinDto[] coins = coinsResponse.getBody();
                        List<MarketCoinDto> coinList = coins != null ? Arrays.asList(coins) : new ArrayList<>();
                        coinList.forEach(coin -> coin.setCurrentPriceInr(coin.getCurrentPrice() * impliedInrRate));

                        List<MarketCoinDto> topGainers = coinList.stream()
                                        .sorted(Comparator.comparingDouble(MarketCoinDto::getPriceChangePercentage24h)
                                                        .reversed())
                                        .limit(5).collect(Collectors.toList());

                        List<MarketCoinDto> topLosers = coinList.stream()
                                        .sorted(Comparator.comparingDouble(MarketCoinDto::getPriceChangePercentage24h))
                                        .limit(5).collect(Collectors.toList());

                        List<MarketCoinDto> trending = coinList.stream().limit(5).collect(Collectors.toList());

                        return new MarketOverviewDto(totalMarketCapUsd, totalMarketCapInr, totalVolume24hUsd,
                                        totalVolume24hInr, marketCapChange24h, topGainers, topLosers, trending);
                } catch (Exception e) {
                        System.err.println("Error fetching market overview: " + e.getMessage());
                        return null;
                }
        }

        public double getPriceBySymbol(String symbol) {
                // 1. Check cache first
                for (Map<String, Object> coin : cachedTopCoins) {
                        if (symbol.equalsIgnoreCase((String) coin.get("symbol"))) {
                                Object priceObj = coin.get("current_price");
                                if (priceObj instanceof Number) {
                                        return ((Number) priceObj).doubleValue();
                                }
                        }
                }

                // 2. If not in cache, try to fetch from API directly
                System.out.println("MarketService: Symbol " + symbol + " not in top 50 cache. Fetching from API...");
                return fetchPriceFromApi(symbol);
        }

        private double fetchPriceFromApi(String symbol) {
                try {
                        String url = String.format(
                                        "%s/coins/markets?vs_currency=usd&symbols=%s",
                                        COINGECKO_API_URL, symbol.toLowerCase());

                        HttpHeaders headers = new HttpHeaders();
                        headers.set("User-Agent", "CryptoPortfolioTracker/1.0");
                        headers.set("Accept", "application/json"); // Important for some APIs
                        HttpEntity<String> entity = new HttpEntity<>(headers);

                        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                                        url,
                                        org.springframework.http.HttpMethod.GET,
                                        entity,
                                        new ParameterizedTypeReference<List<Map<String, Object>>>() {
                                        });

                        List<Map<String, Object>> body = response.getBody();
                        if (body != null && !body.isEmpty()) {
                                Map<String, Object> coinData = body.get(0);
                                double price = ((Number) coinData.get("current_price")).doubleValue();
                                System.out.println(
                                                "MarketService: Fetched price for " + symbol + " from API: " + price);
                                return price;
                        }
                } catch (Exception e) {
                        System.err.println(
                                        "MarketService: Error fetching price for " + symbol + " - " + e.getMessage());
                }
                return 0;
        }
}
