package com.crypto.portfolio.service;

import com.crypto.portfolio.dto.TaxReportItemDTO;
import com.crypto.portfolio.dto.TaxSummaryDTO;
import com.crypto.portfolio.model.Trade;
import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.TradeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaxService {

    private final TradeRepository tradeRepository;
    private final com.crypto.portfolio.repository.AssetRepository assetRepository;

    public TaxService(TradeRepository tradeRepository,
            com.crypto.portfolio.repository.AssetRepository assetRepository) {
        this.tradeRepository = tradeRepository;
        this.assetRepository = assetRepository;
    }

    public TaxSummaryDTO calculateTax(User user, String financialYear) {
        // 1. Sync Logic: Ensure every current asset has at least enough BUY history
        syncPortfolioWithHistory(user);

        System.out.println("DEBUG: Calculating Tax for User ID: " + user.getId());

        // Fetch all trades for the user
        List<Trade> trades = tradeRepository.findByUser(user);
        System.out.println("DEBUG: Found " + trades.size() + " total trades.");

        // Sort trades by date ASC
        trades.sort(Comparator.comparing(Trade::getExecutedAt));

        // Group by Symbol to apply FIFO per asset
        java.util.Map<String, List<Trade>> tradesBySymbol = trades.stream()
                .collect(Collectors.groupingBy(Trade::getAssetSymbol));

        List<TaxReportItemDTO> reportItems = new ArrayList<>();
        double shortTermGains = 0;
        double longTermGains = 0;

        for (String symbol : tradesBySymbol.keySet()) {
            List<Trade> assetTrades = tradesBySymbol.get(symbol);
            assetTrades.sort(Comparator.comparing(Trade::getExecutedAt)); // Ensure sorted

            List<BuyLot> buyLots = new ArrayList<>();

            for (Trade trade : assetTrades) {
                String side = trade.getSide() != null ? trade.getSide().toUpperCase() : "";

                if ("BUY".equals(side)) {
                    BuyLot lot = new BuyLot(trade);
                    buyLots.add(lot);

                    // ALSO Add BUY to report so user sees the valid connection/history
                    boolean includeInReport = matchesFinancialYear(trade.getExecutedAt(), financialYear);
                    if (includeInReport) {
                        reportItems.add(new TaxReportItemDTO(
                                symbol, trade.getQuantity(), trade.getPrice(), 0.0, 0.0, "Buy/Hold",
                                trade.getExecutedAt(), trade.getExecutedAt()));
                    }

                } else if ("SELL".equals(side)) {
                    double qtyToSell = trade.getQuantity();

                    // fallback if no buy lots (Missing History / Zero Cost Basis)
                    if (buyLots.isEmpty()) {
                        System.out
                                .println("DEBUG: No BUY history for SELL of " + symbol + ". Assuming Zero Cost Basis.");
                        double matchQty = qtyToSell;
                        double buyPrice = 0.0; // Zero Cost Basis
                        double sellPrice = trade.getPrice();
                        double gain = (sellPrice - buyPrice) * matchQty;

                        // Default to Short Term if we don't know
                        String term = "Short-Term";
                        shortTermGains += gain;

                        if (matchesFinancialYear(trade.getExecutedAt(), financialYear)) {
                            reportItems.add(new TaxReportItemDTO(
                                    symbol, matchQty, buyPrice, sellPrice, gain, term, trade.getExecutedAt(),
                                    trade.getExecutedAt()));
                        }
                        qtyToSell = 0; // Handled
                    }

                    // FIFO Matching
                    while (qtyToSell > 0.00000001) { // Epsilon check
                        if (buyLots.isEmpty()) {
                            // Ran out of buy lots mid-sell
                            System.out.println("DEBUG: Ran out of BUY history for SELL of " + symbol + ". Remaining "
                                    + qtyToSell + " assumed Zero Cost Basis.");
                            double matchQty = qtyToSell;
                            double buyPrice = 0.0;
                            double sellPrice = trade.getPrice();
                            double gain = (sellPrice - buyPrice) * matchQty;
                            String term = "Short-Term";
                            shortTermGains += gain;

                            if (matchesFinancialYear(trade.getExecutedAt(), financialYear)) {
                                reportItems.add(new TaxReportItemDTO(
                                        symbol, matchQty, buyPrice, sellPrice, gain, term, trade.getExecutedAt(),
                                        trade.getExecutedAt()));
                            }
                            qtyToSell = 0;
                            break;
                        }

                        BuyLot mp = buyLots.get(0); // First in
                        double matchQty = Math.min(qtyToSell, mp.remainingQty);

                        double buyPrice = mp.price;
                        double sellPrice = trade.getPrice();
                        double gain = (sellPrice - buyPrice) * matchQty;

                        // Check Term (12 months = 365 days approximately, or use specific dates)
                        long daysHeld = java.time.temporal.ChronoUnit.DAYS.between(mp.date, trade.getExecutedAt());
                        String term = daysHeld >= 365 ? "Long-Term" : "Short-Term";

                        if ("Long-Term".equals(term)) {
                            longTermGains += gain;
                        } else {
                            shortTermGains += gain;
                        }

                        // Add to Report
                        boolean includeInReport = matchesFinancialYear(trade.getExecutedAt(), financialYear);

                        if (includeInReport) {
                            reportItems.add(new TaxReportItemDTO(
                                    symbol, matchQty, buyPrice, sellPrice, gain, term, mp.date, trade.getExecutedAt()));
                        }

                        // Update State
                        qtyToSell -= matchQty;
                        mp.remainingQty -= matchQty;

                        if (mp.remainingQty <= 0.00000001) { // Epsilon for double
                            buyLots.remove(0);
                        }
                    }
                }
            }
        }

        System.out.println("DEBUG: Calculation complete. Items: " + reportItems.size());

        double totalTaxable = shortTermGains + longTermGains;
        return new TaxSummaryDTO(financialYear, shortTermGains, longTermGains, totalTaxable, reportItems);
    }

    private void syncPortfolioWithHistory(User user) {
        // Fetch Current Portfolio
        List<com.crypto.portfolio.model.Asset> assets = assetRepository.findByUserId(user.getId());
        // Fetch History
        List<Trade> history = tradeRepository.findByUser(user);

        // Calculate Net History per Symbol
        java.util.Map<String, Double> historyNetQty = new java.util.HashMap<>();
        for (Trade t : history) {
            double amount = t.getQuantity();
            if ("SELL".equalsIgnoreCase(t.getSide()))
                amount = -amount;
            historyNetQty.put(t.getAssetSymbol(), historyNetQty.getOrDefault(t.getAssetSymbol(), 0.0) + amount);
        }

        // Compare and Fix
        for (com.crypto.portfolio.model.Asset a : assets) {
            String symbol = a.getSymbol();
            double actualQty = a.getAmount().doubleValue();
            double recordedQty = historyNetQty.getOrDefault(symbol, 0.0);

            // Precision check
            if (actualQty - recordedQty > 0.000001) {
                // MISSING HISTORY!
                // We have more assets than we bought. We must have bought them before tracking.
                // Insert a SYNTHETIC BUY
                double diff = actualQty - recordedQty;

                Trade correction = new Trade();
                correction.setUser(user);
                correction.setAssetSymbol(symbol);
                correction.setSide("BUY");
                correction.setQuantity(diff);
                correction.setPrice(a.getAvgBuyPrice().doubleValue()); // Use user's avg price as cost basis
                correction.setExchange("System Sync");

                // Set date to creation date of asset or slightly in past
                // If createdAt is missing, use NOW minus 1 day.
                // Using Asset created date is best safe bet.
                LocalDateTime refDate = a.getCreatedAt() != null ? a.getCreatedAt().toLocalDateTime()
                        : LocalDateTime.now().minusDays(1);

                correction.setExecutedAt(refDate);
                correction.setFee(0);

                tradeRepository.save(correction);
            }
        }
    }

    private boolean matchesFinancialYear(java.time.LocalDateTime date, String fy) {
        if (fy == null || fy.isEmpty() || "all".equalsIgnoreCase(fy))
            return true;
        // Format "YYYY-YY", e.g. "2024-25" implies April 1, 2024 to March 31, 2025
        // (India/UK style) or Jan-Dec?
        // Let's assume User wants Jan-Dec for simplicity OR standard April-March.
        // Given typically crypto generic -> Let's stick to Calendar Year or simple
        // parsing.
        // If user passes "2024-25", we parse 2024 start and 2025 end.
        try {
            String[] parts = fy.split("-");
            int startYear = Integer.parseInt(parts[0]);
            // If parts[1] is 2 digits (25), add 2000.
            int endYear = parts.length > 1
                    ? (parts[1].length() == 2 ? 2000 + Integer.parseInt(parts[1]) : Integer.parseInt(parts[1]))
                    : startYear + 1;

            // FY Assumption: Apr 1 (Start Year) to Mar 31 (End Year)
            // Or Simple Calendar Year 2024.
            // Let's implement Calendar Year if just "2024", or Split Year if "2024-25"

            if (fy.contains("-")) {
                LocalDateTime start = LocalDateTime.of(startYear, 4, 1, 0, 0); // Apr 1
                LocalDateTime end = LocalDateTime.of(endYear, 3, 31, 23, 59, 59); // Mar 31
                return !date.isBefore(start) && !date.isAfter(end);
            } else {
                return date.getYear() == startYear;
            }
        } catch (Exception e) {
            return true; // Fallback to show all
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void resetHistory(User user) {
        // Delete all trade history
        List<Trade> trades = tradeRepository.findByUser(user);
        tradeRepository.deleteAll(trades);

        // Re-sync current portfolio so we don't lose cost basis for existing assets
        syncPortfolioWithHistory(user);
    }

    private static class BuyLot {
        double remainingQty;
        double price;
        LocalDateTime date;

        public BuyLot(Trade trade) {
            this.remainingQty = trade.getQuantity();
            this.price = trade.getPrice();
            this.date = trade.getExecutedAt();
        }
    }
}
