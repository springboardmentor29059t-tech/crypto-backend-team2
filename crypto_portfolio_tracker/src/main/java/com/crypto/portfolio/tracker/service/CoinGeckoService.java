package com.crypto.portfolio.tracker.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class CoinGeckoService {

    private final RestTemplate restTemplate = new RestTemplate();

    public double getPrice() {

        String url =
                "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd";

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        Map<String, Object> bitcoin =
                (Map<String, Object>) response.get("bitcoin");

        Number price = (Number) bitcoin.get("usd");

        return price.doubleValue(); // ✅ FIX
    }
}