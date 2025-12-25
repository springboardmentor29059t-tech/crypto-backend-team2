package com.internship.crypto_tracker.service;

import com.internship.crypto_tracker.model.Exchange;
import com.internship.crypto_tracker.model.Holding;
import com.internship.crypto_tracker.model.User;
import com.internship.crypto_tracker.repository.ExchangeRepository;
import com.internship.crypto_tracker.repository.HoldingRepository;
import com.internship.crypto_tracker.repository.UserRepository;
import com.internship.crypto_tracker.util.BinanceSignatureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.internship.crypto_tracker.model.Trade;
import com.internship.crypto_tracker.repository.TradeRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BinanceAccountService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BinanceSignatureUtil signatureUtil;


    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private TradeRepository tradeRepository;

    private final String BINANCE_API_BASE = "https://testnet.binance.vision/api/v3";

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getAccountDetails(String apiKey, String apiSecret) {
                
        long timestamp = System.currentTimeMillis() - 2000;
        String queryString = "recvWindow=60000&timestamp=" + timestamp;
        String signature = signatureUtil.createSignature(queryString, apiSecret);
        String finalUrl = BINANCE_API_BASE + "/account?" + queryString + "&signature=" + signature;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-MBX-APIKEY", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                finalUrl, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> allBalances = (List<Map<String, Object>>) body.get("balances");
            List<Map<String, Object>> nonZeroBalances = new ArrayList<>();

            for (Map<String, Object> coin : allBalances) {
                String free = (String) coin.get("free");
                String locked = (String) coin.get("locked");
                if (Double.parseDouble(free) > 0 || Double.parseDouble(locked) > 0) {
                    nonZeroBalances.add(coin);
                }
            }
            return nonZeroBalances;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Binance account: " + e.getMessage());
        }
    }

    
    public void saveHoldingsForUser(Long userId, List<Map<String, Object>> balances) {
        
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

       
        Exchange binanceExchange = exchangeRepository.findByName("Binance")
                .orElseGet(() -> {
                    Exchange newEx = new Exchange();
                    newEx.setName("Binance");
                    return exchangeRepository.save(newEx);
                });
        
        List<Holding> oldHoldings = holdingRepository.findByUserId(userId);
        holdingRepository.deleteAll(oldHoldings);

        List<Holding> newHoldings = new ArrayList<>();
        
        for (Map<String, Object> balance : balances) {
            String symbol = (String) balance.get("asset");
            String free = (String) balance.get("free");
            String locked = (String) balance.get("locked");
            
            BigDecimal quantity = new BigDecimal(free).add(new BigDecimal(locked));

            Holding holding = new Holding();
            holding.setUser(user);
            holding.setExchange(binanceExchange);
            holding.setAssetSymbol(symbol);
            holding.setQuantity(quantity);
            holding.setWalletType(Holding.WalletType.EXCHANGE);
            
            newHoldings.add(holding);
        }

        holdingRepository.saveAll(newHoldings);
        System.out.println("Saved " + newHoldings.size() + " coins for User ID: " + userId);
    }

    public void syncTradesForUser(Long userId, String apiKey, String apiSecret, String symbol) {
        try {
            long timestamp = System.currentTimeMillis();
            String queryString = "symbol=" + symbol + "&timestamp=" + timestamp;
            String signature = signatureUtil.createSignature(queryString, apiSecret);
            String finalUrl = BINANCE_API_BASE + "/myTrades?" + queryString + "&signature=" + signature;

            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> tradeList = response.getBody();
            if (tradeList == null) return;

            User user = userRepository.findById(userId).orElseThrow();
            Exchange binance = exchangeRepository.findByName("Binance").orElse(null);

            
            for (Map<String, Object> t : tradeList) {
                
                BigDecimal qty = new BigDecimal(t.get("qty").toString());
                BigDecimal price = new BigDecimal(t.get("price").toString());
                BigDecimal fee = new BigDecimal(t.get("commission").toString());
                long time = ((Number) t.get("time")).longValue();
                LocalDateTime executedAt = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
                
                boolean isBuyer = (boolean) t.get("isBuyer");
                Trade.Side side = isBuyer ? Trade.Side.BUY : Trade.Side.SELL;

                
                if (!tradeRepository.existsByUserIdAndAssetSymbolAndExecutedAtAndQuantity(
                        userId, symbol, executedAt, qty)) {
                    
                    Trade trade = new Trade();
                    trade.setUser(user);
                    trade.setExchange(binance);
                    trade.setAssetSymbol(symbol);
                    trade.setSide(side);
                    trade.setQuantity(qty);
                    trade.setPrice(price);
                    trade.setFee(fee);
                    trade.setExecutedAt(executedAt);
                    
                    tradeRepository.save(trade);
                }
            }

            updateCostBasis(userId, symbol);

            System.out.println("Synced trades for " + symbol);

        } catch (Exception e) {
            System.err.println("Could not sync trades for " + symbol + ": " + e.getMessage());
        }
    }
    
    public String placeTrade(String apiKey, String apiSecret, String symbol, String side, String quantity) {
        try {
            long timestamp = System.currentTimeMillis();
            String queryString = "symbol=" + symbol + "&side=" + side + "&type=MARKET" + "&quantity=" + quantity + "&timestamp=" + timestamp;
            String signature = signatureUtil.createSignature(queryString, apiSecret);
            String finalUrl = BINANCE_API_BASE + "/order?" + queryString + "&signature=" + signature;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-MBX-APIKEY", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(finalUrl, HttpMethod.POST, entity, String.class);
            return "✅ Trade Executed! " + response.getBody();
        } catch (Exception e) {
            return "❌ Trade Failed: " + e.getMessage();
        }
    }

    private String extractBaseAsset(String symbol) {
    
        List<String> commonQuotes = List.of("USDT", "BUSD", "USDC", "TUSD", "BTC", "ETH", "BNB");

        for (String quote : commonQuotes) {
            if (symbol.endsWith(quote)) {
                
                return symbol.substring(0, symbol.length() - quote.length());
            }
        }
        
        return symbol;
    }

    private void updateCostBasis(Long userId, String symbol) {
    
        List<Trade> trades = tradeRepository.findByUserIdOrderByExecutedAtDesc(userId); 
        
        List<Trade> symbolTrades = trades.stream()
                .filter(t -> t.getAssetSymbol().equals(symbol))
                .collect(Collectors.toList());

        if (symbolTrades.isEmpty()) return;

        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;

        for (Trade trade : symbolTrades) {
            if (trade.getSide() == Trade.Side.BUY) {
                BigDecimal tradeCost = trade.getPrice().multiply(trade.getQuantity());
                totalCost = totalCost.add(tradeCost);
                totalQty = totalQty.add(trade.getQuantity());
            }
        }

        if (totalQty.compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal avgCost = totalCost.divide(totalQty, java.math.RoundingMode.HALF_UP);

       String assetName = extractBaseAsset(symbol);
        
        

        List<Holding> holdings = holdingRepository.findByUserId(userId);
        Holding targetHolding = holdings.stream()
                .filter(h -> h.getAssetSymbol().equals(assetName)) 
                .findFirst()
                .orElse(null);
        
        if (targetHolding != null) {
            targetHolding.setAvgCost(avgCost);
            holdingRepository.save(targetHolding);
            System.out.println("💰 Updated Avg Cost for " + symbol + ": $" + avgCost);
        }
    }
}