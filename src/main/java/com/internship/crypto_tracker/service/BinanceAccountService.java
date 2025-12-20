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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        System.out.println("✅ Saved " + newHoldings.size() + " coins for User ID: " + userId);
    }
}