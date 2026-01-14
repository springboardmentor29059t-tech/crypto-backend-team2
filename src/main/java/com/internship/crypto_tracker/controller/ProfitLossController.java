package com.internship.crypto_tracker.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.internship.crypto_tracker.dto.ProfitLossReportDTO;
import com.internship.crypto_tracker.model.User;
import com.internship.crypto_tracker.repository.UserRepository;
import com.internship.crypto_tracker.service.ProfitLossService;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfitLossController {

    @Autowired
    private ProfitLossService profitLossService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        String email = authentication.getName(); 

        Optional<User> user = userRepository.findByEmail(email);
        
        if (user.isPresent()) {
            return user.get().getId();
        } else {
            throw new RuntimeException("User not found for email: " + email);
        }
    }

    
    @GetMapping("/pnl")
    public ResponseEntity<List<ProfitLossReportDTO>> getPortfolioReport() {
        Long userId = getCurrentUserId(); 
        List<ProfitLossReportDTO> report = profitLossService.generateReport(userId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        Long userId = getCurrentUserId(); 
        
        StringBuilder csvContent = profitLossService.generateCsvReport(userId);
        byte[] csvBytes = csvContent.toString().getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=crypto_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
    
    @GetMapping("/total-balance")
    public ResponseEntity<BigDecimal> getTotalBalance() {
        Long userId = getCurrentUserId();
        BigDecimal total = profitLossService.calculateTotalPortfolioValue(userId);
        return ResponseEntity.ok(total);
    }
}