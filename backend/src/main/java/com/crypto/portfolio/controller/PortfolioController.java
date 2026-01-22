package com.crypto.portfolio.controller;

import com.crypto.portfolio.model.Holding;
import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.HoldingRepository;
import com.crypto.portfolio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PortfolioController.class);

    @Autowired
    private com.crypto.portfolio.repository.AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.crypto.portfolio.service.MarketService marketService;

    @Autowired
    private com.crypto.portfolio.repository.TradeRepository tradeRepository;

    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadHoldingsCsv(@RequestParam("file") MultipartFile file, Principal principal) {
        logger.info("PortfolioController: uploadHoldingsCsv accessed by "
                + (principal != null ? principal.getName() : "null"));
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (file.isEmpty())
            return ResponseEntity.badRequest().body("File is empty");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int count = 0;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length < 3)
                    continue;

                String symbol = values[0].trim().toUpperCase();
                java.math.BigDecimal qty = new java.math.BigDecimal(values[1].trim());
                java.math.BigDecimal price = new java.math.BigDecimal(values[2].trim());
                String source = values.length > 4 ? values[4].trim() : "Binance";

                java.util.Optional<com.crypto.portfolio.model.Asset> existing = assetRepository
                        .findByUserIdAndSymbol(user.getId(), symbol);
                com.crypto.portfolio.model.Asset asset;
                if (existing.isPresent()) {
                    asset = existing.get();
                    // Weighted avg update
                    java.math.BigDecimal totalQty = asset.getAmount().add(qty);
                    java.math.BigDecimal currentCost = asset.getAmount().multiply(asset.getAvgBuyPrice());
                    java.math.BigDecimal newCost = qty.multiply(price);

                    if (totalQty.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        java.math.BigDecimal avgPrice = currentCost.add(newCost).divide(totalQty,
                                java.math.RoundingMode.HALF_UP);
                        asset.setAmount(totalQty);
                        asset.setAvgBuyPrice(avgPrice);
                    }
                    asset.setSource(source);
                } else {
                    asset = new com.crypto.portfolio.model.Asset();
                    asset.setUser(user);
                    asset.setSymbol(symbol);
                    asset.setName(symbol);
                    asset.setAmount(qty);
                    asset.setAvgBuyPrice(price);
                    asset.setSource(source);
                }
                assetRepository.save(asset);
                count++;
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Uploaded successfully");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error parsing CSV: " + e.getMessage());
        }
    }

    @PostMapping("/upload-trades-csv")
    public ResponseEntity<?> uploadTradesCsv(@RequestParam("file") MultipartFile file, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (file.isEmpty())
            return ResponseEntity.badRequest().body("File is empty");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int count = 0;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length < 7)
                    continue;

                String symbol = values[0].trim().toUpperCase();
                String side = values[1].trim().toUpperCase();
                double qtyD = Double.parseDouble(values[2].trim());
                double priceD = Double.parseDouble(values[3].trim());
                double feeD = Double.parseDouble(values[4].trim());
                String exchange = values[5].trim();
                String dateStr = values[6].trim();

                java.math.BigDecimal qty = java.math.BigDecimal.valueOf(qtyD);
                java.math.BigDecimal price = java.math.BigDecimal.valueOf(priceD);
                java.math.BigDecimal fee = java.math.BigDecimal.valueOf(feeD);

                com.crypto.portfolio.model.Trade trade = new com.crypto.portfolio.model.Trade(
                        user, symbol, side, qtyD, priceD, feeD, exchange, LocalDate.parse(dateStr).atStartOfDay());
                tradeRepository.save(trade);

                // Update Asset
                java.util.Optional<com.crypto.portfolio.model.Asset> existing = assetRepository
                        .findByUserIdAndSymbol(user.getId(), symbol);
                com.crypto.portfolio.model.Asset asset;

                if (existing.isPresent()) {
                    asset = existing.get();
                    if ("BUY".equalsIgnoreCase(side)) {
                        java.math.BigDecimal totalQty = asset.getAmount().add(qty);
                        java.math.BigDecimal currentCost = asset.getAmount().multiply(asset.getAvgBuyPrice());
                        java.math.BigDecimal newCost = qty.multiply(price).add(fee);

                        if (totalQty.compareTo(java.math.BigDecimal.ZERO) > 0) {
                            java.math.BigDecimal avgPrice = currentCost.add(newCost).divide(totalQty,
                                    java.math.RoundingMode.HALF_UP);
                            asset.setAmount(totalQty);
                            asset.setAvgBuyPrice(avgPrice);
                        }
                    } else if ("SELL".equalsIgnoreCase(side)) {
                        asset.setAmount(asset.getAmount().subtract(qty));
                    }
                    if (asset.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        assetRepository.delete(asset);
                    } else {
                        assetRepository.save(asset);
                    }
                } else if ("BUY".equalsIgnoreCase(side)) {
                    asset = new com.crypto.portfolio.model.Asset();
                    asset.setUser(user);
                    asset.setSymbol(symbol);
                    asset.setName(symbol);
                    asset.setAmount(qty);
                    // Avg price including fee
                    java.math.BigDecimal cost = qty.multiply(price).add(fee);
                    if (qty.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        asset.setAvgBuyPrice(cost.divide(qty, java.math.RoundingMode.HALF_UP));
                    } else {
                        asset.setAvgBuyPrice(price);
                    }
                    asset.setSource(exchange);
                    assetRepository.save(asset);
                }

                count++;
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Uploaded successfully");
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error parsing CSV: " + e.getMessage());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<com.crypto.portfolio.dto.PortfolioSummaryDto> getPortfolioSummary(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<com.crypto.portfolio.model.Asset> assets = assetRepository.findByUserId(user.getId());

        List<com.crypto.portfolio.dto.AssetDetailDto> assetDetails = new java.util.ArrayList<>();
        double totalValue = 0;

        for (com.crypto.portfolio.model.Asset asset : assets) {
            double livePrice = marketService.getPriceBySymbol(asset.getSymbol());
            // Fallback to avg buy price if live price is 0/unavailable? Logic from before.
            double currentPrice = livePrice > 0 ? livePrice : asset.getAvgBuyPrice().doubleValue();

            double quantity = asset.getAmount().doubleValue();
            double avgBuyPrice = asset.getAvgBuyPrice().doubleValue();

            double currentValue = quantity * currentPrice;
            double investmentValue = quantity * avgBuyPrice;
            double pnl = currentValue - investmentValue;

            com.crypto.portfolio.dto.AssetDetailDto detail = new com.crypto.portfolio.dto.AssetDetailDto(
                    asset.getId(),
                    asset.getSymbol(),
                    asset.getName(),
                    quantity,
                    avgBuyPrice,
                    currentPrice,
                    currentValue,
                    pnl);
            assetDetails.add(detail);
            totalValue += currentValue;
        }

        com.crypto.portfolio.dto.PortfolioSummaryDto summary = new com.crypto.portfolio.dto.PortfolioSummaryDto(
                totalValue,
                assetDetails,
                java.time.LocalDateTime.now());

        return ResponseEntity.ok(summary);
    }

    // CRUD endpoints (addHolding, updateHolding, deleteHolding) REMOVED
    // These are handled by AssetController (mapped to /api/assets)

    @GetMapping("/global")
    public ResponseEntity<com.crypto.portfolio.dto.PortfolioSummaryDto> getGlobalPortfolio() {
        // Fetch top 50 global assets
        List<Map<String, Object>> topCoins = marketService.getTopCoins(1, 50);
        List<com.crypto.portfolio.dto.AssetDetailDto> globalAssets = new java.util.ArrayList<>();
        double totalGlobalCap = 0;

        if (topCoins != null) {
            for (Map<String, Object> coin : topCoins) {
                String symbol = ((String) coin.get("symbol")).toUpperCase();
                String name = (String) coin.get("name");

                double price = 0.0;
                Object priceObj = coin.get("current_price");
                if (priceObj instanceof Number) {
                    price = ((Number) priceObj).doubleValue();
                }

                double marketCap = 0.0;
                Object mcObj = coin.get("market_cap");
                if (mcObj instanceof Number) {
                    marketCap = ((Number) mcObj).doubleValue();
                }

                double change24h = 0.0;
                Object changeObj = coin.get("price_change_percentage_24h");
                if (changeObj instanceof Number) {
                    change24h = ((Number) changeObj).doubleValue();
                }

                String riskLevel = Math.abs(change24h) > 5.0 ? "HIGH" : (Math.abs(change24h) > 2.0 ? "MEDIUM" : "LOW");

                com.crypto.portfolio.dto.AssetDetailDto detail = new com.crypto.portfolio.dto.AssetDetailDto(
                        0L,
                        symbol,
                        name,
                        null,
                        null,
                        price,
                        marketCap,
                        null,
                        riskLevel,
                        marketCap,
                        change24h);
                globalAssets.add(detail);
                totalGlobalCap += marketCap;
            }
        }

        com.crypto.portfolio.dto.PortfolioSummaryDto summary = new com.crypto.portfolio.dto.PortfolioSummaryDto(
                totalGlobalCap,
                globalAssets,
                java.time.LocalDateTime.now());
        return ResponseEntity.ok(summary);
    }
}