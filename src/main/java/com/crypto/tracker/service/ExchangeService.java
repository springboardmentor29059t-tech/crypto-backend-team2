package com.crypto.tracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crypto.tracker.model.Exchange;
import com.crypto.tracker.model.ExchangeCredential;
import com.crypto.tracker.model.User;
import com.crypto.tracker.repository.ExchangeCredentialRepository;
import com.crypto.tracker.repository.ExchangeRepository;
import com.crypto.tracker.repository.UserRepository;
import com.crypto.tracker.util.EncryptionUtil; 

@Service
public class ExchangeService {

    @Autowired
    private ExchangeCredentialRepository credentialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private BinanceService binanceService;

    // --- 1. Connect New Exchange (Secure + Instant Sync) ---
    @Transactional
    public ExchangeCredential connectExchange(Long userId, String exchangeName, String apiKey, String apiSecret, String label) {
        // A. Validate User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // B. Find or Create Exchange (With Dynamic URL Logic)
        Exchange exchange = exchangeRepository.findByName(exchangeName)
                .orElseGet(() -> {
                    Exchange newEx = new Exchange(exchangeName);
                    
                    // Set API URL based on name
                    if (exchangeName.toLowerCase().contains("testnet")) {
                        newEx.setUrl("https://testnet.binance.vision");
                    } else if (exchangeName.toLowerCase().contains("binance")) {
                        newEx.setUrl("https://api.binance.com"); // Real Binance URL
                    } else {
                        newEx.setUrl("https://api.binance.com"); // Default fallback
                    }
                    
                    return exchangeRepository.save(newEx);
                });

        // C. Encrypt Keys (Security at Rest)
        String encKey = encryptionUtil.encrypt(apiKey);
        String encSecret = encryptionUtil.encrypt(apiSecret);

        // D. Save Credentials
        ExchangeCredential creds = credentialRepository.findByUserIdAndExchangeName(userId, exchangeName)
                .orElse(new ExchangeCredential());

        creds.setUserId(userId);
        creds.setExchange(exchange);
        creds.setEncryptedApiKey(encKey);
        creds.setEncryptedSecret(encSecret);
        creds.setLabel(label);
        
        ExchangeCredential savedCreds = credentialRepository.save(creds);

        // E. INSTANT SYNC (Option 1)
        if (exchangeName.toLowerCase().contains("binance")) {
            System.out.println("Auto-syncing new connection for User " + userId);

            String finalUrl = exchange.getUrl(); 
            Long exchangeId = exchange.getId();
            
            // Run in a separate thread so the HTTP response is instant
            new Thread(() -> {
                try {
                    
                    binanceService.fetchAccountHistory(userId, apiKey, apiSecret, finalUrl, exchangeId);
                } catch (Exception e) {
                    System.err.println("Initial Auto-sync failed: " + e.getMessage());
                }
            }).start();
        }

        return savedCreds;
    }

    // --- 2. Get User's Connected Exchanges ---
    public List<ExchangeCredential> getUserConnections(Long userId) {
        return credentialRepository.findByUserId(userId);
    }

    // --- 3. Sync Trades (Used by Scheduler) ---
    public void syncTrades(Long userId) {
        List<ExchangeCredential> connections = credentialRepository.findByUserId(userId);

        for (ExchangeCredential cred : connections) {
            String exchangeName = cred.getExchange().getName();

            // Support both 'Binance' and 'Binance Testnet'
            if (exchangeName.toLowerCase().contains("binance")) {
                System.out.println("Triggering Scheduled Sync for User " + userId + " -> " + exchangeName);
                try {
                    
                    binanceService.fetchAccountHistory(userId, exchangeName);
                } catch (Exception e) {
                    System.err.println("Sync Error: " + e.getMessage());
                }
            }
        }
    }
}