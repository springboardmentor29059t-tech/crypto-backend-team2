package com.crypto.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.crypto.portfolio.dto.MarketDataResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LiveMarketService {

    private final String COINGECKO_API_URL = "https://api.coingecko.com/api/v3";
    private final RestTemplate restTemplate;

    // In-memory cache: Key (e.g., "global") -> Result
    private final ConcurrentHashMap<String, MarketDataResult> cache = new ConcurrentHashMap<>();
    private final long CACHE_TTL_SECONDS = 30;

    public LiveMarketService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000); // 2 seconds connect timeout
        factory.setReadTimeout(2000); // 2 seconds read timeout
        this.restTemplate = new RestTemplate(factory);
    }

    public BigDecimal getLivePrice(String coinId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(COINGECKO_API_URL + "/simple/price")
                    .queryParam("ids", coinId)
                    .queryParam("vs_currencies", "usd")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "CryptoPortfolioTracker/1.0");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>("parameters",
                    headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET,
                    entity, JsonNode.class);

            if (response.getBody() != null && response.getBody().has(coinId)) {
                return BigDecimal.valueOf(response.getBody().get(coinId).get("usd").asDouble());
            }
        } catch (Exception e) {
            System.err.println("Error fetching live price for " + coinId + ": " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public MarketDataResult getLiveMarketData(List<String> coinIds) {
        String cacheKey = "global_" + String.join("_", coinIds).hashCode();

        // 1. Check Cache (Fast Path)
        MarketDataResult cached = cache.get(cacheKey);
        if (cached != null) {
            Instant lastUpdate = Instant.parse(cached.getTimestamp());
            if (Duration.between(lastUpdate, Instant.now()).getSeconds() < CACHE_TTL_SECONDS) {
                // Return fresh cache
                return new MarketDataResult(cached.getData(), "CACHED", cached.getTimestamp());
            }
        }

        // 2. Try Fetching Live (Slow Path)
        try {
            String url = UriComponentsBuilder.fromHttpUrl(COINGECKO_API_URL + "/coins/markets")
                    .queryParam("vs_currency", "usd")
                    .queryParam("ids", String.join(",", coinIds))
                    .queryParam("order", "market_cap_desc")
                    .queryParam("sparkline", "false")
                    .queryParam("price_change_percentage", "24h")
                    .toUriString();

            System.out.println("Fetching Global Data URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "CryptoPortfolioTracker/1.0");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>("parameters",
                    headers);

            ResponseEntity<List> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity,
                    List.class);

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                System.out.println("Global Data Fetched: " + response.getBody().size() + " items");

                // Update Cache
                MarketDataResult result = new MarketDataResult(
                        response.getBody(),
                        "LIVE",
                        Instant.now().toString());
                cache.put(cacheKey, result);
                return result;
            }
        } catch (Exception e) {
            System.err.println("Error fetching live market data: " + e.getMessage());
        }

        // 3. Fallback to Stale Cache
        if (cached != null) {
            System.out.println("Returning Stale Cache due to API failure");
            return new MarketDataResult(cached.getData(), "FALLBACK", cached.getTimestamp());
        }

        // 4. Absolute Fallback (Offline)
        System.err.println("No cache available. Returning Offline data.");
        // Return structured offline data so UI doesn't break
        List<Map<String, Object>> offlineData = List.of(
                Map.of("id", "bitcoin", "symbol", "btc", "current_price", 40000.0, "price_change_percentage_24h", 0.0),
                Map.of("id", "ethereum", "symbol", "eth", "current_price", 2200.0, "price_change_percentage_24h", 0.0),
                Map.of("id", "solana", "symbol", "sol", "current_price", 95.0, "price_change_percentage_24h", 0.0),
                Map.of("id", "binancecoin", "symbol", "bnb", "current_price", 300.0, "price_change_percentage_24h",
                        0.0),
                Map.of("id", "ripple", "symbol", "xrp", "current_price", 0.50, "price_change_percentage_24h", 0.0),
                Map.of("id", "cardano", "symbol", "ada", "current_price", 0.50, "price_change_percentage_24h", 0.0),
                Map.of("id", "dogecoin", "symbol", "doge", "current_price", 0.08, "price_change_percentage_24h", 0.0),
                Map.of("id", "avalanche-2", "symbol", "avax", "current_price", 35.0, "price_change_percentage_24h",
                        0.0),
                Map.of("id", "polkadot", "symbol", "dot", "current_price", 7.0, "price_change_percentage_24h", 0.0),
                Map.of("id", "chainlink", "symbol", "link", "current_price", 14.0, "price_change_percentage_24h", 0.0));

        return new MarketDataResult(
                offlineData,
                "OFFLINE",
                Instant.now().toString());
    }

    public List<Map<String, Object>> getHistoricalPrices(String coinId, int days) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(COINGECKO_API_URL + "/coins/" + coinId + "/market_chart")
                    .queryParam("vs_currency", "usd")
                    .queryParam("days", days)
                    .queryParam("interval", "daily")
                    .toUriString();

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET,
                    null, JsonNode.class);

            if (response.getBody() != null && response.getBody().has("prices")) {
                JsonNode prices = response.getBody().get("prices");
                List<Map<String, Object>> history = new java.util.ArrayList<>();
                for (JsonNode pricePoint : prices) {
                    history.add(Map.of(
                            "timestamp", pricePoint.get(0).asLong(),
                            "price", pricePoint.get(1).asDouble()));
                }
                return history;
            }
        } catch (Exception e) {
            System.err.println("Error fetching historical data: " + e.getMessage());
        }
        return java.util.Collections.emptyList();
    }

    public Map<String, Object> getGlobalMarketOverview() {
        try {
            String url = COINGECKO_API_URL + "/global";
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET,
                    null, JsonNode.class);

            if (response.getBody() != null && response.getBody().has("data")) {
                JsonNode data = response.getBody().get("data");
                return Map.of(
                        "market_cap_change_percentage_24h_usd",
                        data.get("market_cap_change_percentage_24h_usd").asDouble(),
                        "active_cryptocurrencies", data.get("active_cryptocurrencies").asInt());
            }
        } catch (Exception e) {
            System.err.println("Error fetching global data: " + e.getMessage());
        }
        return Map.of("market_cap_change_percentage_24h_usd", 0.0, "active_cryptocurrencies", 0);
    }
}
