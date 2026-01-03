package com.crypto.tracker.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set; 

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.connector.client.impl.SpotClientImpl;
import com.crypto.tracker.model.ExchangeCredential;
import com.crypto.tracker.model.Holding;
import com.crypto.tracker.model.Trade;
import com.crypto.tracker.repository.ExchangeCredentialRepository;
import com.crypto.tracker.repository.HoldingRepository;
import com.crypto.tracker.repository.TradeRepository;
import com.crypto.tracker.util.EncryptionUtil;

@Service
public class BinanceService {

    @Autowired
    private CryptoPriceService cryptoPriceService;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private TradeRepository tradeRepository; 

    @Autowired
    private ExchangeCredentialRepository credentialRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    // --- Method 1: Sync by looking up DB ---
    public List<Trade> fetchAccountHistory(Long userId, String exchangeName) {
        Optional<ExchangeCredential> credsOpt = credentialRepository.findByUserIdAndExchangeName(userId, exchangeName);
        if (credsOpt.isEmpty())
            return new ArrayList<>();

        ExchangeCredential creds = credsOpt.get();
        String apiKey = encryptionUtil.decrypt(creds.getEncryptedApiKey());
        String secretKey = encryptionUtil.decrypt(creds.getEncryptedSecret());
        String apiUrl = creds.getExchange().getUrl();
        Long exchangeId = creds.getExchange().getId();

        return performSync(userId, apiKey, secretKey, apiUrl, exchangeId, exchangeName);
    }

    // --- Method 2: Direct Sync ---
    public List<Trade> fetchAccountHistory(Long userId, String apiKey, String secretKey, String apiUrl,
            Long exchangeId) {
        return performSync(userId, apiKey, secretKey, apiUrl, exchangeId, "Binance");
    }

    // --- Shared Logic ---
    private List<Trade> performSync(Long userId, String apiKey, String secretKey, String apiUrl, Long exchangeId,
            String exchangeName) {
        List<Trade> trades = new ArrayList<>();
        if (apiUrl == null || apiUrl.isEmpty())
            apiUrl = "https://testnet.binance.vision";

        try {
            SpotClientImpl client = new SpotClientImpl(apiKey, secretKey, apiUrl);
            Map<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("recvWindow", 60000L);

            String result = client.createTrade().account(parameters);
            JSONObject json = new JSONObject(result);
            JSONArray balances = json.getJSONArray("balances");

            System.out.println("--- SYNCING USER " + userId + " [" + exchangeName + "] ---");
            Set<String> supportedCoins = cryptoPriceService.getSupportedSymbols();

            for (int i = 0; i < balances.length(); i++) {
                JSONObject asset = balances.getJSONObject(i);
                String symbol = asset.getString("asset").toUpperCase();
                double free = Double.parseDouble(asset.getString("free"));
                double locked = Double.parseDouble(asset.getString("locked"));
                double total = free + locked;

                if (total > 0.0 && supportedCoins.contains(symbol)) {
                    double marketPrice = cryptoPriceService.getCurrentPrice(symbol);

                    if (marketPrice > 0) {
                        
                        var existingHolding = holdingRepository.findByUserIdAndAssetSymbol(userId, symbol);

                        if (existingHolding.isEmpty()) {
                            System.out.println("New Asset Found: " + symbol + " -> Creating Initial Trade Record");
                            Trade t = new Trade();
                            t.setUserId(userId);
                            t.setAssetSymbol(symbol);
                            t.setQuantity(total);
                            t.setPrice(marketPrice);
                            t.setSide("BUY");
                            t.setExchangeName(exchangeName);
                            t.setStorageType("Exchange");
                            t.setFee(0.0);
                            t.setExecutedAt(LocalDateTime.now());

                            double totalValue = total * marketPrice;
                            double estimatedFee = totalValue * 0.001; // 0.1%
                            t.setFee(estimatedFee);

                            tradeRepository.save(t); 
                            trades.add(t);
                        }

                        updateHoldingTable(userId, symbol, total, marketPrice, exchangeId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Binance Sync Error (" + exchangeName + "): " + e.getMessage());
        }
        return trades;
    }

    private void updateHoldingTable(Long userId, String symbol, double quantity, double currentPrice, Long exchangeId) {
        try {
            Optional<Holding> existing = holdingRepository.findByUserIdAndAssetSymbol(userId, symbol);
            Holding holding = existing.orElse(new Holding());

            if (!existing.isPresent()) {
                holding.setUserId(userId);
                holding.setSymbol(symbol);
                holding.setWalletType("EXCHANGE");
            }
            holding.setQuantity(quantity);
            holding.setAvgCost(currentPrice);
            holding.setUpdatedAt(LocalDateTime.now());
            holding.setExchangeId(exchangeId);

            holdingRepository.save(holding);
            System.out.println("Updated DB Holding: " + symbol);
        } catch (Exception e) {
            System.err.println("Failed to save holding: " + e.getMessage());
        }
    }
}