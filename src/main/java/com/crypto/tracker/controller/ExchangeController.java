package com.crypto.tracker.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.crypto.tracker.model.Exchange;
import com.crypto.tracker.model.ExchangeCredential;
import com.crypto.tracker.repository.ExchangeRepository;
import com.crypto.tracker.repository.ExchangeCredentialRepository; 
import com.crypto.tracker.service.ExchangeService;

@RestController
@RequestMapping("/api/exchanges")
@CrossOrigin(origins = "http://localhost:5173") 
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private ExchangeCredentialRepository credentialRepository;

    // Helper DTO for incoming requests
    public static class ConnectRequest {
        public Long userId;
        public String exchange; 
        public String apiKey;
        public String apiSecret;
        public String label;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Exchange>> getAllSupportedExchanges() {
        return ResponseEntity.ok(exchangeRepository.findAll());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<ExchangeCredential>> getUserExchanges(@PathVariable Long userId) {
        return ResponseEntity.ok(exchangeService.getUserConnections(userId));
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connectExchange(@RequestBody ConnectRequest request) {
        try {
            ExchangeCredential savedCred = exchangeService.connectExchange(
                request.userId, 
                request.exchange, 
                request.apiKey, 
                request.apiSecret, 
                request.label
            );
            return ResponseEntity.ok(savedCred);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{credentialId}")
    public ResponseEntity<String> deleteExchangeCredential(@PathVariable Long credentialId) {
        
        if (credentialRepository.existsById(credentialId)) {
            credentialRepository.deleteById(credentialId);
            return ResponseEntity.ok("Deleted successfully");
        } else {
            return ResponseEntity.status(404).body("Credential not found");
        }
    }
}