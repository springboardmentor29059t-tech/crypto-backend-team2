package com.crypto.portfolio.controller;

import com.crypto.portfolio.dto.PortfolioSummaryDto;
import com.crypto.portfolio.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/portfolio-summary")
    public ResponseEntity<PortfolioSummaryDto> getPortfolioSummary(Principal principal) {
        try {
            String userEmail = principal.getName();
            PortfolioSummaryDto summary = dashboardService.getPortfolioSummary(userEmail);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}