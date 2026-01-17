package com.internship.crypto_tracker.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference; 
import org.springframework.http.HttpMethod; 
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import com.internship.crypto_tracker.model.Holding;
import com.internship.crypto_tracker.model.PriceSnapshot;
import com.internship.crypto_tracker.repository.HoldingRepository;
import com.internship.crypto_tracker.repository.PriceSnapshotRepository;

import jakarta.annotation.PostConstruct;

@Service
public class CoinGeckoService {

    private static final String COINGECKO_BATCH_URL = "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd";
    private static final String COINGECKO_LIST_URL = "https://api.coingecko.com/api/v3/coins/list";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;

    @Autowired
    private HoldingRepository holdingRepository;

   
    private final Map<String, String> symbolToIdMap = new ConcurrentHashMap<>();

   
   @PostConstruct
    public void loadCoinMappings() {
        System.out.println("⏳ Fetching coin list from CoinGecko to build dynamic mappings...");
        try {
            ResponseEntity<List<Map<String, String>>> response = restTemplate.exchange(
                COINGECKO_LIST_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, String>>>() {}
            );
            List<Map<String, String>> allCoins = response.getBody();

            if (allCoins != null) {
                for (Map<String, String> coin : allCoins) {
                    String symbol = coin.get("symbol");
                    String id = coin.get("id");

                    if (symbol != null && id != null) {
                        symbolToIdMap.putIfAbsent(symbol.toUpperCase(), id);
                    }
                }
                System.out.println("✅ Loaded " + symbolToIdMap.size() + " coin mappings dynamically!");
            }
        } catch (RestClientException e) {
            System.err.println("❌ Failed to load coin list: " + e.getMessage());
            symbolToIdMap.put("BTC", "bitcoin");
            symbolToIdMap.put("ETH", "ethereum");
            symbolToIdMap.put("USDT", "tether");
        }
    }

    public Map<String, BigDecimal> getBatchPrices(List<String> symbols) {
        Map<String, BigDecimal> priceMap = new HashMap<>();
        if (symbols == null || symbols.isEmpty()) return priceMap;

        Map<String, String> idToSymbolMap = new HashMap<>();
        Set<String> uniqueIds = new HashSet<>();

        for (String symbol : symbols) {
            String id = convertSymbolToId(symbol);
            if (id != null) {
                uniqueIds.add(id);
                idToSymbolMap.put(id, symbol);
            }
        }

        if (uniqueIds.isEmpty()) return priceMap;

        try {
            String idsParam = String.join(",", uniqueIds);
            String url = String.format(COINGECKO_BATCH_URL, idsParam);

            ResponseEntity<Map<String, Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {}
            );
            
            Map<String, Map<String, Object>> body = response.getBody();

            if (body != null) {
                for (String id : body.keySet()) {
                    Object priceObj = body.get(id).get("usd");
                    if (priceObj != null) {
                        BigDecimal price = new BigDecimal(priceObj.toString());
                        for (Map.Entry<String, String> entry : idToSymbolMap.entrySet()) {
                            if (entry.getKey().equals(id)) {
                                priceMap.put(entry.getValue(), price);
                            }
                        }
                    }
                }
            }
        } catch (RestClientException e) {
            System.err.println("❌ Batch Price Fetch Failed: " + e.getMessage());
        }
        return priceMap;
    }
    public List<PriceSnapshot> getHistoryForCoin(String symbol) {
        return priceSnapshotRepository.findByAssetSymbolOrderByCapturedAtAsc(symbol);
    }

    private String convertSymbolToId(String symbol) {
        if (symbol == null) return null;
        
        return symbolToIdMap.get(symbol.toUpperCase());
    }

    public BigDecimal getSimplePrice(String symbol) {
        Map<String, BigDecimal> result = getBatchPrices(List.of(symbol));
        return result.getOrDefault(symbol, BigDecimal.ZERO);
    }

    
    @Scheduled(cron = "0 0 0 * * *")
    public void refreshMappings() {
        loadCoinMappings();
    }
    
    @Scheduled(cron = "0 */5 * * * *")
    public void fetchAndSavePrices() {
       try {
            List<Holding> allHoldings = holdingRepository.findAll();
            Set<String> symbolsToTrack = allHoldings.stream()
                    .map(Holding::getAssetSymbol)
                    .collect(Collectors.toSet());

            symbolsToTrack.add("BTC");
            symbolsToTrack.add("ETH");
            symbolsToTrack.add("BNB");
            symbolsToTrack.add("SOL");

            if (symbolsToTrack.isEmpty()) return;

            System.out.println("🔄 Fetching prices for: " + symbolsToTrack);

            Map<String, BigDecimal> prices = getBatchPrices(new ArrayList<>(symbolsToTrack));

            List<PriceSnapshot> snapshots = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
                PriceSnapshot snapshot = new PriceSnapshot();
                snapshot.setAssetSymbol(entry.getKey());
                snapshot.setPriceUsd(entry.getValue());
                snapshot.setCapturedAt(now);
                snapshot.setSource("CoinGecko");
                snapshots.add(snapshot);
            }

            if (!snapshots.isEmpty()) {
                priceSnapshotRepository.saveAll(snapshots);
                System.out.println("Saved " + snapshots.size() + " price snapshots.");
            }

        } catch (Exception e) {
            System.err.println("Error in scheduled price fetch: " + e.getMessage());
        }
    }
}