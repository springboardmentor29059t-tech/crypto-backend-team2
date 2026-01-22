package com.crypto.portfolio.controller;

import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.UserRepository;
import com.crypto.portfolio.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pnl/personal")
public class PnlController {

    @Autowired
    private com.crypto.portfolio.repository.AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketService marketService;

    @GetMapping("/summary")
    public ResponseEntity<?> getPersonalPnlSummary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<com.crypto.portfolio.model.Asset> assets = assetRepository.findByUserId(user.getId());

        double totalInvested = 0;
        double currentValue = 0;
        double unrealizedPnl = 0;
        // Realized PnL would require Trade history. For now, we sum up Unrealized from
        // current assets based on avg buy price.
        // If we want realized, we need to query TradeRepository for 'SELL' trades and
        // calculate diff.
        // For this step, we'll focus on the Asset snapshot.
        double realizedPnl = 0;

        List<Map<String, Object>> assetBreakdown = new ArrayList<>();

        for (com.crypto.portfolio.model.Asset asset : assets) {
            double qty = asset.getAmount().doubleValue();
            double avgCost = asset.getAvgBuyPrice().doubleValue();
            double price = marketService.getPriceBySymbol(asset.getSymbol());

            // Fallback if price is 0
            if (price == 0)
                price = avgCost;

            double invested = qty * avgCost;
            double currVal = qty * price;
            double unPnl = currVal - invested;

            totalInvested += invested;
            currentValue += currVal;
            unrealizedPnl += unPnl;

            Map<String, Object> item = new HashMap<>();
            item.put("symbol", asset.getSymbol());
            item.put("quantity", qty);
            item.put("avgCost", avgCost);
            item.put("currentPrice", price);
            item.put("realizedPnl", 0); // Placeholder
            item.put("unrealizedPnl", unPnl);
            assetBreakdown.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalInvested", totalInvested);
        response.put("currentValue", currentValue);
        response.put("realizedPnl", realizedPnl);
        response.put("unrealizedPnl", unrealizedPnl);
        response.put("assetBreakdown", assetBreakdown);
        response.put("dataSource", "LIVE");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/csv")
    public void exportPnlCsv(HttpServletResponse response, @AuthenticationPrincipal UserDetails userDetails)
            throws java.io.IOException {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        List<com.crypto.portfolio.model.Asset> assets = assetRepository.findByUserId(user.getId());

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"pnl_personal.csv\"");

        try (java.io.PrintWriter writer = response.getWriter()) {
            writer.println("Symbol,Quantity,Avg Cost,Current Price,Unrealized P&L");
            for (com.crypto.portfolio.model.Asset asset : assets) {
                double qty = asset.getAmount().doubleValue();
                double avgCost = asset.getAvgBuyPrice().doubleValue();
                double price = marketService.getPriceBySymbol(asset.getSymbol());
                if (price == 0)
                    price = avgCost;

                double unPnl = (qty * price) - (qty * avgCost);

                writer.printf("%s,%.4f,%.2f,%.2f,%.2f%n",
                        asset.getSymbol(), qty, avgCost, price, unPnl);
            }
        }
    }
}
