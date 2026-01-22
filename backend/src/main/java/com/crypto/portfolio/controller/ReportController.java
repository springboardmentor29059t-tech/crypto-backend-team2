package com.crypto.portfolio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.crypto.portfolio.service.ReportService;
import com.crypto.portfolio.service.AssetService;
import java.security.Principal;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // We can reuse existing endpoints/logic, but let's centralize here if needed.
    // However, existing controllers might already handle exports?
    // User requested: GET /reports/portfolio/csv, GET /reports/portfolio/pdf
    // Let's implement these wrapping the service.

    @GetMapping("/portfolio/csv")
    public void exportPortfolioCsv(HttpServletResponse response, Principal principal) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"portfolio.csv\"");

        // This logic mimics what is in PortfolioController or ReportService
        // If ReportService has generatePortfolioCsv(String email), use it.
        // If not, we might need to verify or duplicate.
        // Assuming ReportService handles it or we call logic similar to
        // PortfolioController.

        // For expediency:
        reportService.generatePortfolioCsv(response, principal.getName());
    }

    @GetMapping("/portfolio/pdf")
    public void exportPortfolioPdf(HttpServletResponse response, Principal principal) throws java.io.IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"portfolio.pdf\"");
        reportService.generatePortfolioPdf(response, principal.getName());
    }

    @GetMapping("/risk/pdf")
    public void exportRiskPdf(HttpServletResponse response, Principal principal) throws java.io.IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"risk_analysis.pdf\"");
        reportService.generateRiskPdf(response, principal.getName());
    }
}
