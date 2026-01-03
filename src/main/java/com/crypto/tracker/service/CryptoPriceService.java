package com.crypto.tracker.service;

import com.crypto.tracker.model.PriceSnapshot;
import com.crypto.tracker.repository.PriceSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CryptoPriceService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;

    public static class MarketData {
        public double price;
        public double change24h;
        public double marketCap;
        public double volume24h;

        public MarketData(double price, double change24h, double marketCap, double volume24h) {
            this.price = price;
            this.change24h = change24h;
            this.marketCap = marketCap;
            this.volume24h = volume24h;
        }
    }

    private final Map<String, String> SYMBOL_TO_ID = new ConcurrentHashMap<>();
    private final Map<String, MarketData> marketCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private final Map<String, List<double[]>> historyCache = new ConcurrentHashMap<>();
    
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 Minutes

    private HttpEntity<String> getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "CryptoTracker/Demo");
        return new HttpEntity<>(headers);
    }

    @PostConstruct
public void initCoinList() {
        // --- 1. Add "SCAM" for Testing Risk Alerts ---
        SYMBOL_TO_ID.put("SCAM", "scam-token-id"); // Fake ID for internal logic

        // --- 2. Major Caps ---
        SYMBOL_TO_ID.put("BTC", "bitcoin");
        SYMBOL_TO_ID.put("ETH", "ethereum");
        SYMBOL_TO_ID.put("USDT", "tether");
        SYMBOL_TO_ID.put("BNB", "binancecoin");
        SYMBOL_TO_ID.put("SOL", "solana");
        SYMBOL_TO_ID.put("USDC", "usd-coin");
        SYMBOL_TO_ID.put("XRP", "ripple");
        SYMBOL_TO_ID.put("ADA", "cardano");
        SYMBOL_TO_ID.put("AVAX", "avalanche-2");
        SYMBOL_TO_ID.put("DOGE", "dogecoin");

        // --- 3. Common Altcoins ---
        SYMBOL_TO_ID.put("DOT", "polkadot");
        SYMBOL_TO_ID.put("TRX", "tron");
        SYMBOL_TO_ID.put("LINK", "chainlink");
        SYMBOL_TO_ID.put("MATIC", "matic-network");
        SYMBOL_TO_ID.put("TON", "the-open-network");
        SYMBOL_TO_ID.put("LTC", "litecoin");
        SYMBOL_TO_ID.put("SHIB", "shiba-inu");
        SYMBOL_TO_ID.put("UNI", "uniswap");
        SYMBOL_TO_ID.put("XLM", "stellar");
        SYMBOL_TO_ID.put("ATOM", "cosmos");
        SYMBOL_TO_ID.put("XMR", "monero");
        SYMBOL_TO_ID.put("ETC", "ethereum-classic");
        SYMBOL_TO_ID.put("LDO", "lido-dao");
        SYMBOL_TO_ID.put("APT", "aptos");
        SYMBOL_TO_ID.put("ARB", "arbitrum");
        SYMBOL_TO_ID.put("NEAR", "near");
        SYMBOL_TO_ID.put("VET", "vechain");
        SYMBOL_TO_ID.put("QNT", "quant-network");
        SYMBOL_TO_ID.put("AAVE", "aave");
        SYMBOL_TO_ID.put("ALGO", "algorand");
        SYMBOL_TO_ID.put("GRT", "the-graph");
        SYMBOL_TO_ID.put("SAND", "the-sandbox");
        SYMBOL_TO_ID.put("MANA", "decentraland");
        SYMBOL_TO_ID.put("AXS", "axie-infinity");
        SYMBOL_TO_ID.put("XTZ", "tezos");
        SYMBOL_TO_ID.put("EGLD", "elrond-erd-2");
        SYMBOL_TO_ID.put("NEO", "neo");
        SYMBOL_TO_ID.put("GALA", "gala");
        SYMBOL_TO_ID.put("LUNC", "terra-luna");
        
        // --- 4. Legacy/Testnet Coins ---
        SYMBOL_TO_ID.put("QTUM", "qtum");
        SYMBOL_TO_ID.put("GAS", "gas");
        SYMBOL_TO_ID.put("LRC", "loopring");
        SYMBOL_TO_ID.put("ZRX", "0x");
        SYMBOL_TO_ID.put("KNC", "kyber-network-crystal");
        SYMBOL_TO_ID.put("BUSD", "binance-usd");

        System.out.println("--- DEMO MODE: Loaded " + SYMBOL_TO_ID.size() + " Top Coins ---");
    }


    public Set<String> getSupportedSymbols() {
        return SYMBOL_TO_ID.keySet();
    }

    public String getNameForSymbol(String symbol) {
        String id = SYMBOL_TO_ID.get(symbol.toUpperCase());
        return (id != null) ? id.substring(0, 1).toUpperCase() + id.substring(1) : symbol;
    }

    public double getSimplePrice(String symbol) {
        if ("SCAM".equalsIgnoreCase(symbol)) return 0.01;
        return getCurrentPrice(symbol);
    }

    public double getCurrentPrice(String symbol) {
        String sym = symbol.toUpperCase();
        if (isValidCache(sym)) return marketCache.get(sym).price;
        getBatchPrices(Collections.singletonList(sym));
        return marketCache.containsKey(sym) ? marketCache.get(sym).price : 0.0;
    }

    public Map<String, Double> getBatchPrices(List<String> symbols) {
        Set<String> uniqueSymbols = new HashSet<>();
        for (String s : symbols) uniqueSymbols.add(s.toUpperCase());
        fetchPricesForSymbols(uniqueSymbols);

        Map<String, Double> results = new HashMap<>();
        for (String sym : uniqueSymbols) {
            results.put(sym, marketCache.containsKey(sym) ? marketCache.get(sym).price : 0.0);
        }
        return results;
    }

    public Map<String, MarketData> getBatchMarketData(Set<String> symbols) {
        fetchPricesForSymbols(symbols);
        Map<String, MarketData> results = new HashMap<>();
        for (String sym : symbols) {
            String upper = sym.toUpperCase();
            results.put(upper, marketCache.getOrDefault(upper, new MarketData(0,0,0,0)));
        }
        return results;
    }

    private void fetchPricesForSymbols(Set<String> symbols) {
        List<String> idsToFetch = new ArrayList<>();
        for (String sym : symbols) {
            if ("SCAM".equalsIgnoreCase(sym)) continue;
            if (!isValidCache(sym)) {
                String id = SYMBOL_TO_ID.get(sym);
                if (id != null) idsToFetch.add(id);
            }
        }

        if (idsToFetch.isEmpty()) return;

        try {
            for (int i = 0; i < idsToFetch.size(); i += 50) {
                int end = Math.min(idsToFetch.size(), i + 50);
                List<String> chunk = idsToFetch.subList(i, end);
                String idsParam = String.join(",", chunk);
                
                String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + idsParam + 
                             "&vs_currencies=usd&include_24hr_change=true&include_market_cap=true&include_24hr_vol=true";

                System.out.println("Fetching Batch Prices for " + chunk.size() + " coins...");
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, getHeaders(), Map.class);
                Map body = response.getBody();

                if (body != null) {
                    for (String sym : symbols) {
                        String id = SYMBOL_TO_ID.get(sym);
                        if (id != null && body.containsKey(id)) {
                            Map<String, Object> data = (Map<String, Object>) body.get(id);
                            double price = getDouble(data.get("usd"));
                            double change = getDouble(data.get("usd_24h_change"));
                            double cap = getDouble(data.get("usd_market_cap"));
                            double vol = getDouble(data.get("usd_24h_vol"));
                            
                            marketCache.put(sym, new MarketData(price, change, cap, vol));
                            cacheTimestamps.put(sym, System.currentTimeMillis());
                        }
                    }
                }
            }
        } catch (HttpClientErrorException.TooManyRequests e) {
            System.err.println("RATE LIMIT HIT! Using cached data.");
        } catch (Exception e) {
            System.err.println("Batch Fetch Failed: " + e.getMessage());
        }
    }

    public List<double[]> getHistoricalPrices(String symbol, int days) {
        if ("SCAM".equalsIgnoreCase(symbol)) {
            List<double[]> mockData = new ArrayList<>();
            long now = System.currentTimeMillis();
            for(int i=0; i<days; i++) mockData.add(new double[]{now - (i*86400000L), 0.01});
            return mockData;
        }

        String id = SYMBOL_TO_ID.get(symbol.toUpperCase());
        if (id == null) return Collections.emptyList();

        String cacheKey = symbol + "_" + days;
        if (historyCache.containsKey(cacheKey)) return historyCache.get(cacheKey);

        try {
            Thread.sleep(200); 
            String url = "https://api.coingecko.com/api/v3/coins/" + id + "/market_chart?vs_currency=usd&days=" + days;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, getHeaders(), Map.class);
            Map body = response.getBody();

            if (body != null && body.containsKey("prices")) {
                List<List<Object>> rawPrices = (List<List<Object>>) body.get("prices");
                List<double[]> cleanPrices = new ArrayList<>();
                for (List<Object> point : rawPrices) {
                    if (point.size() >= 2) {
                        double time = getDouble(point.get(0));
                        double price = getDouble(point.get(1));
                        cleanPrices.add(new double[]{time, price});
                    }
                }
                historyCache.put(cacheKey, cleanPrices);
                return cleanPrices;
            }
        } catch (Exception e) {
            System.err.println("Chart Fetch Error for " + symbol + ": " + e.getMessage());
            
            return getLocalHistory(symbol);
        }
        return Collections.emptyList();
    }

    private List<double[]> getLocalHistory(String symbol) {
        try {
            
            List<PriceSnapshot> snapshots = priceSnapshotRepository.findByAssetSymbolOrderByCapturedAtDesc(symbol.toUpperCase());
            List<double[]> data = new ArrayList<>();
            for (PriceSnapshot snap : snapshots) {
                if(snap.getCapturedAt() != null) {
                    long time = java.sql.Timestamp.valueOf(snap.getCapturedAt()).getTime();
                    data.add(new double[]{time, snap.getPrice()});
                }
            }
            return data;
        } catch (Exception e) {
            System.err.println("DB History fetch failed: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean isValidCache(String symbol) {
        return marketCache.containsKey(symbol) && 
               cacheTimestamps.containsKey(symbol) && 
               (System.currentTimeMillis() - cacheTimestamps.get(symbol) < CACHE_DURATION_MS);
    }

    @Scheduled(fixedRate = 300000) 
    public void refreshLivePrices() {
        if (marketCache.isEmpty()) return;
        System.out.println("Cron: Refreshing " + marketCache.size() + " active coins...");
        for (String key : marketCache.keySet()) cacheTimestamps.remove(key); 
        fetchPricesForSymbols(marketCache.keySet());
    }

    @Scheduled(cron = "0 0 * * * *") 
    public void archivePriceSnapshots() {
        if (marketCache.isEmpty()) return;
        List<PriceSnapshot> snapshots = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, MarketData> entry : marketCache.entrySet()) {
            snapshots.add(new PriceSnapshot(entry.getKey(), entry.getValue().price, now));
        }
        priceSnapshotRepository.saveAll(snapshots);
    }

    private double getDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return 0.0;
    }
}