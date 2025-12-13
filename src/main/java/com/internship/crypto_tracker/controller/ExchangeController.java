package com.internship.crypto_tracker.controller;

import com.internship.crypto_tracker.model.ApiKey;
import com.internship.crypto_tracker.model.Exchange;
import com.internship.crypto_tracker.model.User;
import com.internship.crypto_tracker.repository.ApiKeyRepository;
import com.internship.crypto_tracker.repository.ExchangeRepository;
import com.internship.crypto_tracker.repository.UserRepository;
import com.internship.crypto_tracker.util.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private EncryptionUtils encryptionUtils;

    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connectExchange(@RequestBody Map<String, String> request) {
        User user = getCurrentUser(); 

        String exchangeName = request.get("exchangeName");
        String apiKey = request.get("apiKey");
        String apiSecret = request.get("apiSecret");
        String label = request.get("label");

        if (apiKeyRepository.existsByKey(apiKey)) {
            return ResponseEntity.badRequest().body("Error: This API Key is already added!");
        }

        
        Exchange exchange = exchangeRepository.findByName(exchangeName)
                .orElseGet(() -> {
                    Exchange newEx = new Exchange();
                    newEx.setName(exchangeName);
                    return exchangeRepository.save(newEx);
                });

        ApiKey newKey = new ApiKey();
        newKey.setUser(user);
        newKey.setExchange(exchange);
        newKey.setKey(apiKey);
        newKey.setSecret(encryptionUtils.encrypt(apiSecret)); 
        newKey.setLabel(label);
        
        apiKeyRepository.save(newKey);

        return ResponseEntity.ok("Exchange connected successfully!");
    }

    @GetMapping("/my-keys")
    public ResponseEntity<?> getUserKeys() {
        User user = getCurrentUser();
        List<ApiKey> keys = apiKeyRepository.findByUserId(user.getId());

        List<Map<String, Object>> safeKeys = keys.stream().map(key -> {
        
            String rawKey = key.getKey();
            String maskedKey = (rawKey != null && rawKey.length() > 4) 
                ? rawKey.substring(0, 4) + "..." 
                : rawKey; 

            
            String safeLabel = (key.getLabel() != null) ? key.getLabel() : "No Label";
            String safeDate = (key.getCreatedAt() != null) ? key.getCreatedAt().toString() : "Now";

            
            return Map.<String, Object>of(
                "id", key.getId(),
                "exchange", key.getExchange().getName(),
                "label", safeLabel, 
                "apiKey", maskedKey,
                "addedAt", safeDate
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(safeKeys);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKey(@PathVariable Long id) {
        User user = getCurrentUser();
        
        
        ApiKey key = apiKeyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Key not found"));

        if (!key.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You do not own this key!");
        }

        apiKeyRepository.delete(key);
        return ResponseEntity.ok("Key deleted successfully");
    }
}