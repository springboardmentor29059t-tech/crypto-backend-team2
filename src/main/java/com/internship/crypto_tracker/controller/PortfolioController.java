package com.internship.crypto_tracker.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.internship.crypto_tracker.model.ApiKey;
import com.internship.crypto_tracker.model.Holding;
import com.internship.crypto_tracker.model.User;
import com.internship.crypto_tracker.repository.ApiKeyRepository;
import com.internship.crypto_tracker.repository.HoldingRepository;
import com.internship.crypto_tracker.repository.UserRepository;
import com.internship.crypto_tracker.service.BinanceAccountService;
import com.internship.crypto_tracker.service.PortfolioService;
import com.internship.crypto_tracker.util.EncryptionUtils;

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

    @Autowired
    private PortfolioService portfolioService;

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
                    .orElseThrow(() -> new RuntimeException("No Binance API Keys found."));

            String decryptedSecret = encryptionUtils.decrypt(binanceKey.getApiSecret());

            List<Map<String, Object>> balances = binanceAccountService.getAccountDetails(binanceKey.getApiKey(), decryptedSecret);
            binanceAccountService.saveHoldingsForUser(userId, balances);

            int syncedCount = 0;
            for (Map<String, Object> coin : balances) {
                String symbol = (String) coin.get("asset");
            
                BigDecimal free = new BigDecimal(coin.get("free").toString());
                BigDecimal locked = new BigDecimal(coin.get("locked").toString());
                
                if (free.add(locked).compareTo(BigDecimal.ZERO) > 0) {
                    
                    if (!symbol.equals("USDT")) {
                        binanceAccountService.syncTradesForUser(userId, binanceKey.getApiKey(), decryptedSecret, symbol + "USDT");
                        syncedCount++;
                    }
                }
            }

            return ResponseEntity.ok("Synced portfolio and trades for " + syncedCount + " assets.");

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

    @GetMapping("/dashboard")
    public ResponseEntity<List<com.internship.crypto_tracker.dto.PortfolioAssetDTO>> getDashboard() {
        User user = getCurrentUser();
        List<com.internship.crypto_tracker.dto.PortfolioAssetDTO> data = portfolioService.getPortfolioSummary(user.getId());
        return ResponseEntity.ok(data);
    }

    //Testing purpose 
    @PostMapping("/trade/execute")
    public ResponseEntity<?> executeTestTrade(@RequestParam String symbol, 
                                              @RequestParam String side, 
                                              @RequestParam String quantity) {
        try {
            User user = getCurrentUser();
            
            
            ApiKey binanceKey = apiKeyRepository.findByUserIdAndExchangeName(user.getId(), "Binance")
                    .orElseThrow(() -> new RuntimeException("No Binance API Keys found."));

            String decryptedSecret = encryptionUtils.decrypt(binanceKey.getApiSecret());

            
            String result = binanceAccountService.placeTrade(
                binanceKey.getApiKey(), 
                decryptedSecret, 
                symbol,     
                side,      
                quantity    
            );

            
            binanceAccountService.syncTradesForUser(user.getId(), binanceKey.getApiKey(), decryptedSecret, symbol);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}