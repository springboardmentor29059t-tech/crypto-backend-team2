package com.crypto.portfolio.controller;

import com.crypto.portfolio.model.User;
import com.crypto.portfolio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/tax/personal")
public class TaxController {

    @Autowired
    private com.crypto.portfolio.service.TaxService taxService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/summary")
    public ResponseEntity<?> getPersonalTaxSummary(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "all") String financialYear) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        com.crypto.portfolio.dto.TaxSummaryDTO summary = taxService.calculateTax(user, financialYear);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/export/csv")
    public void exportTaxCsv(HttpServletResponse response,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "2024-25") String financialYear) throws java.io.IOException {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        com.crypto.portfolio.dto.TaxSummaryDTO summary = taxService.calculateTax(user, financialYear);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"tax_personal_" + financialYear + ".csv\"");

        try (java.io.PrintWriter writer = response.getWriter()) {
            writer.println("Symbol,Qty,Buy Price,Sell Price,Gain/Loss,Term,Buy Date,Sell Date");
            for (com.crypto.portfolio.dto.TaxReportItemDTO item : summary.getDetails()) {
                writer.printf("%s,%.4f,%.2f,%.2f,%.2f,%s,%s,%s%n",
                        item.getSymbol(),
                        item.getQuantity(),
                        item.getBuyPrice(),
                        item.getSellPrice(),
                        item.getGainLoss(),
                        item.getTermType(),
                        item.getBuyDate().toString(),
                        item.getSellDate().toString());
            }
        }
    }

    @DeleteMapping("/reset")
    public ResponseEntity<?> resetTaxHistory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        taxService.resetHistory(user);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Tax history reset successfully"));
    }
}
