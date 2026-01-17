package com.internship.crypto_tracker.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.internship.crypto_tracker.dto.ProfitLossReportDTO;
import com.internship.crypto_tracker.model.Holding;
import com.internship.crypto_tracker.model.Trade;
import com.internship.crypto_tracker.repository.HoldingRepository;
import com.internship.crypto_tracker.repository.TradeRepository;

@Service
public class ProfitLossService {

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private CoinGeckoService coinGeckoService;

    private static class PnlResult {
        BigDecimal totalRealized = BigDecimal.ZERO;
        BigDecimal shortTerm = BigDecimal.ZERO;
        BigDecimal longTerm = BigDecimal.ZERO;
    }

    public List<ProfitLossReportDTO> generateReport(Long userId) {
        List<ProfitLossReportDTO> reportList = new ArrayList<>();
        List<Holding> holdings = holdingRepository.findByUserId(userId);

        if (holdings.isEmpty()) {
            return reportList;
        }

        List<String> symbols = holdings.stream()
                .map(Holding::getAssetSymbol)
                .collect(Collectors.toList());

        Map<String, BigDecimal> livePrices = coinGeckoService.getBatchPrices(symbols);

        for (Holding holding : holdings) {
            String symbol = holding.getAssetSymbol();
            
            BigDecimal currentPrice = livePrices.getOrDefault(symbol, BigDecimal.ZERO);
            BigDecimal quantity = holding.getQuantity() != null ? holding.getQuantity() : BigDecimal.ZERO;
            BigDecimal avgCost = holding.getAvgCost() != null ? holding.getAvgCost() : BigDecimal.ZERO;
            
            PnlResult taxData = calculateTaxPnl(userId, symbol);

            ProfitLossReportDTO report = new ProfitLossReportDTO(
                symbol,
                quantity,
                avgCost,
                currentPrice,
                taxData.totalRealized, 
                taxData.shortTerm,
                taxData.longTerm
            );

            reportList.add(report);
        }

        return reportList;
    }

    private PnlResult calculateTaxPnl(Long userId, String symbol) {
        PnlResult result = new PnlResult();
        List<Trade> trades = getTradesForSymbol(userId, symbol);

        if (trades.isEmpty()) return result;

        List<Trade> buyQueue = new ArrayList<>();

        for (Trade trade : trades) {
            String side = trade.getSide().toString().toUpperCase();

            if (side.equals("BUY")) {
                Trade buyBatch = new Trade();
                buyBatch.setPrice(trade.getPrice());
                buyBatch.setQuantity(trade.getQuantity());
                buyBatch.setExecutedAt(trade.getExecutedAt()); 
                buyQueue.add(buyBatch);

            } else if (side.equals("SELL")) {
                BigDecimal qtyToSell = trade.getQuantity();
                BigDecimal sellPrice = trade.getPrice();

                while (qtyToSell.compareTo(BigDecimal.ZERO) > 0 && !buyQueue.isEmpty()) {
                    Trade oldestBuy = buyQueue.get(0); 
                    BigDecimal availableQty = oldestBuy.getQuantity();
                    BigDecimal qtyTaken;
                    
                    if (availableQty.compareTo(qtyToSell) <= 0) {
                        qtyTaken = availableQty;
                        buyQueue.remove(0); 
                    } else {
                        qtyTaken = qtyToSell;
                        oldestBuy.setQuantity(availableQty.subtract(qtyTaken));
                    }

                    BigDecimal priceDiff = sellPrice.subtract(oldestBuy.getPrice());
                    BigDecimal profitChunk = priceDiff.multiply(qtyTaken);
                    
                    if (oldestBuy.getExecutedAt() != null && trade.getExecutedAt() != null) {
                        long daysHeld = Duration.between(oldestBuy.getExecutedAt(), trade.getExecutedAt()).toDays();
                        if (daysHeld > 365) {
                            result.longTerm = result.longTerm.add(profitChunk);
                        } else {
                            result.shortTerm = result.shortTerm.add(profitChunk);
                        }
                    } else {
                        result.shortTerm = result.shortTerm.add(profitChunk);
                    }

                    result.totalRealized = result.totalRealized.add(profitChunk);
                    qtyToSell = qtyToSell.subtract(qtyTaken);
                }
            }
        }
        return result;
    }

    private List<Trade> getTradesForSymbol(Long userId, String symbol) {
        List<Trade> trades = tradeRepository.findByUserIdAndAssetSymbolOrderByExecutedAtAsc(userId, symbol);
        
        if (trades.isEmpty()) {
            trades = tradeRepository.findByUserIdAndAssetSymbolOrderByExecutedAtAsc(userId, symbol + "USDT");
        }
        if (trades.isEmpty()) {
            trades = tradeRepository.findByUserIdAndAssetSymbolOrderByExecutedAtAsc(userId, symbol + "USD");
        }
        
        return trades;
    }

    public StringBuilder generateCsvReport(Long userId) {
        List<ProfitLossReportDTO> reports = generateReport(userId);
        StringBuilder csvContent = new StringBuilder();
        
        csvContent.append("Symbol,Quantity,Avg Cost,Current Price,Value,Unrealized P&L,Realized P&L,Short-Term(Tax),Long-Term(Tax)\n");

        for (ProfitLossReportDTO row : reports) {
            csvContent.append(row.getAssetSymbol()).append(",")
                      .append(row.getQuantity()).append(",")
                      .append(row.getAverageCost()).append(",")
                      .append(row.getCurrentPrice()).append(",")
                      .append(row.getCurrentValue()).append(",")
                      .append(row.getUnrealizedProfit()).append(",")
                      .append(row.getRealizedProfit()).append(",")
                      .append(row.getShortTermProfit()).append(",")
                      .append(row.getLongTermProfit()).append("\n");
        }
        return csvContent;
    }

    public BigDecimal calculateTotalPortfolioValue(Long userId) {
        List<ProfitLossReportDTO> reports = generateReport(userId);
        return reports.stream()
                .map(ProfitLossReportDTO::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}