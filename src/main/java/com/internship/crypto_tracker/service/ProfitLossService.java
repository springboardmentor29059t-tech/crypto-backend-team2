package com.internship.crypto_tracker.service;

import java.math.BigDecimal;
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
            
            BigDecimal realizedProfit = calculateRealizedProfitForSymbol(userId, symbol);

            ProfitLossReportDTO report = new ProfitLossReportDTO(
                symbol,
                quantity,
                avgCost,
                currentPrice,
                realizedProfit
            );

            reportList.add(report);
        }

        return reportList;
    }

    public StringBuilder generateCsvReport(Long userId) {
        List<ProfitLossReportDTO> reports = generateReport(userId);
        StringBuilder csvContent = new StringBuilder();
        
        csvContent.append("Symbol,Quantity,Average Cost,Current Price,Current Value,Unrealized P&L,Realized P&L\n");

        for (ProfitLossReportDTO row : reports) {
            csvContent.append(row.getAssetSymbol()).append(",");
            csvContent.append(row.getQuantity()).append(",");
            csvContent.append(row.getAverageCost()).append(",");
            csvContent.append(row.getCurrentPrice()).append(",");
            csvContent.append(row.getCurrentValue()).append(",");
            csvContent.append(row.getUnrealizedProfit()).append(",");
            csvContent.append(row.getRealizedProfit()).append("\n"); 
        }
        return csvContent;
    }

    public BigDecimal calculateTotalPortfolioValue(Long userId) {
        List<ProfitLossReportDTO> reports = generateReport(userId);
        return reports.stream()
                .map(ProfitLossReportDTO::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private BigDecimal calculateRealizedProfitForSymbol(Long userId, String symbol) {
        List<Trade> trades = getTradesForSymbol(userId, symbol);

        if (trades.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalRealizedProfit = BigDecimal.ZERO;
        List<Trade> buyQueue = new ArrayList<>();

        for (Trade trade : trades) {
            String side = trade.getSide() != null ? trade.getSide().toString().toUpperCase() : "";

            if (side.equals("BUY")) {
                Trade buyBatch = new Trade();
                buyBatch.setPrice(trade.getPrice());
                buyBatch.setQuantity(trade.getQuantity());
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
                    
                    totalRealizedProfit = totalRealizedProfit.add(profitChunk);
                    qtyToSell = qtyToSell.subtract(qtyTaken);
                }
            }
        }
        return totalRealizedProfit;
    }
}