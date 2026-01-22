package com.crypto.portfolio.service;

import com.crypto.portfolio.model.Asset;
import com.crypto.portfolio.model.User;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPageEventHelper;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] generatePortfolioPdf(User user, List<Asset> assets) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterEvent());
            document.open();

            // Font styles
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("Crypto Portfolio Tracker", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // User Info
            Paragraph userInfo = new Paragraph("User: " + user.getFullName(), subHeaderFont);
            document.add(userInfo);
            Paragraph timestamp = new Paragraph(
                    "Date: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    normalFont);
            document.add(timestamp);
            document.add(Chunk.NEWLINE);

            if (assets == null || assets.isEmpty()) {
                document.add(new Paragraph("No data available", normalFont));
            } else {
                // Summary section
                BigDecimal totalInvested = assets.stream()
                        .map(a -> a.getAmount().multiply(a.getAvgBuyPrice()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Paragraph summary = new Paragraph("Portfolio Summary", subHeaderFont);
                document.add(summary);
                document.add(new Paragraph("Total Assets: " + assets.size(), normalFont));
                document.add(new Paragraph("Total Invested: $" + totalInvested.toString(), normalFont));
                document.add(Chunk.NEWLINE);

                // Table
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[] { 1.5f, 3.5f, 2f, 2.5f, 3f });

                // Table Header
                String[] headers = { "Symbol", "Name", "Quantity", "Avg Price", "Total Invested" };
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(
                            new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                    cell.setBackgroundColor(Color.BLACK);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                // Table Body
                for (Asset asset : assets) {
                    table.addCell(new Phrase(asset.getSymbol(), normalFont));
                    table.addCell(new Phrase(asset.getName(), normalFont));
                    table.addCell(new Phrase(asset.getAmount().toString(), normalFont));
                    table.addCell(new Phrase("$" + asset.getAvgBuyPrice().toString(), normalFont));
                    table.addCell(
                            new Phrase("$" + asset.getAmount().multiply(asset.getAvgBuyPrice()).toString(),
                                    normalFont));
                }

                document.add(table);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    public byte[] generateRiskReportPdf(User user, java.util.Map<String, Object> globalRisk,
            java.util.Map<String, Object> personalRisk) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterEvent());
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("Risk Analysis Report", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // User Info
            document.add(new Paragraph("User: " + user.getFullName(), normalFont));
            document.add(new Paragraph(
                    "Date: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    normalFont));
            document.add(Chunk.NEWLINE);

            // Global Risk Section
            if (globalRisk != null) {
                document.add(new Paragraph("Global Market Risk Assessment", subHeaderFont));
                document.add(Chunk.NEWLINE);

                PdfPTable globalTable = new PdfPTable(2);
                globalTable.setWidthPercentage(100);
                globalTable.addCell(createCell("Metric", sectionFont, true));
                globalTable.addCell(createCell("Value", sectionFont, true));

                globalTable.addCell(createCell("Risk Level", normalFont, false));
                globalTable.addCell(createCell(String.valueOf(globalRisk.get("riskLevel")), normalFont, false));

                globalTable.addCell(createCell("Risk Score", normalFont, false));
                globalTable
                        .addCell(createCell(String.valueOf(globalRisk.get("riskScore")) + "/100", normalFont, false));

                globalTable.addCell(createCell("Market Trend", normalFont, false));
                globalTable.addCell(createCell(String.valueOf(globalRisk.get("marketTrend")), normalFont, false));

                globalTable.addCell(createCell("Volatility", normalFont, false));
                globalTable.addCell(createCell(String.valueOf(globalRisk.get("volatility")), normalFont, false));

                document.add(globalTable);
                document.add(Chunk.NEWLINE);
            }

            // Personal Risk Section
            if (personalRisk != null) {
                document.add(new Paragraph("Personal Portfolio Risk Assessment", subHeaderFont));
                document.add(Chunk.NEWLINE);

                PdfPTable personalTable = new PdfPTable(2);
                personalTable.setWidthPercentage(100);
                personalTable.addCell(createCell("Metric", sectionFont, true));
                personalTable.addCell(createCell("Value", sectionFont, true));

                personalTable.addCell(createCell("Risk Level", normalFont, false));
                personalTable.addCell(createCell(String.valueOf(personalRisk.get("riskLevel")), normalFont, false));

                personalTable.addCell(createCell("Concentration Score", normalFont, false));
                personalTable.addCell(
                        createCell(String.format("%.2f", personalRisk.get("riskScore")) + "/100", normalFont, false));

                personalTable.addCell(createCell("Diversity Score", normalFont, false));
                personalTable.addCell(createCell(String.format("%.2f", personalRisk.get("diversityScore")) + "/100",
                        normalFont, false));

                personalTable.addCell(createCell("Max Allocation Asset", normalFont, false));
                personalTable
                        .addCell(createCell(String.valueOf(personalRisk.get("maxAllocationAsset")), normalFont, false));

                personalTable.addCell(createCell("Max Allocation %", normalFont, false));
                personalTable.addCell(
                        createCell(String.format("%.2f%%", personalRisk.get("maxAllocationPct")), normalFont, false));

                document.add(personalTable);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public byte[] generateMarketReportPdf(User user, List<java.util.Map<String, Object>> marketData) {
        Document document = new Document(PageSize.A4.rotate()); // Landscape for market data
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterEvent());
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            // Title
            Paragraph title = new Paragraph("Global Crypto Market Report", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Generated by: " + user.getFullName(), normalFont));
            document.add(new Paragraph(
                    "Date: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    normalFont));
            document.add(Chunk.NEWLINE);

            if (marketData != null && !marketData.isEmpty()) {
                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setWidths(new float[] { 1f, 2.5f, 1.5f, 2f, 2f, 2f });

                String[] headers = { "Rank", "Name", "Symbol", "Price (USD)", "Change (24h)", "Market Cap" };
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                    cell.setBackgroundColor(Color.BLACK);
                    cell.setPadding(5);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }

                for (java.util.Map<String, Object> coin : marketData) {
                    table.addCell(createCell(String.valueOf(coin.get("market_cap_rank")), normalFont, false));
                    table.addCell(createCell(String.valueOf(coin.get("name")), normalFont, false));
                    table.addCell(createCell(String.valueOf(coin.get("symbol")).toUpperCase(), normalFont, false));
                    table.addCell(createCell("$" + String.valueOf(coin.get("current_price")), normalFont, false));

                    double change = Double.parseDouble(String.valueOf(coin.get("price_change_percentage_24h")));
                    PdfPCell changeCell = new PdfPCell(new Phrase(String.format("%.2f%%", change), normalFont));
                    changeCell.setPadding(4);
                    changeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    if (change >= 0)
                        changeCell.setBackgroundColor(new Color(230, 255, 230));
                    else
                        changeCell.setBackgroundColor(new Color(255, 230, 230));
                    table.addCell(changeCell);

                    // Market Cap formatting could be improved but simple string for now
                    Object mcap = coin.get("market_cap");
                    table.addCell(createCell("$" + mcap.toString(), normalFont, false));
                }
                document.add(table);
            } else {
                document.add(new Paragraph("No market data available.", normalFont));
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private PdfPCell createCell(String content, Font font, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (isHeader) {
            cell.setBackgroundColor(Color.LIGHT_GRAY);
        }
        return cell;
    }

    class FooterEvent extends PdfPageEventHelper {
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(1);
            footer.setTotalWidth(527);
            footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            footer.getDefaultCell().setBorder(0);
            footer.addCell(new Phrase("Generated by Crypto Portfolio Tracker",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY)));
            footer.writeSelectedRows(0, -1, 34, 30, writer.getDirectContent());
        }
    }
}
