package com.crypto.portfolio.service;

import com.crypto.portfolio.model.Holding;
import com.crypto.portfolio.repository.HoldingRepository;
import com.crypto.portfolio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Table;
import com.lowagie.text.Font;
import com.lowagie.text.Cell;
import java.awt.Color;

import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class ReportService {

    @Autowired
    private com.crypto.portfolio.repository.AssetRepository assetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.crypto.portfolio.service.MarketService marketService;

    public void generatePortfolioCsv(HttpServletResponse response, String userEmail) throws java.io.IOException {
        var user = userRepository.findByEmail(userEmail).orElseThrow();
        List<com.crypto.portfolio.model.Asset> assets = assetRepository.findByUserId(user.getId());

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Symbol,Name,Quantity,Buy Price,Value,Source");
            for (com.crypto.portfolio.model.Asset a : assets) {
                java.math.BigDecimal val = a.getAmount().multiply(a.getAvgBuyPrice());

                writer.printf("%s,%s,%.4f,%.2f,%.2f,%s%n",
                        a.getSymbol(), a.getName(), a.getAmount(), a.getAvgBuyPrice(),
                        val, a.getSource());
            }
        }
    }

    public void generatePortfolioPdf(HttpServletResponse response, String userEmail) throws java.io.IOException {
        var user = userRepository.findByEmail(userEmail).orElseThrow();
        List<com.crypto.portfolio.model.Asset> assets = assetRepository.findByUserId(user.getId());

        try (Document document = new Document()) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
            document.add(new Paragraph("Portfolio Report for " + user.getFullName(), titleFont));
            document.add(new Paragraph("Generated on: " + java.time.LocalDateTime.now()));
            document.add(new Paragraph(" ")); // Spacer

            Table table = new Table(5);
            table.setWidth(100);
            table.setPadding(3);

            table.addCell(new Cell("Symbol"));
            table.addCell(new Cell("Name"));
            table.addCell(new Cell("Quantity"));
            table.addCell(new Cell("Price"));
            table.addCell(new Cell("Value (Invested)"));

            for (com.crypto.portfolio.model.Asset a : assets) {
                table.addCell(a.getSymbol());
                table.addCell(a.getName());
                table.addCell(a.getAmount().toPlainString());
                table.addCell(a.getAvgBuyPrice().toPlainString());
                table.addCell(a.getAmount().multiply(a.getAvgBuyPrice()).toPlainString());
            }

            document.add(table);
        } catch (Exception e) {
            throw new java.io.IOException("Error generating PDF", e);
        }
    }

    public void generateRiskPdf(HttpServletResponse response, String userEmail) throws java.io.IOException {
        var user = userRepository.findByEmail(userEmail).orElseThrow();
        List<com.crypto.portfolio.model.Asset> assets = assetRepository.findByUserId(user.getId());

        // 1. Calculate Risk Metrics (Logic from RiskController)
        double totalValue = 0;
        java.util.Map<String, Double> values = new java.util.HashMap<>();

        for (com.crypto.portfolio.model.Asset asset : assets) {
            double price = marketService.getPriceBySymbol(asset.getSymbol());
            if (price <= 0) {
                price = asset.getAvgBuyPrice().doubleValue();
            }
            double val = asset.getAmount().doubleValue() * price;
            values.put(asset.getSymbol(), val);
            totalValue += val;
        }

        double hhi = 0;
        double maxAllocation = 0;
        String concentratedAsset = "None";
        List<java.util.Map<String, Object>> distribution = new java.util.ArrayList<>();
        double stablecoinValue = 0;

        if (totalValue > 0) {
            for (java.util.Map.Entry<String, Double> entry : values.entrySet()) {
                double share = (entry.getValue() / totalValue) * 100;
                hhi += (share * share);

                if (share > maxAllocation) {
                    maxAllocation = share;
                    concentratedAsset = entry.getKey();
                }

                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("symbol", entry.getKey());
                item.put("percentage", share);
                item.put("value", entry.getValue());
                distribution.add(item);

                if (java.util.List.of("USDT", "USDC", "DAI", "BUSD", "FDUSD").contains(entry.getKey().toUpperCase())) {
                    stablecoinValue += entry.getValue();
                }
            }
        }

        distribution.sort((a, b) -> Double.compare((double) b.get("percentage"), (double) a.get("percentage")));
        double concentrationScore = Math.min(hhi / 100, 100);
        String riskLevel = "LOW";
        if (concentrationScore > 60 || maxAllocation > 50)
            riskLevel = "HIGH";
        else if (concentrationScore > 30 || maxAllocation > 25)
            riskLevel = "MEDIUM";

        double stablecoinRatio = totalValue > 0 ? (stablecoinValue / totalValue) * 100 : 0;

        // 2. Build PDF
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
            Font labelFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);

            document.add(new Paragraph("Risk Analysis Report: " + user.getFullName(), titleFont));
            document.add(new Paragraph("Generated on: " + java.time.LocalDateTime.now()));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("RISK SUMMARY", labelFont));
            document.add(new Paragraph("Overall Risk Level: " + riskLevel));
            document.add(new Paragraph("Concentration Score: " + String.format("%.1f", concentrationScore) + " / 100"));
            document.add(
                    new Paragraph("Diversity Score: " + String.format("%.1f", (100 - concentrationScore)) + " / 100"));
            document.add(new Paragraph("Stablecoin Ratio: " + String.format("%.1f", stablecoinRatio) + "%"));
            document.add(new Paragraph("Most Concentrated Asset: " + concentratedAsset + " ("
                    + String.format("%.1f", maxAllocation) + "%)"));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("PORTFOLIO COMPOSITION", labelFont));
            document.add(new Paragraph(" "));

            Table table = new Table(3);
            table.setWidth(100);
            table.setPadding(3);

            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
            Cell c1 = new Cell(new Paragraph("Asset Symbol", headerFont));
            c1.setBackgroundColor(Color.DARK_GRAY);
            table.addCell(c1);

            Cell c2 = new Cell(new Paragraph("Current Value (USD)", headerFont));
            c2.setBackgroundColor(Color.DARK_GRAY);
            table.addCell(c2);

            Cell c3 = new Cell(new Paragraph("Portfolio Share (%)", headerFont));
            c3.setBackgroundColor(Color.DARK_GRAY);
            table.addCell(c3);

            for (java.util.Map<String, Object> item : distribution) {
                table.addCell((String) item.get("symbol"));
                table.addCell("$" + String.format("%.2f", (double) item.get("value")));
                table.addCell(String.format("%.2f", (double) item.get("percentage")) + "%");
            }

            document.add(table);
        } catch (Exception e) {
            throw new java.io.IOException("Error generating Risk PDF", e);
        }
    }
}
