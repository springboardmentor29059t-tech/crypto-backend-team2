package com.internship.crypto_tracker.controller;

import com.internship.crypto_tracker.model.ApiKey;
import com.internship.crypto_tracker.model.Holding;
import com.internship.crypto_tracker.model.User;
import com.internship.crypto_tracker.repository.ApiKeyRepository;
import com.internship.crypto_tracker.repository.HoldingRepository;
import com.internship.crypto_tracker.repository.UserRepository;
import com.internship.crypto_tracker.service.BinanceAccountService;
import com.internship.crypto_tracker.util.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private BinanceAccountService binanceAccountService;

    @Autowired
    private EncryptionUtils encryptionUtils;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    
    @PostMapping("/refresh") 
    public ResponseEntity<?> refreshPortfolio() {
        try {
            User user = getCurrentUser();
            Long userId = user.getId();

        
            ApiKey binanceKey = apiKeyRepository.findByUserIdAndExchangeName(userId, "Binance")
                    .orElseThrow(() -> new RuntimeException("No Binance API Keys found. Please connect Binance first."));

        
            String decryptedSecret = encryptionUtils.decrypt(binanceKey.getApiSecret());

            
            List<Map<String, Object>> balances = binanceAccountService.getAccountDetails(binanceKey.getApiKey(), decryptedSecret);
            binanceAccountService.saveHoldingsForUser(userId, balances);

            return ResponseEntity.ok("Portfolio updated successfully for " + user.getName());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Holding>> getPortfolio() {
        User user = getCurrentUser();
        return ResponseEntity.ok(holdingRepository.findByUserId(user.getId()));
    }

    @PostMapping("/manual")
    public ResponseEntity<?> addManualHolding(@RequestBody Map<String, Object> payload) {
        try {
            User user = getCurrentUser();
            
            String symbol = (String) payload.get("assetSymbol");
            
            BigDecimal quantity = new BigDecimal(payload.get("quantity").toString());
            String address = (String) payload.get("address"); 

            Holding holding = new Holding();
            holding.setUser(user);
            holding.setAssetSymbol(symbol);
            holding.setQuantity(quantity);
            holding.setAddress(address);
            holding.setWalletType(Holding.WalletType.WALLET); 
            
            holdingRepository.save(holding);
            
            return ResponseEntity.ok("Manual holding added successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding holding: " + e.getMessage());
        }
    }

    
    @PutMapping("/manual/{id}")
    public ResponseEntity<?> updateManualHolding(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            User user = getCurrentUser();
            Holding holding = holdingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Holding not found"));

            
            if (!holding.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You do not own this holding!");
            }

            
            if (payload.containsKey("quantity")) {
                holding.setQuantity(new BigDecimal(payload.get("quantity").toString()));
            }
            if (payload.containsKey("address")) {
                holding.setAddress((String) payload.get("address"));
            }

            holdingRepository.save(holding);
            return ResponseEntity.ok("Holding updated successfully!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating holding: " + e.getMessage());
        }
    }


    @DeleteMapping("/manual/{id}")
    public ResponseEntity<?> deleteManualHolding(@PathVariable Long id) {
        try {
            User user = getCurrentUser();
            Holding holding = holdingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Holding not found"));

            if (!holding.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You do not own this holding!");
            }

            holdingRepository.delete(holding);
            return ResponseEntity.ok("Holding deleted successfully!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting holding: " + e.getMessage());
        }
    }
}